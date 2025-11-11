# iRUC: Reducing Inter-Microservice Data Communication in Data-Intensive Systems via Unified Computation

## Overview

This repository contains the implementation of iRUC, a method for reducing inter-microservice data communication in data-intensive systems through unified computation.

## Project Structure

```
.
├── agent-workflow/              # LLM agent workflow component
├── execute-engine/              # Execution engine component
├── gateway/                     # Data gateway component
└── org-services-example-train/  # Example original microservice system (6 services)
```

### Components

- **agent-workflow**: Corresponds to the LLM agent workflow section in the paper
- **execute-engine**: Corresponds to the execution engine section in the paper
- **gateway**: Corresponds to the data gateway section in the paper
- **org-services-example-train**: A sample original microservice system consisting of 6 services

## How to Transform Original Services to iRUC Method

Follow these steps to convert your original microservices to use the iRUC method:

### Step 1: Generate GraphQL+ Statements
Run the `agent-workflow` component and place your original service code into it to obtain the transformed GraphQL+ statements.

### Step 2: Configure Data Gateway
Configure the data gateway for these services according to the instructions in the `gateway` directory.

### Step 3: Combine GraphQL+ Statements
Run `statements-combine` in the `execute-engine` directory to combine the GraphQL+ statements from individual services into a complete chain of GraphQL+ statements.

### Step 4: Load and Execute
Run the `engine` in the `execute-engine` directory to load the chain of GraphQL+ statements generated in the previous step.

At this point, you can send requests to invoke the engine, and you should get the same results as calling the original services.
