## Project Name

This project is a Scala 3 application using GelDB for database management and Bleep for build automation.

### Prerequisites

- Scala 3
- GelDB
- Bleep

### Setup

#### Install GelDB

Follow the [GelDB installation guide](https://wwwgeldatacom/docs/guides/deployment/installation) to install GelDB on your system.

#### Install Bleep

Follow the [Bleep installation guide](https://bleep.build/docs/install) to install Bleep on your system.

### Configuration

1. **GelDB Configuration:**

   Ensure that GelDB is running and properly configured. You can start GelDB with:

   ```sh
   edgedb server start
   ```

   Create a new GelDB project:

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

### GelDB Integration

This project uses GelDB for database operations. The `TripServiceGel` class in `TripServiceGel.scala` handles database interactions.

### Additional Resources

- [Scala 3 Documentation](https://docs.scala-lang.org/scala3/)
- [GelDB Documentation](https://wwwgeldatacom/docs)
- [Bleep Documentation](https://bleep.build/docs/)

### License

This project is licensed under the MIT License. See the `LICENSE` file for details.