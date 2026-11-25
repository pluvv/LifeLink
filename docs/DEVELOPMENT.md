# Development Guide

This guide is for anyone who forks, clones, downloads, or contributes to LifeLink.

## Local development checklist

- [ ] JDK 17 or newer installed
- [ ] Maven 3.9+ installed
- [ ] MySQL Server 8 running
- [ ] Apache Tomcat 9 available for deployment
- [ ] Database created from `database/schema.sql`
- [ ] Local `src/main/resources/db.properties` created from the example
- [ ] `mvn clean package` completes successfully

## Working on the code

The project is a Maven WAR application. Java source is under `src/main/java`; JSP pages and static assets are under `src/main/webapp`; SQL setup scripts are under `database`.

Useful commands:

```bash
# Compile and package
mvn clean package

# Run the project's tests (if/when tests are added)
mvn test

# Remove generated build output
mvn clean
```

Deploy `target/LifeLink.war` to a local Tomcat 9 instance to test servlet/JSP behavior. Rebuild after source changes and redeploy the WAR.

## Database changes

- Make schema changes in `database/schema.sql`.
- Keep SQL scripts reproducible for a fresh database.
- Update this guide or the README if setup steps change.
- Do not commit local database dumps, passwords, personal data, or production data.
- Use fictional records when testing.

For a clean local setup, execute the schema script in a disposable development database. Be aware that destructive SQL operations can permanently delete local data.

## Configuration and secrets

The application reads its database connection from `db.properties` on the classpath. Create it locally from `src/main/resources/db.properties.example`. The real file is ignored by Git.

Never add secrets to source code, SQL seed scripts, screenshots, issue reports, or commits. If a credential is accidentally committed, rotate it immediately; deleting it in a later commit does not make the exposed credential safe.

## Pull request workflow

1. Fork the repository.
2. Create a focused branch for your change.
3. Make the change and test with `mvn clean package`.
4. Test relevant behavior in Tomcat and MySQL where applicable.
5. Update documentation and SQL scripts if setup or behavior changes.
6. Open a pull request describing the change, how it was tested, and any limitations.

GitHub Actions automatically builds pull requests targeting `main`.

## Troubleshooting

### Maven command not found

Install Maven and ensure its `bin` directory is on your PATH. Reopen the terminal and run `mvn -version`.

### Database connection fails

Check that MySQL is running, the `lifelink` database exists, and the local `db.properties` URL, username and password are correct. Do not paste passwords into issue reports.

### Application returns HTTP 404

Confirm that the WAR was built and deployed as `LifeLink.war`, Tomcat finished deploying it, and the URL uses the correct context path and port. Check Tomcat's `logs` directory for deployment errors.

### Servlet/JSP classes fail to load

Use Tomcat 9 for this project. The application currently uses the `javax.servlet` API; Tomcat 10 and 11 use `jakarta.servlet` and are not drop-in replacements.

## Scope and safety

LifeLink is an educational demonstration. It has not been designed or audited for production use, clinical workflows, regulated health data, or real blood allocation.
