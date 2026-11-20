# LifeLink — Blood Bank Management System

LifeLink is a Java web application for a university project. It demonstrates workflows between donors, recipients, blood banks and an administrator.

> **Important:** LifeLink is an educational demonstration, not a real blood service. Do not use it for medical decisions or real-world blood allocation.

## Technology

- Java 17 source compatibility
- Maven 3.9+
- Java Servlets / JSP (javax.servlet)
- Apache Tomcat 9
- MySQL 8
- JDBC, JSTL and BCrypt
- Responsive custom CSS
- GitHub Actions Maven build

## Implemented modules

### Accounts and administration
- Donor, recipient and blood-bank registration
- Password hashing with BCrypt
- Administrator-only one-time bootstrap
- Admin approval/rejection of registrations
- Role-protected workspaces and session login/logout
- CSRF protection on form submissions

### Donors
- Book appointments at approved blood banks
- View appointment history and status
- Blood-bank approval/rejection of appointment requests
- Completing an approved donation adds one unit to the donor's blood group inventory in the same database transaction

### Recipients
- Browse approved blood banks and their recorded stock
- Submit blood requests for a selected blood group and number of units
- Track request status
- Bank approval/rejection and fulfilment workflow
- Fulfilment deducts stock only when sufficient inventory exists

### Blood banks
- Maintain the eight blood-group inventory values
- Review and process donation appointments
- Review recipient blood requests
- Request inter-bank stock transfers
- Accepting a transfer atomically deducts from the sending bank and adds to the receiving bank

## Prerequisites

Install these on Windows:

1. **JDK 17 or newer** (the project targets Java 17).
2. **Maven 3.9 or newer**.
3. **MySQL Server 8**. MySQL Workbench is useful for running the SQL scripts.
4. **Apache Tomcat 9**. Do not use Tomcat 10/11 for this build because it uses the javax.servlet API.

## Database setup

1. Open MySQL Workbench and connect to your local MySQL server.
2. Open database/schema.sql and execute the complete script. It creates the lifelink database and all required tables.
3. Copy src/main/resources/db.properties.example to src/main/resources/db.properties.
4. Edit db.properties with your own local MySQL username and password. Example:

   ```properties
   db.url=jdbc:mysql://localhost:3306/lifelink?useSSL=false&serverTimezone=Asia/Kolkata
   db.username=root
   db.password=YOUR_LOCAL_MYSQL_PASSWORD
   ```

   Never commit db.properties. It is ignored by Git.

There are no default demo credentials. Create the first administrator using the one-time setup page after deploying the app.

## Build the application

From the repository folder:

```powershell
mvn clean package
```

The deployable file is created at:

```
target/LifeLink.war
```

GitHub Actions also runs mvn clean package on pushes and pull requests to main.

## Run with Tomcat 9

1. Build the WAR using the command above.
2. Copy target/LifeLink.war into Tomcat's webapps folder.
3. Start Tomcat.
4. Open http://localhost:8080/LifeLink/.
5. Open http://localhost:8080/LifeLink/setup-admin and create the first administrator. This page becomes unavailable after an admin exists.
6. Sign in as admin, then approve donor, recipient and blood-bank registrations.
7. Sign in with each approved role to demonstrate the corresponding workflow.

If your Tomcat port is not 8080, replace the port in the URL with your configured port.

## Suggested demonstration sequence

1. Create the administrator account.
2. Register a blood bank and approve it from the admin workspace.
3. Register a donor and recipient and approve both.
4. Sign in as the bank and enter inventory for the eight blood groups.
5. Sign in as donor, request an appointment, then return to the bank to approve and complete it.
6. Sign in as recipient, browse the bank's stock and submit a request.
7. Return to the bank, approve and fulfil the request, then show the stock deduction.
8. Register and approve a second bank, create a transfer request, and accept it from the receiving bank.

## Database tables

- users, donor_profiles, recipient_profiles, blood_banks
- blood_inventory
- donation_appointments
- blood_requests
- transfer_requests

The SQL schema uses foreign keys, enum-backed statuses, non-negative stock checks and unique inventory rows per bank/blood group.

## Security and project limitations

- Passwords are stored as BCrypt hashes.
- SQL uses prepared statements.
- Role checks are applied to protected routes.
- Session IDs are rotated after successful login.
- State-changing forms use session-bound CSRF tokens.
- User-provided content is escaped in the main administrative and workflow views.
- Database transactions protect donation completion, request fulfilment and transfer acceptance against duplicate stock changes.
- Inventory is demonstration data. There is no identity verification, medical eligibility screening, blood testing, clinical compatibility validation, SMS/email notification, or production hosting configuration.

## Project documentation

- [Project report](docs/PROJECT_REPORT.md)
- [Manual test plan](docs/TEST_PLAN.md)
- [Viva preparation](docs/VIVA_PREPARATION.md)

## Repository layout

```
database/                  MySQL schema and seed notes
src/main/java/             Servlets, filters, DAO and utilities
src/main/resources/        Local database configuration template
src/main/webapp/           JSP pages, views and CSS
.github/workflows/         Maven CI build
pom.xml                    Maven WAR project
```
