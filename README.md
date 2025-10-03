# kraQus

## Overview

kraQus is a Quarkus-based Java application designed for financial trading analysis and strategy simulation.

The system utilizes historical market data, computes various technical indicators, applies machine learning models to generate trade signals, and simulates trading strategies to evaluate profit and loss.

## Features

- Calculation of popular trading indicators such as Moving Averages, RSI, MACD, Bollinger Bands, and more.
- Integration with MongoDB for storage and retrieval of market data, learning signals, and profit simulations.
- Use of WEKA machine learning models for trade signal prediction.
- Implementation of multiple profit calculation and backtesting strategies.
- Configuration managed through Quarkus and support for both JVM and native builds.

## Getting Started

### Prerequisites

- Java 11 or later
- Maven 3.x
- MongoDB instance

### Running the Application

1. Clone the repository:

```
git clone https://github.com/ragasits/kraQus.git
cd kraQus
```

2. Configure database connection and settings in the application configuration or via `MyConfig`.

3. Run in development mode:

```
./mvnw quarkus:dev
```

The dev UI is available at `http://localhost:8080/q/dev/`.

### Packaging and Running

To package:

```
./mvnw package
```

To run the packaged application:

```
java -jar target/quarkus-app/quarkus-run.jar
```

To build a native executable (optional):

```
./mvnw package -Dnative
```

Run the native executable:

```
./target/kraQus-1.0.0-SNAPSHOT-runner
```

## Project Structure

- `calc/`: Technical indicator calculation services and DTOs.
- `profit/`: Profit simulation and trading strategy services.
- `learn/`: Machine learning trade signal management.
- `model/`: Machine learning model metadata and execution.
- `MyConfig.java`: Configuration and MongoDB connection management.

## Contributing

Feel free to contribute by opening issues or pull requests.
