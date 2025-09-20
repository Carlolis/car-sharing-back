# Car Sharing App Backend - Development Guidelines

## Build System & Configuration

### Build Tool: Bleep

This project uses **Bleep** (version 0.0.13) as the build tool with the following key characteristics:

- **Scala Version**: 3.6.4
- **JVM Version**: Temurin 1.17.0.14
- **Configuration File**: `bleep.yaml` at project root

### Project Structure

The project follows a clean architecture with 5 modules:

- **`domain`**: Core business logic, models, and service interfaces
- **`repository`**: EdgeDB persistence layer implementation
- **`remote-storage`**: WebDAV/NextCloud file storage implementation
- **`web`**: HTTP API layer with Tapir endpoints
- **`repository-test`** & **`remote-storage-test`**: Test modules

### Key Dependencies

- **ZIO**: 2.1.19 (Effect system and runtime)
- **Tapir**: 1.11.34 (HTTP API definitions)
- **EdgeDB Driver**: 0.4.0 (Database connectivity)
- **ZIO Test**: 2.1.19 (Testing framework)
- **Sardine**: 5.13 (WebDAV client)
- **Auth0 JWT**: 4.4.0 (Authentication)

### Build Commands

```bash
# Run tests for specific module
bleep test repository-test
bleep test remote-storage-test

# Build the project (use only when necessary, tests auto-build)
bleep compile web
```

## Testing Infrastructure

### Framework: ZIO Test

All tests extend `ZIOSpecDefault` and follow these patterns:

#### Test Structure

```scala
object YourTest extends ZIOSpecDefault {
  def spec: Spec[TestEnvironment & Scope, Any] =
    suite("Test Suite Name")(
      test("test description") {
        // test implementation using ZIO effects
        assertTrue(condition)
      }
    )
}
```

#### Key Testing Patterns

1. **Layer Provision**: Tests provide dependencies via `.provideShared(layers...)`
2. **Setup/Cleanup**: Use `@@ TestAspect.before(setup)` and `@@ TestAspect.after(cleanup)`
3. **Sequential Execution**: Use `@@ TestAspect.sequential` for tests that modify shared state
4. **Debug Logging**: Start debug messages with `[DEBUG_LOG]` prefix

#### Running Tests

```bash
# Run all tests in a module
bleep test repository-test

# Tests automatically compile dependencies
# No separate build step required
```

#### Test Configuration

- Test-specific configs in `src/resources/application.conf` within test modules
- Uses environment variables for external services (EdgeDB, NextCloud)
- Test directory separation (e.g., "test" vs production directories)

## Configuration & External Dependencies

### Required Environment Variables

```bash
# EdgeDB Connection
EDGEDB_DSN="edgedb://username:password@localhost:5656/dbname"

# NextCloud/WebDAV Storage
NEXTCLOUD_USERNAME="your_username"
NEXTCLOUD_PASSWORD="your_password" 
NEXTCLOUD_URL="https://your-nextcloud-instance.com"
NEXTCLOUD_DIRECTORY="production_directory"  # or "test" for tests

# Authentication
AUTH_SECRET_KEY="your-jwt-secret-key"
```

### Docker Setup

The project includes `docker-compose.yml` for containerized deployment:

- App runs on ports 8081 (API) and 8095
- EdgeDB runs in insecure dev mode
- Health check endpoint: `/api/health`
- Database schema mounted at `/dbschema`

### Database: EdgeDB

- **Schema**: `dbschema/default.gel` defines the data model
- **Migrations**: Located in `dbschema/migrations/`
- **AI Extension**: Project uses EdgeDB AI extension
- **Key Types**: `TripGel`, `PersonGel`, `InvoiceGel`, `WriterGel`, `ChatSessionGel`, `MessageGel`

#### Database Migrations

When there are database schema changes, apply migrations using:

```bash
# Apply migrations to test database
gel migration apply --dev-mode -I test
```

## Architecture & Code Patterns

### ZIO Service Pattern

Services follow this structure:

```scala
// Service trait in domain
trait YourService {
  def operation: Task[Result]
}

// Implementation class
class YourServiceLive(dependency: Dependency) extends YourService {
  override def operation: Task[Result] = // implementation
}

// Companion object with layer
object YourServiceLive {
  val layer: ZLayer[Dependency, Nothing, YourServiceLive] =
    ZLayer.fromFunction(YourServiceLive(_))
}
```

### Tapir Endpoint Pattern

API endpoints are defined in two parts:

1. **Endpoint Definition** (`*Endpoints.scala`):

```scala
val endpoint: Endpoint[Unit, Input, Error, Output, Any] = endpoint
  .post
  .in("api" / "path")
  .in(auth.bearer[String]())
  .in(jsonBody[RequestType])
  .out(jsonBody[ResponseType])
  .errorOut(statusCode and jsonBody[ErrorResponse])
```

2. **Endpoint Implementation** (`*EndpointsLive.scala`):

```scala
private val endpointImpl: ZServerEndpoint[Dependencies, Any] =
  Endpoints.endpoint.serverLogic { input =>
    // ZIO effect implementation
    effect.map(Right(_)).catchAll(err => ZIO.left(StatusCode.BadRequest, ErrorResponse(err.getMessage)))
  }
```

### Domain Modeling

- **Case Classes**: For data models with JSON serialization
- **Opaque Types**: For domain concepts (e.g., `DriverName`, `InvoiceId`)
- **Companion Objects**: Contain JSON encoders/decoders using `DeriveJsonEncoder/DeriveJsonDecoder`

### Error Handling & Logging

- Comprehensive logging using `ZIO.log*` methods
- Error compensation patterns (cleanup on failure)
- Structured error responses with `ErrorResponse` case class
- Use `.tapBoth()`, `.tapError()`, and `.tap()` for side effects

### File Upload Handling

- Multipart form data support in Tapir
- File sanitization with `replaceAll("""[\\/:*?"<>|]""", "")`
- Atomic operations with compensation logic
- Separation of file storage and metadata persistence

## Development Best Practices

### Code Style

- Use Scala 3 syntax features
- Prefer `for` comprehensions for sequential ZIO operations
- Use `ZIO.foreachPar` for parallel operations
- Extensive logging at info/debug/error levels
- Consistent naming: `*Service`, `*Repository`, `*Storage` for layers

### Testing Best Practices

- Test data objects in nested `TestData` object
- Test utilities in nested `TestUtils` object
- Always include cleanup in tests using `TestAspect.after`
- Use meaningful test descriptions in plain language
- Test both success and failure scenarios
- **Clean up temporary test files**: Remove any temporary test files created during development (e.g., `test_*.scala`,
  debug scripts) from the project root before submitting changes

### Configuration Management

- All external configuration via environment variables
- Use `${?VAR_NAME}` syntax in `application.conf`
- Separate test configurations with different defaults
- Document required environment variables

This project demonstrates a well-structured ZIO application with clean architecture, comprehensive testing, and proper
separation of concerns.

### EdgeDB/GelDB Development Patterns

#### Handling Optional Integer Fields

When working with optional integer fields in EdgeDB (like `distance: int16` in TripGel), follow this pattern:

1. **In the Gel model class**: Declare the field as `String | Long` to handle null values from EdgeDB:
   ```scala
   // Had to put String when field is null, otherwise it was not working
   distance: String | Long
   ```

2. **In the getter method**: Return the raw type without conversion:
   ```scala
   def getDistance: String | Long = distance
   ```

3. **In the conversion method**: Use `Option()` to handle null values and pattern match for type conversion:
   ```scala
   Option(gelObject.getField).map {
     case s: String => s.toInt
     case l: Long  => l.toInt
   }
   ```

This pattern ensures that null values from the database are properly converted to `None` in Scala domain models, rather
than defaulting to `Some(0)`.