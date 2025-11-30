# iRUC: Reducing Inter-Microservice Data Communication in Data-Intensive Systems via Unified Computation

## Overview

iRUC is a method for reducing inter-microservice data communication in data-intensive systems through unified computation. This repository provides the complete implementation and toolchain for transforming traditional microservice systems into optimized iRUC-based systems.

## Project Structure

```
.
├── agent-workflow/              # LLM agent workflow for code transformation
├── execute-engine/            
│   ├── statements-combine/      # GraphQL+ statement composition
│   └── engine/                  # Execution engine
├── gateway/                     # Data gateway component
└── org-services-example-train/  # Example microservice system (6 services)
```

### Component Mapping to Paper

- **agent-workflow**: LLM agent workflow (Section X)
- **execute-engine**: Execution engine (Section Y)
- **gateway**: Data gateway (Section Z)
- **org-services-example-train**: Baseline microservice example

## Quick Start Example

This tutorial demonstrates transforming a 6-service microservice chain into an iRUC-based system.

### Example Service Chain

```
travel2 ──➤ food ──➤ route ──➤ stationfood ──➤ fooddelivery
             │
             └──➤ trainfood
```

---

## Part 1: Deploy Original Services (Baseline)

First, you need to deploy the original service chain as a baseline for comparison. **Refer to `org-services-example-train/README.md` for complete deployment instructions.**

### Step 1: Configure Database Connections

For each service in `org-services-example-train/`, update the configuration file:

```yaml
server:
  port: 8080

spring:
  application:
    name: travel2-service
  datasource:
    url: jdbc:postgresql://localhost:5432/your_database_name
    username: postgres
    password: your_password
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

### Step 2: Build and Run Services

For each service:

```bash
# Navigate to service directory
cd org-services-example-train/travel2-service

# Build JAR package
mvn clean package

# Run the service
java -jar target/travel2-service-1.0.0.jar
```

Repeat for all 6 services: `travel2`, `food`, `route`, `trainfood`, `stationfood`, `fooddelivery`.

### Step 3: Verify Baseline

Test the original service chain:

```bash
curl http://localhost:8080/travel/retrieve_food/123456
```

---

## Part 2: Transform Services with iRUC

### Step 1: Setup Agent Workflow

**Refer to `agent-workflow/README.md` for detailed configuration and usage instructions.**

Navigate to `agent-workflow/` and configure LLM API credentials:

```bash
cd agent-workflow
```

Edit the `.env` file with your LLM API keys:

```env
# API Configuration
CLAUDE_API_KEY=your_claude_api_key
GEMINI_API_KEY=your_gemini_api_key
OPENAI_API_KEY=your_openai_api_key

# API Endpoints
CLAUDE_API_URL=https://api.anthropic.com
GEMINI_API_URL=https://generativelanguage.googleapis.com
OPENAI_API_URL=https://api.openai.com
```

Test API connectivity:

```bash
node test-api.js
```

Start the agent workflow:

```bash
npm install
npm start
```

### Step 2: Generate GraphQL+ Statements

For each service, submit the complete source code to the agent workflow.

**Example for travel2-service:**

Submit all code files from `org-services-example-train/travel2-service/`:
- `Travel2Application.java`
- `controller/` folder
- `service/` folder
- `repository/` folder
- `domain/` folder
- `util/` folder

**Agent Output (2 parts):**

1. **GraphQL+ Statement** (save as `travel2.gqlp`):

```graphql
service travel2 {
    new trip123 = gql query {getTripByTripIdquery};
  
    output travel2.trip = trip123;
  
    new trainTypeName321 = train1.jar/api1(travel2.trip);
  
    ...
  
    new orderResponse = gql query {getFoodOrderByOrderIdquery};
  
    output food.trip = travel2.trip;
}
```

2. **Plugin Packaging Instructions**:

The agent will provide complete project structure and packaging instructions similar to:

```
plugin-service/
├── pom.xml
├── src/
│   └── main/
│       ├── java/
│       │   └── com/example/plugin/
│       │       ├── entity/
│       │       ├── service/
│       │       └── handler/
│       └── resources/
│           └── application.yml
```

Follow the provided instructions to build the plugin JAR:

```bash
mvn clean package
```

This generates `travel2.jar`.

**Repeat this process for all 6 services** to generate:
- GraphQL+ files: `travel2.gqlp`, `food.gqlp`, `route.gqlp`, `trainfood.gqlp`, `stationfood.gqlp`, `fooddelivery.gqlp`
- Plugin JARs: `travel2.jar`, `food.jar`, `route.jar`, `trainfood.jar`, `stationfood.jar`, `fooddelivery.jar`

### Step 3: Combine GraphQL+ Statements

**Refer to `execute-engine/statements-combine/README.md` for detailed instructions on statement composition.**

Navigate to the statement composition service:

```bash
cd execute-engine/statements-combine
```

Start the service (the simplest way):

```bash
mvn spring-boot:run
```

Upload all GraphQL+ statements generated in Step 2:

```bash
curl -X POST http://localhost:8080/api/v1/gqlp/compose/upload \
  -F "files=@travel2.gqlp" \
  -F "files=@food.gqlp" \
  -F "files=@route.gqlp" \
  -F "files=@trainfood.gqlp" \
  -F "files=@stationfood.gqlp" \
  -F "files=@fooddelivery.gqlp" \
  -F "entryServiceName=travel2"
```

**Output**: The service will return a combined statement. Save it as `train.gqlp`:

```graphql
new trip123 = gql query {getTripByTripIdquery};

output travel2.trip = trip123;

...

new stations789 = stations1.jar/api2(deliveryOrder123);

output fooddelivery.deliveryOrder = deliveryOrder123;
```

### Step 4: Configure Data Gateway

**Refer to `gateway/README.md` for complete gateway configuration instructions.**

Navigate to gateway:

```bash
cd gateway
```

Update the database configuration in `train-services/db.js`:

```javascript
const dbConfig = {
  host: 'localhost',
  port: 5432,
  database: 'your_database_name',
  user: 'postgres',
  password: 'your_password'
};
```

Start the gateway:

```bash
npm install
npm start
```

> **Note**: For creating new gateway configurations for different service chains, refer to the AI agent prompts and detailed instructions in `gateway/build-new/`. Simply copy your code to the designated positions in the prompt template and follow the agent's output to modify the gateway accordingly.

### Step 5: Deploy Execution Engine

**Refer to `execute-engine/engine/README.md` for detailed engine deployment and usage instructions.**

Navigate to the engine:

```bash
cd execute-engine/engine
```

Start the engine (the simplest way):

```bash
mvn spring-boot:run
```

Upload all plugin JAR files generated in Step 2:

```bash
curl -X POST http://localhost:8080/update \
  -F "jar=@travel2.jar" \
  -F "jar=@food.jar" \
  -F "jar=@route.jar" \
  -F "jar=@trainfood.jar" \
  -F "jar=@stationfood.jar" \
  -F "jar=@fooddelivery.jar"
```

Upload the combined GraphQL+ statement generated in Step 3:

```bash
curl -X POST http://localhost:8080/update \
  -F "gqlp=@train.gqlp"
```

---

## Part 3: Verify Transformation

Now all queries to the original service chain can be executed on the transformed GraphQL+ engine.

### Original Service Request

```bash
GET http://localhost:8080/travel/retrieve_food/123456
```

### iRUC Engine Request

```bash
curl -X POST http://localhost:8080/execute \
  -H "Content-Type: application/json" \
  -d '{
    "scriptname": "train",
    "init": {
      "tripId": "123456"
    }
  }'
```

**Expected Result**: Using the same `tripId` parameter, both requests should return identical results. This completes the transformation from traditional microservice deployment to the iRUC method.

---


