# PlanMyTrip Backend

PlanMyTrip is an AI-powered travel planning application designed to help users create, manage, and view their travel itineraries with ease.

## Table of Contents
- [Features](#features)
- [Technologies Used](#technologies-used)
- [Getting Started](#getting-started)
- [Available Scripts](#available-scripts)

## Features
- **User Management**: Handles user registration, authentication, and profile management.
- **Trip Planning**: Provides endpoints for creating, updating, and deleting travel itineraries.
- **Email Notifications**: Sends email confirmations and notifications to users regarding their travel plans.
- **Security**: Implements JWT-based authentication and authorization mechanisms.

## Technologies Used
- **Backend Framework**: Spring Boot 3.3.2
- **Database**: PostgreSQL
- **ORM**: Spring Data JPA
- **Email Service**: Spring Boot Starter Mail
- **Security**: Spring Security, JSON Web Tokens (JWT)
- **Mapping Services**: Google Maps Services
- **Build Tool**: Maven

## Getting Started
To get a local copy up and running, follow these steps:

1. Clone the Repository: ```git clone https://github.com/mohansai98/planmytrip-backend.git```
  
2. Navigate to the Project Directory: ```cd planmytrip-backend```

3. Configure the Database:
- Ensure that PostgreSQL is installed and running.
- Create a database named `planmytrip`.
- Update the `application.properties` file with your database credentials.

4. Install Dependencies and Build the Project: ```./mvnw clean install```
 
5. Run the Application: ```./mvnw spring-boot:run```

The backend server will start, and you can access it at http://localhost:8080.

## Available Scripts
In the project directory, you can run:

- `./mvnw spring-boot:run`: Starts the application.
- `./mvnw test`: Runs the test suite.
- `./mvnw clean install`: Cleans the target directory and installs the dependencies.

