# SemUmReal - Financial Management System

A modern financial management web application built with Spring Boot, following **Hexagonal Architecture** principles and **Domain-Driven Design** (DDD) practices.

## 🏗️ Architecture

This project implements **Hexagonal Architecture** (also known as Ports and Adapters), ensuring clear separation of concerns and high maintainability.

### Architecture Layers

```
┌─────────────────────────────────────────┐
│      Adapter/In (Web Layer)            │  ← REST Controllers + DTOs
│      Receives HTTP requests             │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│      Application (Service Layer)        │  ← Business logic orchestration
│      Use cases implementation            │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│      Domain (Core Business Logic)       │  ← Pure business rules
│      Entities + Business rules           │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│      Adapter/Out (Persistence Layer)    │  ← JPA Entities + Repositories
│      Database persistence                │
└─────────────────────────────────────────┘
```

### Project Structure

```
src/main/java/com/jmarcos/semumreal/
├── domain/                    # Domain Layer (Business Core)
│   ├── model/                # Domain entities (pure business logic)
│   ├── enums/                # Domain enumerations
│   └── exception/            # Domain exceptions
│
├── application/              # Application Layer
│   └── service/              # Use cases orchestration
│
└── adapter/                  # Adapters Layer
    ├── in/                   # Input Adapters (Drivers)
    │   └── web/              # REST API
    │       ├── controller/   # REST Controllers
    │       └── dto/          # Data Transfer Objects
    │           ├── request/  # Request DTOs
    │           └── response/ # Response DTOs
    │
    └── out/                  # Output Adapters (Driven)
        └── persistence/      # Database persistence
            ├── entity/       # JPA Entities (mapping layer)
            └── repository/   # Spring Data Repositories
```

## 🛠️ Technologies

- **Java 21**
- **Spring Boot 4.0.1**
- **Spring Data JPA** - Data persistence
- **Spring Security** - Authentication & Authorization
- **Spring Validation** - Input validation
- **PostgreSQL** - Relational database
- **Liquibase** - Database versioning and migration
- **Lombok** - Reducing boilerplate code
- **SpringDoc OpenAPI** - API documentation (Swagger)

## ✨ Key Features

### Domain Model with Automatic Validation
- **Rich Domain Models** with business rules embedded
- **Automatic validation** on object creation and modification
- **Factory Method Pattern** for safe object instantiation
- **Immutable objects** where appropriate

### Example: User Domain Model

```java
// Factory method for safe creation
User user = User.create("John Doe", "john@example.com", "securePassword");

// Automatic validation on setters
user.setEmail("invalid-email"); // Throws IllegalArgumentException

// Business methods
if (user.isAdmin()) {
    // Admin-specific logic
}
```

### Database Versioning
- **Liquibase** for database schema management
- Version-controlled database changes
- Easy rollback capabilities
- Team collaboration friendly

## 🚀 Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.6+
- PostgreSQL 12+

### Configuration

1. **Clone the repository**
```bash
git clone <repository-url>
cd semumreal
```

2. **Configure local secrets and database**

Copy the example file and edit your local values (never commit `application-local.properties`):

```bash
cp src/main/resources/application-local.example.properties src/main/resources/application-local.properties
```

Generate a JWT secret:

```bash
openssl rand -base64 32
```

Paste the result into `app.jwt.secret` in `application-local.properties`, along with your PostgreSQL credentials.

Alternatively, set the environment variable before running:

```bash
export APP_JWT_SECRET="your-base64-secret"
```

3. **Run the application**

```bash
mvn spring-boot:run
```

Or with local profile:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### API Documentation

Once the application is running, access Swagger UI at:
```
http://localhost:8080/swagger-ui.html
```

## 📋 Design Patterns & Principles

### Implemented Patterns

- **Hexagonal Architecture** - Clean separation of concerns
- **Factory Method** - Safe object creation
- **Repository Pattern** - Data access abstraction
- **DTO Pattern** - Data transfer objects

### SOLID Principles

- **Single Responsibility** - Each class has one reason to change
- **Open/Closed** - Open for extension, closed for modification
- **Liskov Substitution** - Proper inheritance usage
- **Interface Segregation** - Focused interfaces
- **Dependency Inversion** - Depend on abstractions

### Domain-Driven Design

- **Rich Domain Models** - Business logic in domain entities
- **Value Objects** - Immutable domain concepts
- **Aggregates** - Consistency boundaries
- **Domain Events** - (To be implemented)

## 🧪 Testing

Run tests with:
```bash
mvn test
```

Test profile uses H2 in-memory database for fast execution.

## 📝 Database Migrations

Database changes are managed through Liquibase changelogs:

```
src/main/resources/db/changelog/
├── db.changelog-master.yaml
└── changes/
    ├── 001-create-users-table.yaml
    └── ...
```

## 🔒 Security

- Spring Security integration
- Role-based access control (RBAC)
- Secure password handling
- JWT secret via environment variable (`APP_JWT_SECRET`) or local profile — never committed
- Protected domain model operations

## 📦 Build

```bash
mvn clean install
```

## 🤝 Contributing

This is a personal project, but suggestions and feedback are welcome!

## 📄 License

[Your License Here]

## 👤 Author

**José Marcos**
---

**Note**: SemUmReal is built as a learning exercise and portfolio demonstration, showcasing modern Java development practices and clean architecture principles.