# Contributing to LifeLink

Thanks for your interest in improving LifeLink!

## How to contribute

1. Fork the repository.
2. Create a branch for your change (for example, `feature/better-inventory-validation`).
3. Make a focused change.
4. Run `mvn clean package` and test the affected workflow locally when possible.
5. Update documentation when setup, configuration, or behavior changes.
6. Open a pull request with a clear summary and testing notes.

## Contribution guidelines

- Keep changes focused and explain why they are needed.
- Follow the existing Java, JSP, SQL and CSS conventions.
- Use prepared statements for SQL and preserve role checks, CSRF protection and password hashing.
- Treat inventory changes as transactional operations.
- Do not commit credentials, local configuration, database dumps, real personal data, or generated build output.
- Use fictional data for examples and screenshots.
- Be clear about anything that has not been tested.

## Before opening a pull request

- [ ] `mvn clean package` succeeds.
- [ ] Relevant workflows have been tested, or untested areas are clearly identified.
- [ ] Documentation and database scripts are updated if needed.
- [ ] No secrets or personal data are included.
