# LifeLink — Viva Preparation

## Short project introduction

LifeLink is a Java web-based blood bank management prototype. It connects four roles: donor, recipient, blood bank and administrator. The main workflows are donation appointments, inventory management, recipient blood requests and bank-to-bank transfers. Java Servlets handle application logic, JSP/JSTL render pages, JDBC connects to MySQL, and Tomcat 9 runs the WAR application.

## Common questions and answers

**1. What problem does LifeLink address?**  
It demonstrates a centralized way to organize donor appointments, blood stock records, recipient requests and transfers instead of maintaining disconnected records.

**2. Why did you choose Java?**  
Java provides a mature object-oriented language and servlet ecosystem for building structured server-side web applications.

**3. What is a Servlet?**  
A Servlet is a Java server-side component that receives HTTP requests, runs application logic and sends or forwards an HTTP response.

**4. What is JSP?**  
JavaServer Pages is a server-side view technology used to generate HTML. In this project, JSP pages display data prepared by servlets.

**5. What is JDBC?**  
Java Database Connectivity is the Java API used to connect to relational databases and execute SQL statements.

**6. Why use MySQL?**  
MySQL provides relational tables, constraints, foreign keys and transactions suitable for the application's structured records.

**7. Why use BCrypt?**  
BCrypt hashes passwords with a salt and a configurable work factor. The application stores the hash, not the original password.

**8. What is the purpose of the administrator?**  
The administrator reviews new registrations and approves or rejects accounts before they can use role-specific features.

**9. Why are roles used?**  
Role-based access ensures that donors, recipients, blood banks and administrators can only access the workflows intended for them.

**10. What is a prepared statement?**  
A PreparedStatement parameterizes SQL values. It helps prevent SQL injection and separates query structure from user input.

**11. Why are database transactions important here?**  
A transaction groups related database changes. If one part fails, the application rolls back the whole operation so inventory and request status do not become inconsistent.

**12. How do you prevent duplicate stock additions after a donation?**  
The appointment row is locked and can transition to COMPLETED only from APPROVED. The stock increment occurs in the same transaction as that one-time transition.

**13. How is stock deducted for a recipient request?**  
The bank can fulfil an APPROVED request only if an update confirms enough units are available. The stock deduction and status change occur in one transaction.

**14. How does a bank transfer work?**  
The receiving bank accepts a pending request. The application verifies sender stock, deducts it, adds the same quantity to the receiver and marks the transfer complete within one transaction.

**15. What is CSRF?**  
Cross-Site Request Forgery tricks a logged-in browser into submitting an unwanted action. LifeLink uses a session-bound token on state-changing forms.

**16. Why Tomcat 9?**  
This version of the application uses the javax.servlet API. Tomcat 9 supports it; Tomcat 10 and later use the renamed jakarta.servlet API.

**17. What is a WAR file?**  
A Web Application Archive packages a Java web application for deployment to a servlet container such as Tomcat.

**18. What is the role of Maven?**  
Maven manages dependencies and builds the application into a WAR file using the project's configuration.

**19. What does GitHub Actions do?**  
It automatically runs the Maven build when code is pushed to the main branch or a pull request targets main.

**20. What are the limitations?**  
This is an academic simulation. It does not perform medical eligibility screening, blood testing, clinical compatibility checks, identity verification or real-world blood dispatch.

## Demo flow

1. Create the first admin account.
2. Register and approve a blood bank.
3. Register and approve a donor and recipient.
4. Enter stock as the blood bank.
5. Book and complete a donor appointment.
6. Submit and fulfil a recipient request.
7. Demonstrate a transfer between two approved banks.
