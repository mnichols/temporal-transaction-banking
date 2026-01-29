# Temporal Transaction Banking

Batch creation, transaction splitting, concurrent processing for ISO 20022 PAIN payment transaction banking using Temporal workflows.

## Architecture

This project implements payment transaction banking with the following bounded context:

- **initiations** - File processing, entitlement checks, batch creation based on payment criteria, fraud detection, and downstream transmission

## Tech Stack

- **Language**: Java 21 (OpenJDK)
- **Framework**: Spring Boot 3.5.9
- **Orchestration**: Temporal Workflow Engine (1.32.1)
- **Build**: Maven 3.9.9
- **Observability**: OpenTelemetry 1.40.0, Micrometer Prometheus 1.16.2
- **Testing**: JUnit 5.14.2, Mockito 5.21.0, AssertJ 3.27.6

## Project Structure

```
temporal-transaction-banking/
├── java/                          # Java implementation
│   ├── pom.xml                   # Root Maven parent POM
│   └── initiations/
│       ├── pom.xml               # Domain parent POM
│       ├── initiations-core/     # Workflows, Activities, domain logic (non-deployable)
│       ├── initiations-api/      # REST API (deployable Spring Boot JAR)
│       └── initiations-workers/  # Temporal Workers (deployable Spring Boot JAR)
├── docs/
│   ├── ARCHITECTURE.md           # Domain design and bounded contexts
│   ├── DEPLOYMENT.md             # Deployment instructions
│   ├── WORKFLOWS.md              # File and Batch workflow specifications
│   └── CONTRIBUTING.md           # Development guidelines
├── files/                        # Test data and utilities
│   ├── generate_pain_113.py      # PAIN.001.001.03 test file generator
│   ├── README.md                 # PAIN ISO 20022 documentation
│   ├── USAGE.md                  # File generator usage
│   └── generated/                # Generated test files (gitignored)
├── .github/
│   └── workflows/
│       └── build.yaml            # CI/CD pipeline
├── .tool-versions                # asdf version management
└── .gitignore
```

## Getting Started

### Prerequisites

- **Java 21** - Install via asdf: `asdf install` or manually
- **Maven 3.8.1+** - Included with Java 21 in most distributions
- **Temporal CLI** - `brew install temporal`

### Quick Start - Local Development

#### 1. Start Temporal Server

```bash
temporal server start-dev
```

This starts:
- **Temporal Server** on localhost:7233
- **Temporal UI** on http://localhost:8233

#### 2. Build the Java Project

```bash
cd java
mvn clean install -DskipTests
```

#### 3. Start the Workers Service (in a terminal)

```bash
cd java/initiations/initiations-workers
java -jar target/initiations-workers-1.0.0.jar
```

Workers listen on task queue `initiations`.

#### 4. Start the API Service (in another terminal)

```bash
cd java/initiations/initiations-api
java -jar target/initiations-api-1.0.0.jar
```

API listens on http://localhost:8080.

### Workflows

See [WORKFLOWS.md](docs/WORKFLOWS.md) for detailed File and Batch workflow specifications.

## Configuration

### Temporal

**Namespace**: `initiations`
**Task Queue**: `initiations`

Configuration files:
- `config/temporal.yaml` - Default (local) configuration
- `config/environments/dev.yaml` - Development environment
- `config/environments/prod.yaml` - Production environment

Override via environment variables:
```bash
TEMPORAL_NAMESPACE=initiations
TEMPORAL_TASK_QUEUE=initiations
TEMPORAL_ADDRESS=localhost:7233
```

### Application

**API Module** (`initiations-api`):
- Temporal Client only (does not start workers)
- Submits File Workflow tasks to queue

**Workers Module** (`initiations-workers`):
- Temporal Worker (processes File and Batch Workflows)
- Listens on `initiations` task queue

## Documentation

- **[ARCHITECTURE.md](docs/ARCHITECTURE.md)** - Domain design, bounded contexts, workflow orchestration
- **[DEPLOYMENT.md](docs/DEPLOYMENT.md)** - Production deployment instructions
- **[WORKFLOWS.md](docs/WORKFLOWS.md)** - Detailed File and Batch Workflow specifications
- **[files/README.md](files/README.md)** - ISO 20022 PAIN payment message documentation
- **[files/USAGE.md](files/USAGE.md)** - Test file generator usage

## Development

### Running Tests

```bash
cd java
mvn test
```

### Code Structure

**initiations-core**:
- `messages/` - Message classes and domain models
- `workflows/` - Temporal Workflows and Activities
- `config/` - Shared configuration

**initiations-api**:
- `controller/` - REST API controllers
- `config/` - Spring Boot and Temporal client configuration

**initiations-workers**:
- `config/` - Worker registration and startup

### Building for Production

```bash
# Build with optimizations
mvn clean install -P production
```

See [DEPLOYMENT.md](docs/DEPLOYMENT.md) for containerization and Kubernetes deployment.

## Temporal UI

Once Temporal is running, access the UI at:

- **Local**: http://localhost:8233
- **Namespace**: initiations
- **View Workflows** to monitor File and Batch processing

## Project Scope

This project is focused on turning **intent into authorized, risk-cleared payment obligations, ready for execution**.

Within the `initiations` bounded context, it handles:

✅ File receipt and validation
✅ Entitlement checks
✅ Payment splitting into batches
✅ Format transformation (PAIN-113 → PAIN-116)
✅ Batch creation and persistence
✅ Fraud detection setup
✅ Approval workflows

Out of scope:
❌ Fraud checks (to be implemented in separate context)
❌ Downstream transmission (to be routed from here)
❌ Payment settlement

## Contributing

See [CONTRIBUTING.md](docs/CONTRIBUTING.md) for development guidelines.
