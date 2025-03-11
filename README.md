# Zero Downtime Tester

A Spring Boot CLI application for testing REST, SOAP, and website services for zero downtime by continuously sending requests at specified intervals.

## Features

- Continuous testing of REST, SOAP, and website services
- Customizable test intervals
- Detailed statistics tracking without scrolling output
- HTML report generation with charts
- Command-line interface with helpful prompts and guidance
- Support for multiple simultaneous tests
- Environment-specific service configuration (pre/pro)
- Simplified service-specific commands

## Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.6 or higher

### Installation

1. Clone the repository
2. Build the application:
   ```bash
   mvn clean package
   ```
3. Run the application:
   ```bash
   java -jar target/zero-downtime-0.0.1-SNAPSHOT.jar
   ```

### User Interface

When you start the application, you'll see a welcome banner with a list of available commands. The application provides a command-line interface with a custom prompt:

```text
 ______                ______                      _   _                 _____         _           
|___  /               |  _  \                    | | (_)               |_   _|       | |          
   / / ___ _ __ ___   | | | |_____      ___ __ | |_ _ _ __ ___   ___   | | ___  ___| |_ ___ _ __ 
  / / / _ \ '__/ _ \  | | | / _ \ \ /\ / / '_ \| __| | '_ ` _ \ / _ \  | |/ _ \/ __| __/ _ \ '__|
 / /_|  __/ | | (_) | | |/ / (_) \ V  V /| | | | |_| | | | | | |  __/  | |  __/\__ \ ||  __/ |   
/_____\___|_|  \___/  |___/ \___/ \_/\_/ |_| |_|\__|_|_| |_| |_|\___| \_/\___||___/\__\___|_|   

Zero Downtime Tester v1.0.0

Available commands:
  list                       - List available services
  start psis [--env <environment>] [--url <url>] [--interval-ms <interval>]
                             - Start testing PSIS service
  start iarxiu [--env <environment>] [--url <url>] [--interval-ms <interval>]
                             - Start testing iArxiu service
  start signador [--env <environment>] [--url <url>] [--interval-ms <interval>]
                             - Start testing Signador service
  start valid [--env <environment>] [--url <url>] [--interval-ms <interval>]
                             - Start testing VALID service
  start enotum [--env <environment>] [--url <url>] [--interval-ms <interval>]
                             - Start testing eNotum service
  start <service-number> [--env <environment>] [--url <url>] [--interval-ms <interval>]
                             - Start testing a service (legacy)
  status                     - Show status of running tests
  stop <service-name>        - Stop testing a service
  stop-all                   - Stop all running tests
  help                       - Display help about available commands
  quit                       - Exit the application

zero-downtime:> 
```

You can type `help` at any time to see detailed information about each command.

## Commands

### List Available Services

```bash
list
```

Lists all available services for testing and any currently active tests.

### Start Testing a Service

#### Simplified Service-Specific Commands

```bash
start <service-name> [--env <environment>] [--url <url>] [--interval-ms <interval>]
```

Starts continuous testing of a specific service.

- `<service-name>`: The name of the service (psis, iarxiu, signador, valid, enotum)
- `--env <environment>`: The environment to use (pre or pro, optional, default: pre)
- `--url <url>`: The URL of the service (optional, overrides environment-specific URL)
- `--interval-ms <interval>`: The interval between requests in milliseconds (optional, default: 1000)

Example:

```bash
start psis --env pro --interval-ms 500
```

#### Legacy Command (Backward Compatibility)

```bash
start <service-number> [--env <environment>] [--url <url>] [--interval-ms <interval>]
```

Starts continuous testing of a service using the service number.

- `<service-number>`: The number of the service from the list (required)
- `--env <environment>`: The environment to use (pre or pro, optional, default: pre)
- `--url <url>`: The URL of the service (optional, overrides environment-specific URL)
- `--interval-ms <interval>`: The interval between requests in milliseconds (optional, default: 1000)

Example:

```bash
start 1 --env pre --interval-ms 500
```

### Show Test Status

```bash
status
```

Shows the status of all running tests, including:
- Elapsed time
- Total requests sent
- Success/failure counts
- Success rate
- Response time statistics (min/avg/max)
- Status code distribution

This command provides a static view of the test progress without scrolling messages.

### Stop Testing a Service

```bash
stop <service-name>
```

Stops testing a specific service and generates a report.

- `<service-name>`: The name of the service to stop (must match one of the active test names)

Example:

```bash
stop PSIS
```

### Stop All Tests

```bash
stop-all
```

Stops all running tests and generates reports for each.

## Environment Configuration

The application supports different environments (pre and pro) for service URLs. You can configure the URLs for each service in each environment in the `application.properties` file:

```properties
# Default environment
service.default.environment=pre

# PRE environment service URLs
service.pre.psis.url=https://pre.example.com/psis/api
service.pre.iarxiu.url=https://pre.example.com/iarxiu/soap
service.pre.signador.url=https://pre.example.com/signador/api
service.pre.valid.url=https://pre.example.com/valid/api
service.pre.enotum.url=https://pre.example.com/enotum/api

# PRO environment service URLs
service.pro.psis.url=https://pro.example.com/psis/api
service.pro.iarxiu.url=https://pro.example.com/iarxiu/soap
service.pro.signador.url=https://pro.example.com/signador/api
service.pro.valid.url=https://pro.example.com/valid/api
service.pro.enotum.url=https://pro.example.com/enotum/api
```

You can specify which environment to use when starting a test with the `--env` parameter. If not specified, the default environment will be used.

## Reports

HTML reports are generated in the `reports` directory when a test is stopped. Each report includes:

- Test summary (service name, type, duration, etc.)
- Success rate and response time statistics
- Response time chart
- Status distribution chart
- Detailed list of all test results

## Extending the Application

### Adding New Service Types

To add a new service type:

1. Create a new implementation of `ServiceTester` interface
2. Add the new service type to the `ServiceType` enum
3. Update the `ServiceTesterFactory` to create instances of the new service type
4. Add the new service to the list in `ZeroDowntimeCommands`
5. Update the `ServiceConfig` class to include URLs for the new service

### Customizing Reports

The HTML report template is located at `src/main/resources/templates/report.html`. You can modify this template to customize the report format and content.

## License

This project is licensed under the MIT License - see the LICENSE file for details.
