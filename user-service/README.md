# RSS User Service

This is an independently runnable Spring Boot service for RSS Feed Generator accounts. It runs on port `8082`, stores users in MySQL, and hashes passwords with BCrypt before saving them.

## Database setup

Create the service database in MySQL:

```sql
CREATE DATABASE rss_users;
```

Configure the connection before starting the service. PowerShell example:

```powershell
$env:MYSQL_URL = "jdbc:mysql://localhost:3306/rss_users"
$env:MYSQL_USERNAME = "root"
$env:MYSQL_PASSWORD = "your-local-mysql-password"
$env:ADMIN_API_KEY = "set-a-long-random-admin-key"
```

The password is read from the environment and is not stored in this project. If your MySQL account has no password, set `MYSQL_PASSWORD` to an empty string.

## Run

From the `user-service` directory:

```powershell
mvn spring-boot:run
```

The frontend defaults to `http://localhost:8082/api`. To use another service URL, set `VITE_USER_API_BASE_URL` before starting Vite.

## Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/users` | Register an account and store a BCrypt hash |
| `POST` | `/api/users/login` | Verify credentials and return the safe profile |
| `PUT` | `/api/users/{id}` | Update name and email after confirming the current password |
| `GET` | `/admin` | Return each account's registration time and sign-in history; requires `X-Admin-Key` matching `ADMIN_API_KEY` |

The `/admin` endpoint is backend-only; there is no frontend admin page or link. It returns `id`, `name`, `email`, `registeredAt`, and each successful `signIns` timestamp. Do not expose the admin key to browser code. Registration and sign-in timestamps are stored in MySQL in UTC. New accounts receive a registration timestamp; older account rows may have a null timestamp. Login verifies credentials and records successful sign-ins, but does not create a persistent user session or issue a token.
