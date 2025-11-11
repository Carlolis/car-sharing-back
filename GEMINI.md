# Project Overview

This is a Scala 3 backend application for a car-sharing service. It's built with a modern functional programming stack, including:

*   **ZIO:** For asynchronous and concurrent programming.
*   **Tapir:** For defining type-safe, OpenAPI-compliant HTTP endpoints.
*   **GelDB:** As the database for data persistence.
*   **Bleep:** As the build tool for managing dependencies and the build process.

The application follows a layered architecture, with clear separation between the `domain`, `repository`, `web`, and `remote-storage` layers.

# Building and Running

## Prerequisites

*   Java 17
*   Bleep
*   GelDB

## Setup

1.  **Install Bleep:** Follow the [official Bleep installation guide](https://bleep.build/docs/install).
2.  **Install GelDB:** Follow the [official GelDB installation guide](https://www.edgedb.com/docs/guides/deployment/installation).
3.  **Initialize GelDB:**
    ```bash
    edgedb project init
    ```
4.  **Configure the application:**
    *   Copy the example configuration file:
        ```bash
        cp domain/src/resources/application.conf.example domain/src/resources/application.conf
        ```
    *   Edit `domain/src/resources/application.conf` with your database credentials and other settings.

## Commands

*   **Compile:**
    ```bash
    bleep compile
    ```
*   **Run:**
    ```bash
    bleep run web
    ```
*   **Test:**
    ```bash
    bleep test
    ```

# Development Conventions

*   **Code Style:** The project uses `.scalafmt.conf` to enforce a consistent code style.
*   **Testing:** Tests are written with `zio-test` and can be found in the `*-test` modules (e.g., `domain-test`, `repository-test`).
*   **Dependencies:** Dependencies are managed in the `bleep.yaml` file.
*   **Database Migrations:** Database schema changes are managed with GelDB's migration system. Use `gel migration create` to create new migrations.
