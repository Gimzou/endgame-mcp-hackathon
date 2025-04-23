# Implementation Guide

## Architecture

The Bittensor Subnet Explorer is implemented using a Clean Architecture approach, which separates concerns into distinct layers and ensures the core business logic remains independent from external frameworks and implementation details.

### Technology Stack

- **Primary Language**: Kotlin - chosen for its strong type system, coroutine support for asynchronous operations, and excellent interoperability with Java libraries
- **Build System**: Gradle - for dependency management and build automation
- **JSON Processing**: kotlinx.serialization - for efficient serialization/deserialization of JSON messages
- **MCP SDK**: kotlin-sdk - implementation of the model context protocol

### Architectural Layers

The implementation follows the Clean Architecture pattern with four main layers:

#### 1. Application Layer: 

Orchestrates the flow of data between the domain and external interfaces, containing the Model Context Protocol server implementation.

```
application/
├── logging/            # Logging Utilities
├── server/             # MCP Server Implementation
└── AppConfig.kt        # Application configuration
 ```

#### 2. Data Layer: 

Implements repository interfaces defined in the domain layer and handles all external data interactions, including API clients for Taostats and GitHub.

```
 data/
 ├── cache/                         # Caching Utilities
 ├── model/                         # Data Models from the external APIs
 │   ├── github/                    
 │   └── taostats/                  
 ├── network/                       # Remote data access through the external APIs
 │   ├── github/                    
 │   ├── taostats/               
 │   └── HttpClientProvider.kt      # A generic HttpClient provider
 ├── repository/                    # Data access contract implementation 
 │   ├── github/                    
 │   └── taostats/                  
 ├── util/                     
 │   ├── ApiRequestHandler.kt       # A generic handler for API requests                   
 │   └── PaginationHandler.kt       # A generic handler for paginated responses 
 ```

#### 4. Dependency Injection Layer: 

Manages object creation and dependency resolution throughout the application, ensuring loose coupling between components.

```
di/
├── DependencyContainer.kt        # Loosely coupled module dependency declaration 
├── DependencyContainerImpl.kt    
 ```

#### 3. Domain Layer: 

Contains the core business logic and entity models that represent subnets and their metadata. This layer is completely independent of external frameworks and libraries.

```
domain/
├── entity                       # Data Model specific to the application
│   └── subnet/
├── repository/                  # Data access contract abstraction (only interfaces) 
│   ├── github/                    
│   └── taostats/   
├── service/                     # Business logic 
│   └── subnet/                     
```

>**Note**: All tests are implemented in the src/test folder, following the conventions in Java projects

## Components

### Domain Models
The core entities representing subnets and their metadata:

- `SubnetInfo`: Represents a Bittensor subnet with its netuid, name, description and GitHub repository URL
   ```kotlin
      data class SubnetInfo(
       val id: String,
       val name: String,
       val description: String,
       val gitHubRepo: String
      )
   ```

### Repository Pattern
The application uses the repository pattern to abstract data access:

- `SubnetRepository`: Interface defining methods to retrieve subnet information
   ```kotlin
   interface SubnetRepository {
       suspend fun getAllSubnetIdentities(perPage: Int = 50) : List<SubnetIdentity>
       suspend fun getSubnetIdentityById(id: Int) : SubnetIdentity
       suspend fun getSubnetIdentityByName(name: String) : SubnetIdentity
   }
   ```
- `GitHubRepoRepository`: Interface defining methods to retrieve GitHub content
   ```kotlin
   interface GitHubRepoRepository {
       suspend fun getGithubRepoReadMe(owner: String, repo: String): String
       suspend fun getGithubRepoReadMe(url: String): String
   }
   ```
- Repository implementations handle data fetching from external APIs and caching

### API Clients
Custom API clients for external data sources:

- `TaostatsApi`: Interface for interacting with the Taostats API
  ```kotlin
  interface TaostatsApi {
      suspend fun getSubnetIdentity(page: Int, perPage: Int): ApiResult<PaginatedResponse<SubnetIdentity>>
  }
  ```
- `GitHubApi`: Interface for retrieving repository documentation from GitHub
   ```kotlin
   interface GitHubApi {
      suspend fun getRepositoryReadMe(owner: String, repo: String) : ApiResult<GitHubRepositoryContent>
   }
   ```

### Caching Mechanism
A flexible in-memory caching system with configurable policies:

- `Cache`: General-purpose interface for caching operations
- `CachePolicy`: Defines naming conventions and expiration rules for cached items
- `InMemoryCache`: Implementation using concurrent hash maps for thread safety

### MCP Server Implementation
The core server components implementing the Model Context Protocol:

- `McpServer`: Interface defining the server contract
- `McpServerImpl`: Implementation focused on Bittensor subnet data

> **Note**: The implementation of the request handlers for `get_subnet_list` and `get_subnet_documentation` tools is currently defined in the `AppKt` file rather than within the McpServerImpl class. This makes the tool configuration immediately visible to developers exploring the codebase. As the application grows and tool configuration becomes more complex, this approach could be refactored to move configuration responsibilities into dedicated components within the application layer to maintain separation of concerns.  

### Transport Layer
Communication mechanisms for client-server interaction:

- `McpServerImpl.startWithStdio()`: Implements standard I/O communication using JSON-RPC 2.0
- Additional transport options for Server-Sent Events

> **Note**: Though the SSE transport layer is available, it has not been tested and is still in active development. It is recommended to set up the Bittensor Subnet Explorer with STDIO for real use cases.

### Dependency Injection
Custom dependency injection container:

- `DependencyContainer`: Interface for obtaining dependencies
- `DependencyContainerImpl`: Concrete implementation creating and managing component instances

## Setup

### Prerequisites
- JDK 17 or higher
- Gradle 8.0 or higher
- API keys for Taostats and GitHub APIs

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/Gimzou/endgame-mcp-hackathon.git
   cd endgame-mcp-hackathon
   ```

2. Build the project:
   ```bash
   ./gradlew build
   ```

3. Configure API credentials:
    - Create a `.env` file with the following content:
      ```
      TAOSTATS_API_KEY=your_taostats_api_key
      GITHUB_TOKEN=your_github_token
      ```

4. Generate the executable JAR:
   ```bash
   ./gradlew shadowJar
   ```

## Usage

### Running as an MCP Server

1. Start the server using standard I/O communication:
   ```bash
   java -jar build/libs/bittensor-subnet-explorer-0.0.1-all.jar
   ```

2. The server can now receive MCP requests from clients via stdin and send responses via stdout.

### Example Client Requests

#### List Available Subnets
```json
{
  "jsonrpc":"2.0",
  "id": 27,
  "method":"tools/call",
  "params": {
    "name":"get_subnet_list",
    "arguments":{}
  }
}
```

#### Get Subnet Documentation
```json
{
  "jsonrpc":"2.0",
  "id":54,
  "method":"tools/call",
  "params": {
    "name":"get_subnet_documentation",
    "arguments": {
      "netuid":1
    }
  }
}
```

### Integration with LLMs

To integrate with an LLM that supports the Model Context Protocol:

1. Configure the LLM  (e.g. Claude Desktop) to use the Bittensor Subnet Explorer as an MCP tool
  ```json
  {
    "Bittensor Subnet Explorer": {
       "command": "java",
       "args": [
           "-jar",
           "path/to/bittensor-subnet-explorer-0.0.1-all.jar"
       ],
       "env": {
         "TAOSTATS_API_KEY": "YOUR_TAOSTATS_KEY",
         "GITHUB_PERSONAL_ACCESS_TOKEN": "YOUR_GITHUB_ACCESS_TOKEN"
       } 
   }
  }
  ```

2. The LLM can then request subnet information using the provided functions.
    
   Examples of questions to ask :

   ```text
   [1] Which subnets do not require GPU for their miner nodes ?
   ```

   ```text
   [2] What are the main programming languages used to develop subnets ?
   ```

3. Results are returned to the LLM in structured JSON format for further processing
   ```text
   [1] Let me analyze the Bittensor subnet list to identify which subnets might not require GPUs for their miner nodes. 
   I'll need to examine the technical documentation of various subnets to determine this
   ...
   After analyzing the documentation of several Bittensor subnets, I can identify some subnets that don't require GPUs for mining. 
   Here's a list of subnets that appear to not require GPUs for their miner nodes:
    - Subnet 8: Proprietary Trading Network (PTN) ...
    - Subnet 10: Sturdy ...
   ...
   ```
   
   ```text
   [2] Based on my analysis of the Bittensor subnet documentation and repositories, 
   there are several main programming languages used to develop subnets :
    1. Python ...
    2. Rust ...
    3. JavaScript/TypeScript ...
    ... 
   ```

## Performance

### Caching Strategy
The caching mechanism significantly improves performance by reducing redundant API calls:

- Subnet list queries: Cached for 1 hour
- Subnet documentation: Cached for 1 hour

### Benchmarks

Performance measurements were conducted using the `BenchmarkingApiRequests` test class located in the data/repository test module. Results for typical operations:

| Operation         | First Request | Cached Request |
|-------------------|---------------|----------------|
| List Subnets      | ~550ms        | ~1ms           |
| Get Documentation | ~700ms        | ~1ms           |

## Testing

### Testing Approach
The implementation includes the following tested components:

#### 1. Data Layer Tests:

* **Cache**: 
  * `InMemoryCacheTest` implements comprehensive tests for `InMemoryCacheImpl`

* **Network**:
  * `GitHubAPIImplTest` covers tests for the GitHub API implementation
  * `TaostatsAPIImplTest` covers tests for the Taostats API implementation 

* **Repository**: 
  * `GitHubRepoRepositoryImplTest` covers tests for `GitHubRepoRepositoryImpl` 
  * `SubnetRepositoryImplTest` covers tests for `SubnetRepositoryImpl`

#### 2. Domain Layer Tests:

* **Service**: Test coverage for `SubnetService` class via `SubnetServiceTest`

### Mocking Strategy
External dependencies are mocked to ensure reliable testing:

- Mock HTTP client using the `ktor-client-mock` library for API testing
- `mockk` library utilized for other mocking needs

### Continuous Testing
Tests are automatically run on:

- Local development via Gradle test task

This testing approach ensures reliability and maintainability of the Bittensor Subnet Explorer while facilitating ongoing development and enhancement.