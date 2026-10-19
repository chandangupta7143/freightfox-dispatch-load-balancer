# FreightFox Dispatch Load Balancer

A high-performance, test-driven Spring Boot backend service that optimizes the allocation and routing of delivery orders across a fleet of vehicles. The engine balances payload capacities, prioritizes urgent shipments, and minimizes total travel distance using the spherical Haversine formula.

---

## 1. Project Overview & Business Problem

Logistics operators receive batches of delivery orders and maintain fleets of vehicles operating with varying load capacities and starting locations. The challenge is to compute a feasible, cost-effective dispatch plan that:

1. **Prioritizes Urgent Shipments:** `HIGH` priority orders must be evaluated and routed before `MEDIUM` and `LOW` priority orders.
2. **Strictly Enforces Vehicle Capacities:** The cumulative package weight on any vehicle must never exceed that vehicle's maximum carrying capacity.
3. **Minimizes Travel Distance:** Each order is dynamically routed to the nearest feasible vehicle based on great-circle Haversine distance from the vehicle's latest stop.
4. **Guarantees Complete Accountability:** Every submitted order is either allocated along an explicit route or reported with a transparent reason in the `unassignedOrders` list.
5. **Provides Immediate Visualization:** Each vehicle's route reports the delivery sequence, total payload, and formatted cumulative travel distance.

---

## 2. Technology Stack

- **Language:** Java 21 LTS (`<java.version>21</java.version>` bytecode target)
- **Framework:** Spring Boot 3.4.5
- **Data Access:** Spring Data JPA / Hibernate 6.6
- **Database:** MySQL 8.0 (with production connection pooling via HikariCP)
- **API Documentation:** Springdoc OpenAPI / Swagger UI 2.8.6
- **Validation:** Jakarta Bean Validation (`hibernate-validator`)
- **Testing:** JUnit 5 (Jupiter), Mockito, AssertJ, Spring Test (`MockMvc`, `TestRestTemplate`)
- **Build Tool:** Maven Wrapper (`mvnw` / `mvnw.cmd` with Apache Maven 3.10.0)

---

## 3. Architecture & Project Structure

The project strictly follows layered architecture principles, ensuring complete separation of concerns:

```
freightfox-dispatch-load-balancer/
├── pom.xml                                  # Maven dependencies, plugins & JVM configuration
├── mvnw / mvnw.cmd                          # Maven Wrapper scripts
├── postman/
│   └── FreightFox-Dispatch.postman_collection.json  # Importable Postman test collection
└── src/
    ├── main/
    │   ├── java/com/chandan/freightfox/
    │   │   ├── DispatchApplication.java    # Spring Boot entry point
    │   │   ├── config/
    │   │   │   └── OpenApiConfig.java       # Swagger / OpenAPI documentation configuration
    │   │   ├── controller/
    │   │   │   └── DispatchController.java  # REST controller (3 endpoints)
    │   │   ├── service/
    │   │   │   ├── OrderService.java        # Batch order persistence & retrieval
    │   │   │   ├── VehicleService.java      # Batch vehicle persistence & retrieval
    │   │   │   └── DispatchService.java     # Orchestrates DB retrieval, optimizer, DTO mapping
    │   │   ├── algorithm/
    │   │   │   └── DispatchOptimizer.java   # Pure algorithm: Priority-aware Greedy Nearest Neighbor
    │   │   ├── util/
    │   │   │   └── HaversineCalculator.java # Great-circle distance utility (R = 6,371 km)
    │   │   ├── entity/
    │   │   │   ├── DeliveryOrder.java       # JPA entity for delivery_orders table
    │   │   │   ├── Vehicle.java             # JPA entity for vehicles table
    │   │   │   └── OrderPriority.java       # Enum: HIGH, MEDIUM, LOW
    │   │   ├── repository/
    │   │   │   ├── OrderRepository.java     # Spring Data JPA repository for orders
    │   │   │   └── VehicleRepository.java   # Spring Data JPA repository for vehicles
    │   │   ├── dto/
    │   │   │   ├── OrderRequest.java        # Validated order input DTO
    │   │   │   ├── VehicleRequest.java      # Validated vehicle input DTO
    │   │   │   ├── OrderBatchRequest.java   # Batch order wrapper with duplicate ID check
    │   │   │   ├── VehicleBatchRequest.java # Batch vehicle wrapper with duplicate ID check
    │   │   │   ├── ApiResponse.java         # Standard success response payload
    │   │   │   ├── AssignedOrderDto.java    # Preserves all 6 order fields in plan route
    │   │   │   ├── UnassignedOrderDto.java  # Unassigned order representation with reason
    │   │   │   ├── VehicleDispatchEntry.java# Vehicle route entry (load, distance, stops)
    │   │   │   ├── DispatchPlanResponse.java# Top-level GET /plan response payload
    │   │   │   └── ErrorResponse.java       # Consistent error response payload
    │   │   └── exception/
    │   │       └── GlobalExceptionHandler.java # Centralized @RestControllerAdvice
    │   └── resources/
    │       └── application.properties       # Server, MySQL, JPA & Swagger settings
    └── test/
        └── java/com/chandan/freightfox/
            ├── DispatchApplicationTests.java         # Smoke test
            ├── util/HaversineCalculatorTest.java      # Distance formula & boundary tests
            ├── algorithm/DispatchOptimizerTest.java   # Algorithm logic & tie-breaking tests
            ├── algorithm/SampleDatasetVerificationTest.java # PDF 30-order sample dataset evaluation
            ├── dto/ValidationTest.java                # Bean validation & duplicate detection
            ├── service/ServiceBatchLogicTest.java     # Business pre-flight checks
            ├── service/PersistenceIntegrationTest.java# Live MySQL isolated persistence tests
            ├── controller/DispatchControllerTest.java # MockMvc REST endpoint unit tests
            └── controller/DispatchEndToEndIntegrationTest.java # Full HTTP lifecycle integration tests
```

---

## 4. Prerequisites

- **Java Development Kit (JDK):** Version 21 or higher (verified on JDK 25 LTS).
- **Database:** MySQL 8.0 running locally on port `3306` (or accessible remotely).
- **Build Tool:** Maven Wrapper is included (`mvnw.cmd` on Windows, `./mvnw` on Linux/macOS). No system-wide Maven installation is required.

---

## 5. Local Setup & Configuration

### Step 1: Create the Database
Ensure MySQL is running and create the database schema:

```sql
CREATE DATABASE IF NOT EXISTS FreightFox;
```

### Step 2: Configure Environment Variables
To keep credentials secure, passwords must **never** be hardcoded or committed to version control. Set environment variables in your terminal session before starting the application:

#### Windows PowerShell:
```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/FreightFox"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "your_mysql_password_here"
```

#### Linux / macOS Bash:
```bash
export DB_URL="jdbc:mysql://localhost:3306/FreightFox"
export DB_USERNAME="root"
export DB_PASSWORD="your_mysql_password_here"
```

*Note: If `DB_URL` is omitted, `application.properties` defaults to `jdbc:mysql://localhost:3306/FreightFox`.*

---

## 6. Building & Running the Application

### Run Tests
Execute the comprehensive test suite (68 automated tests):
```powershell
# Windows
.\mvnw.cmd test

# Linux/macOS
./mvnw test
```

### Package Executable JAR
Compile Java 21 sources and package the production JAR:
```powershell
# Windows
.\mvnw.cmd clean package -DskipTests

# Linux/macOS
./mvnw clean package -DskipTests
```

### Run the Application
You can run the application directly via Spring Boot Maven plugin or with Java:

```powershell
# Option A: Maven Wrapper
.\mvnw.cmd spring-boot:run

# Option B: Executable JAR
java -jar target\freightfox-dispatch-load-balancer-0.0.1-SNAPSHOT.jar
```

The service will start on `http://localhost:8080`.

---

## 7. Interactive Swagger / OpenAPI Documentation

When the application is running, full interactive API documentation is available at:

- **Swagger UI:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **OpenAPI JSON Spec:** [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

---

## 8. REST API Specifications

The service exposes the three required endpoints under `/api/dispatch`.

### 1. Input Fleet Details
Persists fleet vehicles into the database using transactional batch upsert.

- **Method:** `POST`
- **Path:** `/api/dispatch/vehicles`
- **Headers:** `Content-Type: application/json`

#### Request Body:
```json
{
  "vehicles": [
    {
      "vehicleId": "VEH001",
      "capacity": 100.0,
      "currentLatitude": 12.9716,
      "currentLongitude": 77.6413,
      "currentAddress": "Indiranagar, Bangalore, Karnataka, India"
    },
    {
      "vehicleId": "VEH002",
      "capacity": 150.0,
      "currentLatitude": 13.0674,
      "currentLongitude": 80.2376,
      "currentAddress": "T Nagar, Chennai, Tamil Nadu, India"
    }
  ]
}
```

#### Success Response (`HTTP 200 OK`):
```json
{
  "message": "Vehicle details accepted.",
  "status": "success"
}
```

---

### 2. Input Delivery Orders
Persists delivery orders into the database using transactional batch upsert.

- **Method:** `POST`
- **Path:** `/api/dispatch/orders`
- **Headers:** `Content-Type: application/json`

#### Request Body:
```json
{
  "orders": [
    {
      "orderId": "ORD001",
      "latitude": 12.9716,
      "longitude": 77.5946,
      "address": "MG Road, Bangalore, Karnataka, India",
      "packageWeight": 10.0,
      "priority": "HIGH"
    },
    {
      "orderId": "ORD002",
      "latitude": 13.0827,
      "longitude": 80.2707,
      "address": "Anna Salai, Chennai, Tamil Nadu, India",
      "packageWeight": 20.0,
      "priority": "MEDIUM"
    }
  ]
}
```

#### Success Response (`HTTP 200 OK`):
```json
{
  "message": "Delivery orders accepted.",
  "status": "success"
}
```

---

### 3. Retrieve Dispatch Plan
Generates an optimized load-balanced dispatch plan based on currently stored orders and fleet vehicles.

- **Method:** `GET`
- **Path:** `/api/dispatch/plan`
- **Headers:** `Accept: application/json`

#### Success Response (`HTTP 200 OK`):
```json
{
  "dispatchPlan": [
    {
      "vehicleId": "VEH001",
      "totalLoad": 10.0,
      "totalDistance": "5.06 km",
      "assignedOrders": [
        {
          "orderId": "ORD001",
          "latitude": 12.9716,
          "longitude": 77.5946,
          "address": "MG Road, Bangalore, Karnataka, India",
          "packageWeight": 10.0,
          "priority": "HIGH"
        }
      ]
    },
    {
      "vehicleId": "VEH002",
      "totalLoad": 20.0,
      "totalDistance": "3.97 km",
      "assignedOrders": [
        {
          "orderId": "ORD002",
          "latitude": 13.0827,
          "longitude": 80.2707,
          "address": "Anna Salai, Chennai, Tamil Nadu, India",
          "packageWeight": 20.0,
          "priority": "MEDIUM"
        }
      ]
    }
  ],
  "unassignedOrders": []
}
```

#### Empty State (`HTTP 200 OK`):
If no orders or vehicles have been input, the endpoint safely returns empty arrays:
```json
{
  "dispatchPlan": [],
  "unassignedOrders": []
}
```

---

## 9. Validation Rules & Error Handling

All requests are strictly validated prior to database interaction.

### Field Constraints:
- `orderId` / `vehicleId`: Required, non-blank string.
- `latitude` / `currentLatitude`: Required, numeric between `-90.0` and `90.0`.
- `longitude` / `currentLongitude`: Required, numeric between `-180.0` and `180.0`.
- `packageWeight` / `capacity`: Required, strictly positive numeric (`> 0.0`).
- `priority`: Required, case-insensitive match for `HIGH`, `MEDIUM`, or `LOW`.
- `address` / `currentAddress`: Required, non-blank string (up to 500 characters).
- **Batch Uniqueness:** Batches containing duplicate `orderId`s or duplicate `vehicleId`s within the same payload are immediately rejected before database persistence.

### Error Response Format (`HTTP 400 Bad Request`):
```json
{
  "status": "error",
  "message": "Validation failed",
  "errors": [
    "orders[0].latitude: latitude must be less than or equal to 90.0",
    "orders[0].packageWeight: packageWeight must be greater than 0",
    "orders[0].priority: priority must be HIGH, MEDIUM, or LOW"
  ]
}
```

### In-Batch Duplicate ID Error (`HTTP 400 Bad Request`):
```json
{
  "status": "error",
  "message": "Validation failed",
  "errors": [
    "uniqueOrderIds: Duplicate orderId found in incoming batch"
  ]
}
```

---

## 10. Dispatch Optimization Algorithm

### Strategy: Priority-aware Greedy Nearest Neighbor
The assignment requires assigning orders to suitable vehicles while respecting vehicle capacity, processing high-priority orders first, and minimizing travel distance. We employ the **Priority-aware Greedy Nearest Neighbor** heuristic:

```
                  ┌─────────────────────────────────┐
                  │    Retrieve Orders & Fleet      │
                  └────────────────┬────────────────┘
                                   │
                                   ▼
                  ┌─────────────────────────────────┐
                  │ Sort Orders:                    │
                  │ 1. Priority (HIGH > MED > LOW)  │
                  │ 2. Tie-break: orderId ASC       │
                  └────────────────┬────────────────┘
                                   │
                                   ▼
                         [ For Each Order ]
                                   │
                                   ▼
                       Is any vehicle feasible?
               (remainingCapacity >= packageWeight)
                                  / \
                            Yes  /   \  No
                                /     \
                               v       v
         ┌─────────────────────────┐  ┌─────────────────────────┐
         │ Find Feasible Vehicle   │  │ Append to               │
         │ with Min Haversine Dist │  │ unassignedOrders with    │
         │ (Tie: vehicleId ASC)    │  │ capacity reason         │
         └────────────┬────────────┘  └────────────┬────────────┘
                      │                            │
                      ▼                            │
         ┌─────────────────────────┐               │
         │ Assign order to vehicle:│               │
         │ - Append to route       │               │
         │ - Deduct remaining cap  │               │
         │ - Add package weight    │               │
         │ - Accumulate Haversine  │               │
         │ - Advance vehicle pos   │               │
         └────────────┬────────────┘               │
                      │                            │
                      └────────────┬───────────────┘
                                   │
                                   ▼
                         [ Next Order ... ]
                                   │
                                   ▼
                  ┌─────────────────────────────────┐
                  │ Format response:                │
                  │ - totalDistance: "X.XX km"      │
                  │ - Include unassigned vehicles   │
                  │ - Return DispatchPlanResponse   │
                  └─────────────────────────────────┘
```

### Key Algorithmic Invariants:
1. **Priority Processing Order:** `HIGH` orders are processed first, followed by `MEDIUM`, then `LOW`.
2. **Capacity Hard Feasibility:** An order is never assigned to a vehicle unless `vehicle.remainingCapacity >= order.packageWeight`.
3. **Local Greedy Distance Minimization:** Among feasible vehicles, the one closest to the order's drop point is selected.
4. **Deterministic Tie-Breaking:** If two vehicles are equidistant (within $\pm 10^{-7}\text{ km}$), the vehicle with the lexicographically smaller `vehicleId` is selected.
5. **Route Sequence Accumulation:** Travel distance starts at the vehicle's initial position and adds each leg sequentially: $\text{Distance} = \text{dist}(S, s_1) + \text{dist}(s_1, s_2) + \dots + \text{dist}(s_{n-1}, s_n)$.
6. **No Return-to-Depot:** In accordance with the assignment specification, vehicles do not return to their starting coordinates.
7. **Every Vehicle Included:** Vehicles receiving 0 orders are included in `dispatchPlan` with `totalLoad: 0.0`, `totalDistance: "0.00 km"`, and empty `assignedOrders`.

### Haversine Distance Formula:
$$d = 2 R \arcsin\left(\sqrt{\sin^2\left(\frac{\Delta \phi}{2}\right) + \cos(\phi_1)\cos(\phi_2)\sin^2\left(\frac{\Delta \lambda}{2}\right)}\right)$$
- $R = 6,371.0\text{ km}$ (Earth's mean spherical radius).
- Coordinates $\phi_1, \phi_2$ (latitudes) and $\lambda_1, \lambda_2$ (longitudes) in radians.
- Identical points return exactly `0.0 km`.

### Time & Space Complexity:
- **Time Complexity:** $O(O \log O + O \cdot V)$ where $O$ is the number of orders and $V$ is fleet size.
- **Space Complexity:** $O(O + V)$ in-memory state tracking.
- For 30 orders and 5 vehicles, the optimizer performs only 150 distance evaluations, executing in sub-millisecond time.

---

## 11. Sample Dataset Analysis (PDF Benchmark)

The assignment provides a benchmark dataset comprising **30 delivery orders** across the Delhi NCR region and **5 fleet vehicles**:

- **Total Order Weight:** $645.0\text{ kg}$
- **Total Fleet Capacity:** $500.0\text{ kg}$ ($\text{VEH001: 100, VEH002: 80, VEH003: 120, VEH004: 90, VEH005: 110}$)
- **Mathematical Invariant:** Because total demand exceeds total fleet capacity by $145\text{ kg}$, **at least $145.0\text{ kg}$ of orders must remain unassigned**.
- **Automated Verification:** Verified both via unit test (`SampleDatasetVerificationTest`) and over live HTTP (`DispatchEndToEndIntegrationTest.testFullSampleDatasetOverHttp`). All 30 orders are strictly accounted for, no vehicle exceeds its capacity, and total load conservation holds.

---

## 12. Postman Collection

An importable Postman collection is provided in:

[`postman/FreightFox-Dispatch.postman_collection.json`](postman/FreightFox-Dispatch.postman_collection.json)

### Included Requests:
1. `GET /api/dispatch/plan` — Retrieve current / initial dispatch plan.
2. `POST /api/dispatch/vehicles` — Submit sample fleet details (`VEH001`, `VEH002`).
3. `POST /api/dispatch/orders` — Submit sample delivery orders (`ORD001`, `ORD002`).
4. `GET /api/dispatch/plan` — Retrieve populated dispatch plan.
5. `POST /api/dispatch/orders - Invalid Fields (400)` — Verification of validation failure response.
6. `POST /api/dispatch/orders - Duplicate IDs (400)` — Verification of in-batch duplicate rejection.
7. `POST /api/dispatch/vehicles - Invalid Capacity (400)` — Verification of vehicle validation.
8. `POST /api/dispatch/vehicles - Full 5 Vehicles (PDF Dataset)` — Bulk import of all 5 PDF vehicles.
9. `POST /api/dispatch/orders - Full 30 Orders (PDF Dataset)` — Bulk import of all 30 PDF orders.
10. `GET /api/dispatch/plan - Full PDF Sample Plan` — Generates complete Delhi NCR dispatch plan.

#### Safe Local Data Reset:
To reset only test data created during manual testing without deleting unrelated database tables, execute in MySQL:
```sql
DELETE FROM delivery_orders WHERE order_id LIKE 'ORD%' OR order_id LIKE 'TEST_%';
DELETE FROM vehicles WHERE vehicle_id LIKE 'VEH%' OR vehicle_id LIKE 'TEST_%';
```

---

## 13. Test Suite Summary

The application is thoroughly verified by **68 automated tests** covering every layer:

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running DispatchEndToEndIntegrationTest .......... [2 passed]
[INFO] Running DispatchApplicationTests ................. [1 passed]
[INFO] Running DispatchControllerTest .................. [10 passed]
[INFO] Running DispatchOptimizerTest ................... [12 passed]
[INFO] Running SampleDatasetVerificationTest ........... [1 passed]
[INFO] Running ValidationTest .......................... [20 passed]
[INFO] Running PersistenceIntegrationTest .............. [6 passed]
[INFO] Running ServiceBatchLogicTest ................... [8 passed]
[INFO] Running HaversineCalculatorTest ................. [8 passed]
[INFO] 
[INFO] Results:
[INFO] Tests run: 68, Failures: 0, Errors: 0, Skipped: 0
[INFO] --------------------------------------------------------
[INFO] BUILD SUCCESS
```

---

## 14. Current Limitations & Assumptions

1. **Greedy Heuristic vs. Global Optimal:** The priority-aware nearest neighbor algorithm prioritizes urgent orders and executes fast ($O(O \cdot V)$), but does not solve NP-hard Capacitated Vehicle Routing Problem (CVRP) to global distance optimality.
2. **Static Distance Metric:** Calculations use great-circle Haversine distances. Road network topology, one-way streets, and live traffic congestion are not considered.
3. **Sequential Single Trip:** Each vehicle performs a single route from its current position until its capacity is exhausted, without multi-trip replenishment.
