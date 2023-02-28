
# Tachyon CFML 
This is a modern CFML server implementation written in Kotlin. 
It supports Java 19 virtual threads for massive concurrency, yaml configurations to enable different profiles for development and production settings, built-in metrics, REST architecture, and dependency injection.

### Features

* Java 19 Virtual Threads: the server supports the use of virtual threads for massive concurrency and efficient resource utilization.
* YAML Configuration: the server can be configured using YAML files, which makes it easy to switch between different profiles for development and production settings.
* Built-in Metrics: the server comes with built-in metrics that can be used to monitor the server's performance and identify any potential issues.
* REST Architecture: the server uses a REST architecture, which makes it easy to build APIs and web services.
* Dependency Injection: the server supports dependency injection, which makes it easy to manage dependencies and promote code reusability.

### Getting Started
To get started with the CFML server, follow these steps:

* Clone the repository.
* Build the server using the provided build script.
* Configure the server using YAML files.
* Start the server.

###Configuration
The server can be configured using YAML files. There are separate configuration files for development and production settings. To switch between the two, simply update the value of an environment variable.

### Metrics
The server comes with built-in metrics that can be used to monitor the server's performance. Metrics are exposed via the /api/metrics endpoint and can be consumed by external monitoring tools.

### REST API
The server uses a REST architecture, which makes it easy to build APIs and web services. The API is designed to be easy to use and intuitive.

### Dependency Injection
The server supports dependency injection, which makes it easy to manage dependencies and promote code reusability. The server uses the Dagger 2 framework for dependency injection.

### Contributing
Contributions are welcome! If you would like to contribute to the project, please submit a pull request. Please make sure to include tests with your changes.

### License
This project is licensed under the MIT License - see the LICENSE file for details.

