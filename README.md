# FreightFox Dispatch Load Balancer

A Spring Boot service that allocates delivery orders across a fleet of vehicles. The system balances vehicle payload limits, respects priority levels (`HIGH` > `MEDIUM` > `LOW`), and minimizes travel distance using the Haversine formula.

Built as part of the FreightFox Backend Engineering assignment.

---

## Features

- **Priority-Driven Routing:** Critical shipments (`HIGH` priority) are evaluated and assigned before `MEDIUM` and `LOW` priority orders.
- **Strict Capacity Enforcement:** Orders are only placed on vehicles that have enough remaining payload capacity. No vehicle is ever overloaded.
- **Proximity Optimization:** For each order, the closest feasible vehicle is selected based on great-circle distance from its current stop to the delivery location.
- **Full Accountability:** Every order submitted is accounted for. If an order cannot fit into the fleet, it is returned in the `unassignedOrders` list with the reason.
- **Idempotent Batch Upserts:** Vehicles and orders can be submitted in batches. Re-submitting the same IDs updates existing records without creating duplicates.
- **MongoDB Persistence:** Fleet and order data are persisted in MongoDB Atlas.
- **Interactive Documentation:** Swagger UI and OpenAPI 3.1.0 specification ready out of the box.

---

## Tech Stack

- **Java:** 21 (LTS)
- **Framework:** Spring Boot 3.4.5
- **Persistence:** Spring Data MongoDB 4.4.5 / MongoDB Atlas
- **Documentation:** Springdoc OpenAPI / Swagger UI 2.8.6
- **Validation:** Jakarta Bean Validation (Hibernate Validator)
- **Testing:** JUnit 5, Mockito, MockMvc, AssertJ (68 automated tests)
- **Build Tool:** Maven Wrapper (`mvnw` / `mvnw.cmd`)
- **Containerization:** Multi-stage Dockerfile (Eclipse Temurin 21 JRE Alpine)

---

## Getting Started

### Prerequisites
- **JDK 21** installed (`java -version`)
- A running **MongoDB** instance (local on `localhost:27017` or a MongoDB Atlas cluster)
- Maven is not required on your system — the repo includes the Maven Wrapper.

### 1. Set MongoDB Connection String

Set the `MONGODB_URI` environment variable in your terminal before running:

**Linux / macOS:**
```bash
export MONGODB_URI="mongodb+srv://<username>:<password>@<cluster>.mongodb.net/freightfox?retryWrites=true&w=majority"
```

**Windows (PowerShell):**
```powershell
$env:MONGODB_URI = "mongodb+srv://<username>:<password>@<cluster>.mongodb.net/freightfox?retryWrites=true&w=majority"
```

*(If omitted, the app defaults to `mongodb://localhost:27017/freightfox`)*

### 2. Run Tests
The test suite contains 68 automated unit and integration tests covering the algorithm, distance calculations, validation rules, repository operations, and REST controllers.

```bash
# Linux / macOS
./mvnw clean test

# Windows
.\mvnw.cmd clean test
```

### 3. Build & Run Locally

```bash
# Package the executable JAR
./mvnw clean package -DskipTests

# Run via Java
java -jar target/freightfox-dispatch-load-balancer-0.0.1-SNAPSHOT.jar

# Or run directly with Maven
./mvnw spring-boot:run
```

Once started, the application listens on `http://localhost:8080`.

---

## API Documentation

When the application is running, open your browser to:
- **Swagger UI:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **OpenAPI Schema (JSON):** [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

---

## REST Endpoints & Usage

### 1. Ingest Fleet Details
Uploads a batch of vehicles with their starting location and capacity.

- **Method:** `POST`
- **URL:** `/api/dispatch/vehicles`
- **Headers:** `Content-Type: application/json`

```bash
curl -X POST "http://localhost:8080/api/dispatch/vehicles" \
  -H "Content-Type: application/json" \
  -d '{
    "vehicles": [
      {
        "vehicleId": "VEH001",
        "capacity": 100.0,
        "currentLatitude": 28.7041,
        "currentLongitude": 77.1025,
        "currentAddress": "Karol Bagh, Delhi, India"
      },
      {
        "vehicleId": "VEH002",
        "capacity": 80.0,
        "currentLatitude": 28.5355,
        "currentLongitude": 77.3910,
        "currentAddress": "Sector 18, Noida, Uttar Pradesh, India"
      }
    ]
  }'
```

**Response (`200 OK`):**
```json
{
  "message": "Vehicle details accepted.",
  "status": "success"
}
```

---

### 2. Ingest Delivery Orders
Uploads a batch of delivery orders with drop-off coordinates, weight, and priority.

- **Method:** `POST`
- **URL:** `/api/dispatch/orders`
- **Headers:** `Content-Type: application/json`

```bash
curl -X POST "http://localhost:8080/api/dispatch/orders" \
  -H "Content-Type: application/json" \
  -d '{
    "orders": [
      {
        "orderId": "ORD001",
        "latitude": 28.6139,
        "longitude": 77.2090,
        "address": "Connaught Place, Delhi, India",
        "packageWeight": 15.0,
        "priority": "HIGH"
      },
      {
        "orderId": "ORD002",
        "latitude": 28.6139,
        "longitude": 77.2090,
        "address": "Connaught Place, Delhi, India",
        "packageWeight": 10.0,
        "priority": "MEDIUM"
      }
    ]
  }'
```

**Response (`200 OK`):**
```json
{
  "message": "Delivery orders accepted.",
  "status": "success"
}
```

---

### 3. Retrieve Dispatch Plan
Generates and returns the optimized routing plan based on all currently saved vehicles and orders.

- **Method:** `GET`
- **URL:** `/api/dispatch/plan`

```bash
curl -X GET "http://localhost:8080/api/dispatch/plan"
```

**Response (`200 OK`):**
```json
{
  "dispatchPlan": [
    {
      "vehicleId": "VEH001",
      "totalLoad": 100.0,
      "totalDistance": "14.44 km",
      "assignedOrders": [
        {
          "orderId": "ORD007",
          "latitude": 28.7041,
          "longitude": 77.1025,
          "address": "Karol Bagh, Delhi, India",
          "packageWeight": 20.0,
          "priority": "HIGH"
        }
      ]
    }
  ],
  "unassignedOrders": [
    {
      "orderId": "ORD006",
      "reason": "Insufficient vehicle capacity available"
    }
  ]
}
```

If the database has no records yet, it returns empty arrays safely:
```json
{
  "dispatchPlan": [],
  "unassignedOrders": []
}
```

---

## Validation & Error Handling

Input validation is enforced using Jakarta Bean Validation annotations:
- Coordinates: Latitude (`-90.0` to `90.0`), Longitude (`-180.0` to `180.0`).
- Weights & Capacities: Must be strictly positive numbers (`> 0.0`).
- Priority: Must be one of `HIGH`, `MEDIUM`, or `LOW` (case-insensitive).
- Batch Payload Uniqueness: Submitting duplicate `orderId` or `vehicleId` entries within the same request body returns `400 Bad Request`.

---

## Dispatch Algorithm Overview

The engine uses a **Priority-aware Greedy Nearest Neighbor** strategy:
1. **Priority Sorting:** Orders are sorted primarily by priority (`HIGH` first, then `MEDIUM`, then `LOW`). Ties within the same priority are broken deterministically by `orderId` ascending.
2. **Feasibility Check:** For each order, we find all vehicles whose current available capacity can accommodate the order's weight (`remainingCapacity >= packageWeight`).
3. **Nearest Feasible Vehicle:** Among feasible vehicles, the order is assigned to the one with the shortest Haversine distance from the vehicle's last stop to the delivery location.
4. **State Update:** The order is appended to the vehicle's route, the package weight is added to its load, and the vehicle's current position advances to this drop-off.
5. **Unassigned Fallback:** If no vehicle has enough remaining capacity, the order is added to `unassignedOrders` with a clear explanation.

Distance calculation uses the spherical Haversine formula ($R = 6371.0\text{ km}$):
$$d = 2R \arcsin\left(\sqrt{\sin^2\left(\frac{\Delta \phi}{2}\right) + \cos(\phi_1)\cos(\phi_2)\sin^2\left(\frac{\Delta \lambda}{2}\right)}\right)$$

---

## Postman Collection

A ready-to-import Postman collection is located at:
```
postman/FreightFox-Dispatch.postman_collection.json
```

It contains pre-configured requests for:
1. Initial empty plan verification
2. Vehicle and order batch creation (sample dataset)
3. Validation error testing (invalid coordinates, negative weight, duplicate IDs)
4. The full 30-order, 5-vehicle benchmark dataset across Delhi NCR
5. Full plan retrieval

To use it, import the JSON file into Postman, configure the `baseUrl` variable (defaults to `http://localhost:8080`), and run the requests.

---

## Docker & Container Deployment

### Local Docker Build

A multi-stage `Dockerfile` is included to build a lightweight, production-ready container:

```bash
# Build the Docker image
docker build -t freightfox-dispatch-load-balancer .

# Run container locally
docker run -d -p 8080:8080 \
  -e MONGODB_URI="mongodb+srv://<user>:<password>@<cluster>.mongodb.net/freightfox?retryWrites=true&w=majority" \
  --name freightfox-app freightfox-dispatch-load-balancer
```

### Dockerfile Highlights
- **Multi-stage build:** Builds JAR with Maven in an Alpine builder stage, then copies only the JAR to a minimal Eclipse Temurin 21 JRE runtime.
- **Security:** Runs under a non-root user (`appuser:appgroup`).
- **Memory tuning:** Configured with `-XX:+UseContainerSupport` and `-XX:MaxRAMPercentage=75.0` to respect container cgroups and prevent out-of-memory errors on small instances.
- **Dynamic Port:** Reads the `$PORT` environment variable (compatible with cloud platforms like Render).

---

## Deploying to Render

1. Create a **New Web Service** on [Render](https://render.com).
2. Connect this GitHub repository.
3. Configure settings:
   - **Environment:** Docker
   - **Branch:** `main`
   - **Plan:** Free
4. Under **Environment Variables**, add:
   - `MONGODB_URI`: Your MongoDB Atlas connection string.
5. In your MongoDB Atlas dashboard under **Network Access**, ensure Render's outbound traffic can connect (allow `0.0.0.0/0` or dedicated static outbound IP).
6. Click **Deploy Web Service**. Render will build the Dockerfile and start the service.

---

## Project Structure

```
freightfox-dispatch-load-balancer/
├── pom.xml
├── Dockerfile
├── .dockerignore
├── postman/
│   └── FreightFox-Dispatch.postman_collection.json
└── src/
    ├── main/
    │   ├── java/com/chandan/freightfox/
    │   │   ├── DispatchApplication.java
    │   │   ├── config/OpenApiConfig.java
    │   │   ├── controller/DispatchController.java
    │   │   ├── service/
    │   │   │   ├── OrderService.java
    │   │   │   ├── VehicleService.java
    │   │   │   └── DispatchService.java
    │   │   ├── algorithm/DispatchOptimizer.java
    │   │   ├── util/HaversineCalculator.java
    │   │   ├── entity/
    │   │   │   ├── DeliveryOrder.java
    │   │   │   ├── Vehicle.java
    │   │   │   └── OrderPriority.java
    │   │   ├── repository/
    │   │   │   ├── OrderRepository.java
    │   │   │   └── VehicleRepository.java
    │   │   ├── dto/
    │   │   └── exception/GlobalExceptionHandler.java
    │   └── resources/application.properties
    └── test/
        └── java/com/chandan/freightfox/
            ├── DispatchApplicationTests.java
            ├── algorithm/
            ├── controller/
            ├── dto/
            ├── service/
            └── util/
```

---

## Author

**Chandan Gupta**  
[GitHub Profile](https://github.com/chandangupta7143)
