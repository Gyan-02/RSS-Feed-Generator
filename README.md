# RSS Feed Generator

React and Vite frontend for creating RSS 2.0 feeds. The project uses two independent Spring Boot services:

- **Feed Service** (`backend/`, port `8080`): manages RSS feeds and articles with Spring Data JPA and an in-memory H2 database.
- **User Service** (`user-service/`, port `8082`): registers accounts and verifies login credentials using BCrypt, with user records stored in MySQL.

## Start the services

### 1. Start MySQL

Create the database once:

```sql
CREATE DATABASE rss_users;
```

Set the connection values in the same PowerShell window where you will start the User Service:

```powershell
$env:MYSQL_URL = "jdbc:mysql://localhost:3306/rss_users"
$env:MYSQL_USERNAME = "root"
$env:MYSQL_PASSWORD = "your-local-mysql-password"
$env:ADMIN_API_KEY = "set-a-long-random-admin-key"
```

### 2. Start the User Service

```powershell
cd user-service
mvn spring-boot:run
```

### 3. Start the Feed Service

In another terminal:

```powershell
cd backend
mvn spring-boot:run
```

### 4. Start the React app

In a third terminal from the project root:

```powershell
npm install
npm run dev
```

The frontend uses `http://localhost:8080/api` for feed requests and `http://localhost:8082/api` for account requests. Override these with `VITE_API_BASE_URL` and `VITE_USER_API_BASE_URL` if your services use different addresses.

## Account API

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/users` | Register an account; passwords are stored as BCrypt hashes |
| `POST` | `/api/users/login` | Verify credentials and return a safe profile, or `401` |
| `PUT` | `/api/users/{id}` | Update profile name and email after confirming the current password |
| `GET` | `/admin` | Return account registration times and successful sign-in history; requires the `X-Admin-Key` header |

Set `ADMIN_API_KEY` in the User Service environment to enable `/admin`. This endpoint has no frontend route, and its key must stay on the server. Successful registrations and sign-ins are timestamped in the MySQL database. The login flow records sign-ins but does not issue a token or create a persistent session.

## Feed API

The Feed Service provides `GET`, `POST`, `PUT`, and `DELETE` operations under `/api/feeds`, plus `GET /api/feeds/{id}/rss`. Its in-memory H2 records reset when the service stops.
