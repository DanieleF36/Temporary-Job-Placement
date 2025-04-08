# Project Documentation: Document Management API

## 1. Overview

This project implements a Document Management API using Kotlin and Spring Boot. It provides functionality for managing documents by allowing clients to upload files, retrieve document metadata and binary content, modify document data, and delete documents. The API supports pagination, sorting, and error handling mechanisms via custom exceptions and a global exception handler.

## 2. Architecture and Project Structure

The project follows a layered architecture to separate concerns. The main components are:

- **Entities:** Represent the domain model objects stored in a relational database.
- **DTOs (Data Transfer Objects):** Used to transfer data between layers, decoupling the internal database representation from the API response.
- **Repositories:** Provide persistence support through Spring Data JPA.
- **Service Layer:** Contains the business logic and orchestrates operations between repositories and controllers.
- **Controller:** Exposes the RESTful API endpoints.
- **Exception Handling:** Uses a global exception handler to map exceptions to meaningful HTTP responses.

## 3. Detailed Components

### 3.1 Entities

The domain model is composed of two primary entities:

- **DocumentMetadata**  
  Located in `DocumentMetadata.kt`, this entity stores:
    - `id`: Unique identifier.
    - `name`: The document’s name.
    - `size`: The document size (in bytes).
    - `contentType`: MIME type of the document.
    - `creationTimestamp`: The timestamp when the document was created.
    - One-to-one relation with **DocumentBinaryData** (see below).

- **DocumentBinaryData**  
  Located in `DocumentBinaryData.kt`, this entity holds:
    - `id`: Unique identifier (shared with `DocumentMetadata` via a one-to-one mapping).
    - `content`: The binary content of the document.
    - A reference back to the **DocumentMetadata** entity, ensuring bidirectional linkage.

The one-to-one association is managed by JPA annotations, with lazy loading and cascading options to ensure entity integrity.

### 3.2 Data Transfer Objects (DTOs)

DTOs are provided to decouple the REST API representation from the internal entity structure:

- **DocumentMetadataDTO**  
  Defined in `DocumentMetadataDTO.kt`, it includes:
    - `id` (nullable)
    - `name`
    - `size`
    - `contentType`
    - `creationTimestamp`

- **DocumentBinaryDataDTO**  
  Defined in `DocumentBinaryDataDTO.kt`, it includes:
    - `id` (auto-generated)
    - `content`: The raw binary content
    - `metadataDTO`: An embedded `DocumentMetadataDTO` that represents the associated metadata

Conversion helper functions (e.g., `toDto()`) are provided in both DTO files for converting entities to DTOs.

### 3.3 Repository Layer

The persistence layer consists of two interfaces extending Spring Data JPA repositories:

- **DocumentMetadataRepository**  
  Located in `DocumentMetadataRepository.kt`, it extends `JpaRepository<DocumentMetadata, Int>`. It also declares:
    - `findByName(name: String): DocumentMetadata?`  
      (used to avoid duplicate document names during creation).

- **DocumentBinaryRepository**  
  Located in `DocumentBinaryRepository.kt`, it extends `JpaRepository<DocumentBinaryData, Int>` to manage binary content.

### 3.4 Service Layer

The business logic is defined via the **DocumentService** interface and its implementation:

- **DocumentService Interface**  
  (See `DocumentService.kt`)  
  Defines the following operations:
    - `getAll(page, limit, sort)`: Retrieves a paginated list of document metadata.
    - `get(id)`: Retrieves metadata for a specific document.
    - `getData(id)`: Retrieves binary data for a specific document.
    - `create(name, contentType, binaryContent)`: Creates a new document.
    - `modify(metadataId, name, contentType, binaryContent)`: Modifies an existing document.
    - `delete(id)`: Deletes a document by its ID.

- **DocumentServiceImpl**  
  Located in `DocumentServiceImpl.kt`, it implements the logic outlined in the interface:
    - Implements input validations (e.g., ensuring non-negative IDs and non-blank names).
    - Uses a `PageRequest` and a custom enum `SortOption` (see `SortOption.kt`) to support pagination and sorting.
    - Throws custom exceptions such as `DocumentNameAlreadyExists` and `DocumentNotFoundException` when necessary.

### 3.5 Controller and Endpoints

The **DocumentController** exposes the REST endpoints for client interactions:

- **Endpoints** (See `DocumentController.kt`):
    - `GET /API/documents`: Retrieves a paginated list of document metadata.
    - `GET /API/documents/{metadataId}`: Retrieves document metadata by its ID.
    - `GET /API/documents/{metadataId}/data`: Retrieves the binary content for download.
    - `POST /API/documents`: Uploads a new document (accepts a multipart file).
    - `PUT /API/documents/{metadataId}`: Updates the metadata and, optionally, the file of an existing document.
    - `DELETE /API/documents/{metadataId}`: Deletes a document by its ID.

Each endpoint validates the input (such as non-negative IDs) and returns appropriate HTTP responses, including success status and error payloads when necessary.

### 3.6 Exception Handling

Global exception management is implemented via **GlobalExceptionHandler** (located in `GlobalExceptionHandler.kt`):

- **DocumentNotFoundException:**  
  Triggered when an operation refers to a non-existing document. The handler maps this to a `404 Not Found` response.

- **DocumentNameAlreadyExists:**  
  Used when attempting to create a document with a name that already exists. Mapped to a `409 Conflict` response.

- **IllegalArgumentException:**  
  Handles validation errors (e.g., negative page numbers or blank names) and returns a `500 Internal Server Error` with a corresponding error message.

The error responses are structured using the `ErrorResponse` data class, which includes a timestamp, HTTP status code, error string, message, and the request path.

### 3.7 Sorting Options

The **SortOption** enum (defined in `SortOption.kt`) provides the following options for sorting document metadata:
- `NAME_ASC` / `NAME_DESC`
- `SIZE_ASC` / `SIZE_DESC`
- `CREATION_DATE_ASC` / `CREATION_DATE_DESC`
- `CONTENT_TYPE_ASC` / `CONTENT_TYPE_DESC`

These options are used in the service layer to generate a dynamic JPA `Sort` object when fetching paginated results.

## 4. API Documentation and OpenAPI Specification

A comprehensive OpenAPI (Swagger) specification has been created for the API. This file details each endpoint (including request parameters, request bodies, responses, and error codes) and serves as a blueprint for client integrations or further documentation.

You can find the OpenAPI specification in a separate YAML file (for example, `openapi.yaml`). This specification includes:
- **Paths and Operations:** Definitions for document retrieval, upload, update, and deletion.
- **Models:** Definitions for `DocumentMetadataDTO`, `ErrorResponse`, and `SortOption`.
- **Responses:** Structured responses for HTTP status codes such as 200, 400, 404, 409, and 500.

Refer to the OpenAPI file provided separately for the complete YAML content.

## 5. Build, Run, and Testing

- **Build:**  
  The project uses Spring Boot. You can build the project with Maven or Gradle by running the standard build commands:

  ```bash
  mvn clean install
  # or for Gradle
  gradle build
  ```

- **Run:**  
  Once built, run the application using:

  ```bash
  mvn spring-boot:run
  # or the equivalent Gradle command
  ```

- **Testing the API:**  
  Use tools such as Postman or curl to test the endpoints. Additionally, tools like Swagger UI can render the OpenAPI specification for interactive API testing.