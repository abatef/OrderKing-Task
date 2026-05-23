# Customer Management Application
> [!NOTE]  
> I wrote this README file using AI because I was in a hurry and I wanted to create a good README file fast enought;
> I just wrote the main idea and let the AI do the rest.

A full-stack customer management system with a **Spring Boot REST API backend** and a **Swing desktop client**. The desktop app communicates exclusively via HTTP — no direct database access.

---

## Project Structure

```
task/
├── customer-api/          ← Spring Boot Backend (REST API + MySQL)
│   ├── pom.xml
│   └── src/main/java/com/customer/api/
│       ├── CustomerApiApplication.java
│       ├── config/SecurityConfig.java
│       ├── controller/
│       │   ├── CustomerController.java
│       │   └── AuthController.java
│       ├── service/
│       │   ├── CustomerService.java
│       │   └── AuthService.java
│       ├── repository/
│       │   ├── CustomerRepository.java
│       │   └── UserRepository.java
│       ├── model/
│       │   ├── Customer.java
│       │   ├── User.java
│       │   └── AuthRequest.java
│       └── exception/GlobalExceptionHandler.java
│
├── customer-desktop/      ← Swing Desktop Client
│   ├── pom.xml
│   └── src/main/java/com/customer/desktop/
│       ├── CustomerApp.java
│       ├── model/Customer.java
│       ├── service/ApiService.java
│       └── ui/
│           ├── LoginDialog.java
│           ├── MainFrame.java
│           └── CustomerDialog.java
│
├── schema.sql             ← MySQL table creation script
└── README.md
```

## Architecture

```
┌─────────────────────┐         HTTP/REST          ┌──────────────────────┐
│   Swing Desktop     │  ──────────────────────►   │   Spring Boot API    │
│      Client         │  ◄──────────────────────   │      Backend         │
│                     │    Authorization: Bearer   │                      │
│  - Login/Register   │                            │  - /auth (login,     │
│  - JTable           │                            │    register, logout) │
│  - Add/Edit/Delete  │                            │  - /customers CRUD   │
│  - Search/Filter    │                            │  - JPA + MySQL       │
│  - Loading Spinner  │                            │  - Input Validation  │
└─────────────────────┘                            └──────────┬───────────┘
                                                              │
                                                    ┌─────────▼──────────┐
                                                    │   MySQL Database   │
                                                    │   (customer_db)    │
                                                    └────────────────────┘
```

---

## Prerequisites

- **Java 21** (or newer)
- **Apache Maven 3.8+**
- **MySQL 8.0+**

---

## Quick Start

### 1. Set Up MySQL

```bash
sudo mysql -e "
  CREATE DATABASE IF NOT EXISTS customer_db;
  CREATE USER IF NOT EXISTS 'customer_user'@'localhost' IDENTIFIED BY 'customer_pass';
  GRANT ALL PRIVILEGES ON customer_db.* TO 'customer_user'@'localhost';
  FLUSH PRIVILEGES;
"
```

Or run the schema file directly:

```bash
sudo mysql < schema.sql
```

### 2. Start the Spring Boot Backend

```bash
cd customer-api
mvn spring-boot:run
```

The API starts on `http://127.0.0.1:8080`.

### 3. Start the Swing Desktop Client

In a new terminal:

```bash
cd customer-desktop
mvn compile exec:java -Dexec.mainClass="com.customer.desktop.CustomerApp"
```

The login dialog will appear — register a new account and log in.

Or build and run as a standalone JAR:

```bash
cd customer-desktop
mvn clean package -DskipTests
java -jar target/customer-desktop-1.0.0.jar
```

### Building Standalone JARs

```bash
# Backend
cd customer-api
mvn clean package -DskipTests
java -jar target/customer-api-1.0.0.jar

# Desktop (fat JAR — includes all dependencies)
cd customer-desktop
mvn clean package -DskipTests
java -jar target/customer-desktop-1.0.0.jar
```

---

## API Documentation

### Authentication Endpoints

These endpoints are **public** (no token required):

| Method | Endpoint | Description | Status Codes |
|--------|----------|-------------|--------------|
| POST | `/auth/register` | Register a new user | 201, 400 |
| POST | `/auth/login` | Login and get a bearer token | 200, 400 |
| POST | `/auth/logout` | Invalidate the current token | 204 |

**Register:**
```bash
curl -X POST http://127.0.0.1:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin12345"}'
```

**Login:**
```bash
curl -X POST http://127.0.0.1:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin12345"}'
```

Response: `{"token":"<your-bearer-token>"}`

**Logout:**
```bash
curl -X POST http://127.0.0.1:8080/auth/logout \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Customer Endpoints

All customer endpoints require a `Bearer` token in the `Authorization` header:

```
Authorization: Bearer <your-token>
```

| Method | Endpoint | Description | Status Codes |
|--------|----------|-------------|--------------|
| GET | `/customers` | List all customers (paginated) | 200 |
| GET | `/customers?search=query` | Search by name or email | 200 |
| GET | `/customers?page=0&size=10` | Paginated listing | 200 |
| GET | `/customers/{id}` | Get a single customer | 200, 404 |
| POST | `/customers` | Create a new customer | 201, 400 |
| PUT | `/customers/{id}` | Update a customer | 200, 400, 404 |
| DELETE | `/customers/{id}` | Delete a customer | 204, 404 |

### Example Requests

**Create a customer:**
```bash
curl -X POST http://127.0.0.1:8080/customers \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"John Doe","email":"john@test.com","phone":"+123456789"}'
```

**List all customers:**
```bash
curl http://127.0.0.1:8080/customers \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Search customers:**
```bash
curl "http://127.0.0.1:8080/customers?search=john" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## Desktop Application Features

- **Login/Register**: Secure authentication with username and password
- **Logout**: Session invalidation via the header button
- **Customer Table**: JTable with alternating row colors
- **Add Customer**: Modal dialog with input validation
- **Edit Customer**: Pre-fills dialog with selected customer data (also via double-click)
- **Delete Customer**: Confirmation dialog before deletion
- **Search/Filter**: Search by name or email via the API
- **Loading Indicator**: Animated spinner overlay during API calls
- **Error Handling**: Graceful error messages when API is unreachable
- **Modern Look**: FlatLaf dark theme with polished styling

---

## Technologies Used

| Component | Technology |
|-----------|------------|
| Backend | Spring Boot 3.2, Spring Data JPA |
| Database | MySQL 8.0 |
| Authentication | BCrypt password hashing, Bearer tokens |
| Desktop UI | Swing + FlatLaf (Modern Dark Theme) |
| HTTP Client | java.net.http.HttpClient |
| JSON | Jackson |
| Build Tool | Maven |
| Language | Java 21 |
