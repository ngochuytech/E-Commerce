# Brand API Documentation

## Overview
This document describes the REST API endpoints for managing Brand entities in the E-Commerce TechShop application.

Base URL: `${api.prefix}/brands`

## Endpoints

### 1. Get All Brands (Paginated)
- **Method**: `GET`
- **URL**: `/brands`
- **Parameters**:
  - `page` (optional, default: 0) - Page number
  - `size` (optional, default: 10) - Page size
  - `sortBy` (optional, default: "name") - Sort field
  - `sortDirection` (optional, default: "asc") - Sort direction (asc/desc)
- **Response**: List of BrandDTO objects
- **Example**: `GET /brands?page=0&size=10&sortBy=name&sortDirection=asc`

### 2. Get All Brands (No Pagination)
- **Method**: `GET`
- **URL**: `/brands/all`
- **Response**: List of all BrandDTO objects
- **Example**: `GET /brands/all`

### 3. Get Brand by ID
- **Method**: `GET`
- **URL**: `/brands/{id}`
- **Parameters**:
  - `id` (path) - Brand ID
- **Response**: BrandDTO object
- **Example**: `GET /brands/60f7b1b9e4b0c8a3f8f3a1b2`

### 4. Get Brand by Name
- **Method**: `GET`
- **URL**: `/brands/name/{name}`
- **Parameters**:
  - `name` (path) - Brand name
- **Response**: BrandDTO object
- **Example**: `GET /brands/name/Apple`

### 5. Create Brand
- **Method**: `POST`
- **URL**: `/brands`
- **Request Body**: BrandDTO object
- **Response**: Created BrandDTO object
- **Validation**:
  - `name`: Required, 1-100 characters
- **Example Request**:
```json
{
  "name": "Apple"
}
```

### 6. Update Brand
- **Method**: `PUT`
- **URL**: `/brands/{id}`
- **Parameters**:
  - `id` (path) - Brand ID
- **Request Body**: BrandDTO object
- **Response**: Updated BrandDTO object
- **Validation**:
  - `name`: Required, 1-100 characters
- **Example Request**:
```json
{
  "name": "Apple Inc."
}
```

### 7. Delete Brand
- **Method**: `DELETE`
- **URL**: `/brands/{id}`
- **Parameters**:
  - `id` (path) - Brand ID
- **Response**: 204 No Content
- **Example**: `DELETE /brands/60f7b1b9e4b0c8a3f8f3a1b2`

### 8. Check Brand Exists by ID
- **Method**: `GET`
- **URL**: `/brands/{id}/exists`
- **Parameters**:
  - `id` (path) - Brand ID
- **Response**: Boolean value
- **Example**: `GET /brands/60f7b1b9e4b0c8a3f8f3a1b2/exists`

### 9. Check Brand Exists by Name
- **Method**: `GET`
- **URL**: `/brands/name/{name}/exists`
- **Parameters**:
  - `name` (path) - Brand name
- **Response**: Boolean value
- **Example**: `GET /brands/name/Apple/exists`

## Data Transfer Objects

### BrandDTO
```json
{
  "id": "string",          // Brand ID (auto-generated)
  "name": "string"         // Brand name (required, 1-100 characters)
}
```

## Error Responses

### Validation Errors
- **Status**: 400 Bad Request
- **Response**: List of validation error messages

### Not Found
- **Status**: 404 Not Found
- **Response**: Empty body

### Duplicate Name
- **Status**: 400 Bad Request
- **Response**: Error message about duplicate brand name

## Business Rules

1. **Unique Name**: Brand names must be unique across the system
2. **Required Fields**: Brand name is required and cannot be blank
3. **Name Length**: Brand name must be between 1 and 100 characters
4. **Case Sensitivity**: Brand name comparison is case-sensitive

## Usage Examples

### Create a new brand
```bash
curl -X POST "${api.prefix}/brands" \
  -H "Content-Type: application/json" \
  -d '{"name": "Samsung"}'
```

### Get all brands with pagination
```bash
curl -X GET "${api.prefix}/brands?page=0&size=5&sortBy=name&sortDirection=asc"
```

### Update a brand
```bash
curl -X PUT "${api.prefix}/brands/60f7b1b9e4b0c8a3f8f3a1b2" \
  -H "Content-Type: application/json" \
  -d '{"name": "Samsung Electronics"}'
```

### Delete a brand
```bash
curl -X DELETE "${api.prefix}/brands/60f7b1b9e4b0c8a3f8f3a1b2"
```