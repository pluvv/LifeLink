# LifeLink — Blood Bank Management System

LifeLink is a Java web application demonstrating workflows between donors, recipients, blood banks and an administrator.

> **Educational project only.** LifeLink is not a real blood service. Do not use it for medical decisions, real patient data, or real-world blood allocation.

## Technology stack

- Java 17+
- Maven 3.9+
- Java Servlets / JSP using the `javax.servlet` API
- Apache Tomcat 9 (Tomcat 10/11 are not compatible without migrating to `jakarta.servlet`)
- MySQL 8 and JDBC
- JSTL and BCrypt
- GitHub Actions for automated Maven builds

## Features

- Donor, recipient and blood-bank registration
- BCrypt password hashing and role-based access
- One-time first-administrator setup and admin approval of registrations
- Donor appointment booking and blood-bank processing
- Blood inventory management for all eight blood groups
- Recipient blood requests and fulfilment
- Inter-bank blood stock transfer workflow
- CSRF protection and database transactions for stock-changing operations

## Quick start: fork, clone and run

### 1. Get the source code

You can fork this repository on GitHub, or clone it directly:

```bash
git clone https://github.com/pluvv/LifeLink.git
cd LifeLink
```

If you are developing from your own fork, clone your fork's URL instead. You can also use GitHub's **Code → Download ZIP** option, then extract the project.

### 2. Install prerequisites

Install:

1. JDK 17 or newer
2. Maven 3.9 or newer
3. MySQL Server 8 (MySQL Workbench is optional but helpful)
4. Apache Tomcat 9

Check that Java and Maven are available:

```bash
java -version
mvn -version
```

### 3. Create the database

1. Start MySQL.
2. Open MySQL Workbench (or another MySQL client).
3. Open `database/schema.sql` and execute the entire script. It creates the `lifelink` database and all application tables.
4. Optionally run `database/seed.sql`. It contains notes only; it does not create users or insert real personal data.

### 4. Configure your local database connection

Copy the example configuration:

**Windows PowerShell**
```powershell
Copy-Item src/main/resources/db.properties.example src/main/resources/db.properties
```

**macOS / Linux**
```bash
cp src/main/resources/db.properties.example src/main/resources/db.properties
```

Edit `src/main/resources/db.properties` and enter your own MySQL credentials:

```properties
db.url=jdbc:mysql://localhost:3306/lifelink?useSSL=false&serverTimezone=Asia/Kolkata
db.username=YOUR_MYSQL_USERNAME
db.password=YOUR_LOCAL_MYSQL_PASSWORD
```

**Never commit or share `db.properties`, database passwords, production credentials, or real user data.** The local file is excluded by `.gitignore`. Keep the example file in the repository and update it only with non-secret defaults.

### 5. Build the application

Run this from the project root:

```bash
mvn clean package
```

Maven creates:

```
target/LifeLink.war
```

### 6. Deploy to Tomcat 9

1. Copy `target/LifeLink.war` into Tomcat's `webapps` directory.
2. Start Tomcat.
3. Open [http://localhost:8080/LifeLink/](http://localhost:8080/LifeLink/).
4. Open [http://localhost:8080/LifeLink/setup-admin](http://localhost:8080/LifeLink/setup-admin) and create the first administrator. This one-time setup endpoint is unavailable once an administrator exists.
5. Register donor, recipient and blood-bank accounts, then approve them from the administrator account.

If Tomcat uses a different port, change `8080` in the URLs. If the application cannot connect to MySQL, verify that MySQL is running and that `db.properties` has the correct database name, username and password.

## CI build

GitHub Actions runs `mvn clean package` for pushes and pull requests targeting `main`. A successful run uploads `LifeLink.war` as the `LifeLink-war` workflow artifact for 14 days. See the **Actions** tab in GitHub to view build results and download the artifact.

## Project structure

```
database/                  MySQL schema and seed notes
docs/                      Project report, test plan and viva notes
src/main/java/             Servlets, filters, DAO, models and utilities
src/main/resources/        Database configuration example
src/main/webapp/           JSP pages, views, CSS and web configuration
.github/workflows/         GitHub Actions Maven build
pom.xml                    Maven WAR project definition
```

## Documentation

- [Development and troubleshooting guide](docs/DEVELOPMENT.md)
- [Contributing guide](CONTRIBUTING.md)
- [Project report](docs/PROJECT_REPORT.md)
- [Manual test plan](docs/TEST_PLAN.md)
- [Viva preparation](docs/VIVA_PREPARATION.md)

## Suggested demo flow

1. Create the administrator.
2. Register a blood bank and approve it.
3. Register a donor and recipient and approve both.
4. Sign in as the blood bank and enter sample inventory.
5. Book and process a donor appointment.
6. Submit and fulfil a recipient request.
7. Register a second bank and demonstrate an inter-bank transfer.

Use fictional demonstration data only.

## Security and limitations

The project includes BCrypt password hashing, prepared SQL statements, role checks, session ID rotation after login, CSRF protection, output escaping in key views, and transactions for inventory-changing workflows.

This is a learning project, not production-ready software. It does not perform identity verification, medical eligibility screening, blood testing, clinical compatibility validation, or real-world notification and compliance workflows. Do not deploy it with real patient or donor information.
