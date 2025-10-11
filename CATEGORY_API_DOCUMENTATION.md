# Category API Documentation

## Overview
This document describes the REST API endpoints for managing Category entities in the E-Commerce TechShop application.

Base URL: `${api.prefix}/categories`

## Endpoints

### 1. Get All Categories (Paginated)
- **Method**: `GET`
- **URL**: `/categories`
- **Parameters**:
  - `page` (optional, default: 0) - Page number
  - `size` (optional, default: 10) - Page size
  - `sortBy` (optional, default: "name") - Sort field
  - `sortDirection` (optional, default: "asc") - Sort direction (asc/desc)
- **Response**: List of CategoryDTO objects
- **Example**: `GET /categories?page=0&size=10&sortBy=name&sortDirection=asc`

### 2. Get All Categories (No Pagination)
- **Method**: `GET`
- **URL**: `/categories/all`
- **Response**: List of all CategoryDTO objects
- **Example**: `GET /categories/all`

### 3. Get Category by ID
- **Method**: `GET`
- **URL**: `/categories/{id}`
- **Parameters**:
  - `id` (path) - Category ID
- **Response**: CategoryDTO object
- **Example**: `GET /categories/60f7b1b9e4b0c8a3f8f3a1b2`

### 4. Get Category by Name
- **Method**: `GET`
- **URL**: `/categories/name/{name}`
- **Parameters**:
  - `name` (path) - Category name
- **Response**: CategoryDTO object
- **Example**: `GET /categories/name/Electronics`

### 5. Create Category
- **Method**: `POST`
- **URL**: `/categories`
- **Request Body**: CategoryDTO object
- **Response**: Created CategoryDTO object
- **Validation**:
  - `name`: Required, 1-100 characters
  - `description`: Optional, max 500 characters
- **Example Request**:
```json
{
  "name": "Electronics",
  "description": "Electronic devices and accessories"
}
```

### 6. Update Category
- **Method**: `PUT`
- **URL**: `/categories/{id}`
- **Parameters**:
  - `id` (path) - Category ID
- **Request Body**: CategoryDTO object
- **Response**: Updated CategoryDTO object
- **Validation**:
  - `name`: Required, 1-100 characters
  - `description`: Optional, max 500 characters
- **Example Request**:
```json
{
  "name": "Consumer Electronics",
  "description": "Consumer electronic devices and accessories"
}
```

### 7. Delete Category
- **Method**: `DELETE`
- **URL**: `/categories/{id}`
- **Parameters**:
  - `id` (path) - Category ID
- **Response**: 204 No Content
- **Example**: `DELETE /categories/60f7b1b9e4b0c8a3f8f3a1b2`

### 8. Check Category Exists by ID
- **Method**: `GET`
- **URL**: `/categories/{id}/exists`
- **Parameters**:
  - `id` (path) - Category ID
- **Response**: Boolean value
- **Example**: `GET /categories/60f7b1b9e4b0c8a3f8f3a1b2/exists`

### 9. Check Category Exists by Name
- **Method**: `GET`
- **URL**: `/categories/name/{name}/exists`
- **Parameters**:
  - `name` (path) - Category name
- **Response**: Boolean value
- **Example**: `GET /categories/name/Electronics/exists`

## Data Transfer Objects

### CategoryDTO
```json
{
  "id": "string",            // Category ID (auto-generated)
  "name": "string",          // Category name (required, 1-100 characters)
  "description": "string"    // Category description (optional, max 500 characters)
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
- **Response**: Error message about duplicate category name

## Business Rules

1. **Unique Name**: Category names must be unique across the system
2. **Required Fields**: Category name is required and cannot be blank
3. **Name Length**: Category name must be between 1 and 100 characters
4. **Description Length**: Category description is optional but cannot exceed 500 characters
5. **Case Sensitivity**: Category name comparison is case-sensitive

## Usage Examples

### Create a new category
```bash
curl -X POST "${api.prefix}/categories" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Smartphones",
    "description": "Mobile phones and smartphones"
  }'
```

### Create a category without description
```bash
curl -X POST "${api.prefix}/categories" \
  -H "Content-Type: application/json" \
  -d '{"name": "Laptops"}'
```

### Get all categories with pagination
```bash
curl -X GET "${api.prefix}/categories?page=0&size=5&sortBy=name&sortDirection=asc"
```

### Update a category
```bash
curl -X PUT "${api.prefix}/categories/60f7b1b9e4b0c8a3f8f3a1b2" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Gaming Laptops",
    "description": "High-performance laptops for gaming"
  }'
```

### Delete a category
```bash
curl -X DELETE "${api.prefix}/categories/60f7b1b9e4b0c8a3f8f3a1b2"
```

### Search for a category by name
```bash
curl -X GET "${api.prefix}/categories/name/Electronics"
```

### Check if category exists
```bash
curl -X GET "${api.prefix}/categories/name/Electronics/exists"
```

## Common Use Cases

### 1. Category Management Dashboard
```javascript
// Get paginated categories for admin dashboard
fetch('/api/v1/categories?page=0&size=20&sortBy=name')
  .then(response => response.json())
  .then(categories => displayCategories(categories));
```

### 2. Category Dropdown for Product Creation
```javascript
// Get all categories for product form dropdown
fetch('/api/v1/categories/all')
  .then(response => response.json())
  .then(categories => populateDropdown(categories));
```

### 3. Category Validation
```javascript
// Check if category name is available before creating
fetch('/api/v1/categories/name/NewCategory/exists')
  .then(response => response.json())
  .then(exists => {
    if (exists) {
      showError('Category name already exists');
    }
  });
```