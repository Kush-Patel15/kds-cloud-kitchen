# Kitchen Display System (KDS) for Cloud Kitchens

## Overview
The Kitchen Display System (KDS) is designed for cloud kitchen owners and users to streamline order management and enhance kitchen operations. This application provides a user-friendly interface for managing orders, menu items, kitchen stations, and notifications.

## Features
- **Order Management**: Create, update, retrieve, and delete orders.
- **Menu Management**: Manage menu items including their details and pricing.
- **Kitchen Station Management**: Monitor and manage kitchen stations.
- **User Management**: Handle user accounts and roles.
- **Notifications**: Send and manage notifications related to orders and kitchen operations.

## Technologies Used
- Spring Boot: For building the application.
- JPA/Hibernate: For database interactions.
- Maven: For project management and dependencies.

## Getting Started

### Prerequisites
- Java 11 or higher
- Maven
- A relational database (e.g., MySQL, PostgreSQL)

### Installation
1. Clone the repository:
   ```
   git clone <repository-url>
   ```
2. Navigate to the project directory:
   ```
   cd kds-cloud-kitchen
   ```
3. Update the `application.properties` file with your database configuration.
4. Build the project using Maven:
   ```
   mvn clean install
   ```

### Running the Application
To run the application, use the following command:
```
mvn spring-boot:run
```
The application will start on the default port (8080).

### API Endpoints
- **Orders**
  - `POST /orders`: Create a new order
  - `GET /orders/{id}`: Retrieve an order by ID
  - `PUT /orders/{id}`: Update an existing order
  - `DELETE /orders/{id}`: Delete an order

- **Menu Items**
  - `POST /menu-items`: Create a new menu item
  - `GET /menu-items/{id}`: Retrieve a menu item by ID
  - `PUT /menu-items/{id}`: Update an existing menu item
  - `DELETE /menu-items/{id}`: Delete a menu item

- **Kitchen Stations**
  - `POST /kitchen-stations`: Create a new kitchen station
  - `GET /kitchen-stations/{id}`: Retrieve a kitchen station by ID
  - `PUT /kitchen-stations/{id}`: Update an existing kitchen station
  - `DELETE /kitchen-stations/{id}`: Delete a kitchen station

- **Users**
  - `POST /users`: Create a new user
  - `GET /users/{id}`: Retrieve a user by ID
  - `PUT /users/{id}`: Update an existing user
  - `DELETE /users/{id}`: Delete a user

- **Notifications**
  - `POST /notifications`: Create a new notification
  - `GET /notifications/{id}`: Retrieve a notification by ID
  - `DELETE /notifications/{id}`: Delete a notification

## Testing
To run the tests, use the following command:
```
mvn test
```

## License
This project is licensed under the MIT License. See the LICENSE file for details.