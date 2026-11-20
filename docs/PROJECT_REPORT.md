# LifeLink — Project Report

## 1. Abstract

LifeLink is a role-based blood bank management web application developed with Java Servlets, JSP, JDBC and MySQL. It provides a shared workflow for donors, recipients, blood banks and administrators. Donors can request donation appointments, recipients can view recorded blood stock and submit requests, blood banks can manage inventory and process requests, and administrators can approve registrations. The system uses database transactions for stock-changing operations so a donation, fulfilled request or accepted transfer updates inventory consistently.

LifeLink is an academic software demonstration. It is not intended to replace a real blood bank, medical screening process or clinical decision system.

## 2. Problem statement

Blood donation coordination involves several parties and time-sensitive information. When donor appointments, available stock, recipient requests and transfers are maintained separately, information can become difficult to track and stock records may become inconsistent. A small centralized application can organize these records, provide role-specific access and make important status changes traceable.

## 3. Objectives

- Provide separate workspaces for donors, recipients, blood banks and administrators.
- Maintain records for all eight standard ABO/Rh blood groups.
- Allow donors to request and track donation appointments.
- Allow recipients to browse approved banks and submit blood requests.
- Allow blood banks to process appointments, recipient requests and stock transfers.
- Keep inventory changes consistent using database transactions.
- Protect accounts and routes with password hashing, role checks and CSRF protection.

## 4. Scope

The project covers account registration and approval, appointment booking, stock recording, recipient requests, and inter-bank stock transfer. It stores fictional demonstration data and demonstrates the software workflow only.

The project does not perform donor identity verification, medical eligibility checks, blood testing, compatibility validation, emergency dispatch, real-time hospital integration, or regulated clinical inventory management.

## 5. Technology stack

| Layer | Technology |
|---|---|
| User interface | JSP, HTML, CSS, JSTL |
| Application logic | Java Servlets |
| Data access | JDBC |
| Database | MySQL 8 |
| Password security | BCrypt |
| Application server | Apache Tomcat 9 |
| Build | Maven WAR |
| CI | GitHub Actions |

The application uses the javax.servlet API and is configured for Tomcat 9.

## 6. System roles

### Administrator
Reviews pending account registrations and approves or rejects donor, recipient and blood-bank accounts. The first administrator is created through a one-time bootstrap page that becomes unavailable once an administrator exists.

### Donor
Registers with a blood group, requests an appointment at an approved blood bank and checks appointment status/history.

### Recipient
Browses approved blood banks and their recorded inventory, submits a request for a blood group and number of units, and tracks its status.

### Blood bank
Maintains its inventory, processes donor appointments, processes recipient requests and participates in transfers with other approved banks.

## 7. Architecture

The application follows a simple layered web architecture:

1. JSP pages render the interface.
2. Servlets receive requests, validate input and coordinate workflows.
3. JDBC prepared statements communicate with MySQL.
4. The relational database stores accounts, profiles, appointments, stock, requests and transfers.
5. Servlet filters enforce authentication, role access and CSRF validation.

## 8. Database design

Main tables:

- users — login identity, password hash, role and approval status.
- donor_profiles — donor blood group and contact/location details.
- recipient_profiles — recipient contact/location details.
- blood_banks — bank identity, address and contact details.
- blood_inventory — units for each blood group at each bank.
- donation_appointments — donor appointment requests and status.
- blood_requests — recipient requests and status.
- transfer_requests — stock movement requests between banks.

Relationships:

- Each profile row belongs to one user.
- Each blood bank has multiple inventory rows, one per blood group.
- A donor can have multiple appointments.
- A recipient can have multiple blood requests.
- A bank can send and receive multiple transfer requests.

The inventory table uses a composite primary key of bank_user_id and blood_group, preventing duplicate stock rows for the same bank and blood group.

## 9. Important workflows

### Donation
1. An approved donor selects an approved blood bank and appointment time.
2. The appointment is created with PENDING status.
3. The bank approves or rejects it.
4. The bank marks an approved appointment COMPLETED after the donation.
5. The application updates appointment status and increments the donor's blood group stock in one transaction.

### Recipient request
1. An approved recipient views approved banks and their recorded stock.
2. The recipient submits a blood group and requested units.
3. The bank approves or rejects the request.
4. On fulfilment, the application checks that sufficient stock exists, deducts the units and changes the request status in one transaction.

### Inter-bank transfer
1. A bank sends a transfer request to another approved bank.
2. The receiving bank accepts or rejects the request.
3. On acceptance, the application checks the sending bank's stock, deducts the units, adds them to the receiving bank and marks the transfer completed in one transaction.

## 10. Security measures

- Passwords are stored as BCrypt hashes rather than plaintext.
- SQL statements use prepared parameters.
- Protected routes check login state and role.
- Session IDs are rotated after successful login.
- State-changing forms use session-bound CSRF tokens.
- Main workflow views escape user-supplied text before rendering.
- Inventory-changing operations use database transactions and conditional updates.

## 11. Testing approach

The project includes a manual role-based test plan in TEST_PLAN.md. GitHub Actions runs a Maven package build on pushes and pull requests to the main branch. The Maven build verifies Java compilation and WAR packaging; a successful build does not replace manual testing against a configured MySQL database and Tomcat server.

## 12. Limitations and future enhancements

Possible future work includes email/SMS notifications, searchable bank locations, appointment rescheduling, donor eligibility workflows, detailed admin analytics, audit history, automated integration tests, and deployment to a secured production environment. Clinical validation and real-world blood handling would require professional oversight and compliance beyond this academic project.

## 13. Conclusion

LifeLink demonstrates how a centralized role-based web application can organize donor appointments, recipient requests, blood-bank inventory and inter-bank transfers. It combines Java web technologies with a relational database and transactional stock updates to provide a coherent end-to-end academic prototype.
