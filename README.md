## Project Name

This project is a Scala 3 application using EdgeDB for database management and Bleep for build automation.

### Prerequisites

- Scala 3
- EdgeDB
- Bleep

### Setup

#### Install EdgeDB

Follow the [EdgeDB installation guide](https://www.edgedb.com/docs/guides/deployment/installation) to install EdgeDB on your system.

#### Install Bleep

Follow the [Bleep installation guide](https://bleep.build/docs/install) to install Bleep on your system.

### Configuration

1. **EdgeDB Configuration:**

   Ensure that EdgeDB is running and properly configured. You can start EdgeDB with:

   ```sh
   edgedb server start
   ```

   Create a new EdgeDB project:

   ```sh
   edgedb project init
   ```

2. **Bleep Configuration:**

   Initialize Bleep in your project directory:

   ```sh
   bleep new
   ```

### Usage

#### Compile the Project

To compile the project, run:

```sh
bleep compile
```

#### Run the Project

To run the project, use:

```sh
bleep run
```

#### Test the Project

To execute tests, run:

```sh
bleep test
```

### Project Structure

- `src/main/scala`: Main source code
- `src/test/scala`: Test source code
- `bleep.yaml`: Bleep build configuration file

### EdgeDB Integration

This project uses EdgeDB for database operations. The `TripServiceEdgeDb` class in `TripServiceEdgeDb.scala` handles database interactions.

### Additional Resources

- [Scala 3 Documentation](https://docs.scala-lang.org/scala3/)
- [EdgeDB Documentation](https://www.edgedb.com/docs)
- [Bleep Documentation](https://bleep.build/docs/)

### License

This project is licensed under the MIT License. See the `LICENSE` file for details.