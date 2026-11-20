# LifeLink — Manual Test Plan

Run these tests against a local MySQL 8 database and Apache Tomcat 9 deployment. Use fictional data only. Record the build/run date and result for each test during the project demonstration.

| ID | Area | Test | Expected result |
|---|---|---|---|
| T01 | Registration | Submit a valid donor registration | Account is created with PENDING status |
| T02 | Registration | Register with an email already in use | Duplicate account is rejected with a friendly message |
| T03 | Registration | Register a donor without a blood group | Validation rejects the form |
| T04 | Login | Log in with an incorrect password | Access is denied |
| T05 | Login | Log in with a pending account | Access is denied and pending status is explained |
| T06 | Login | Log in with an approved account | Session is created and dashboard opens |
| T07 | Admin | Open /admin as a donor or bank | Request is rejected with HTTP 403 |
| T08 | Admin | Approve a pending bank registration | Bank status becomes APPROVED and login is enabled |
| T09 | Admin | Reject a pending registration | Account remains unable to access protected pages |
| T10 | Inventory | Save all eight blood-group quantities | Eight inventory rows are saved for the bank |
| T11 | Inventory | Submit a negative or non-integer quantity | Server-side validation rejects the submission |
| T12 | Appointment | Donor requests a future appointment at an approved bank | Appointment is stored as PENDING |
| T13 | Appointment | Donor chooses an unapproved/nonexistent bank ID | Appointment is not created |
| T14 | Appointment | Bank approves a pending appointment | Appointment status becomes APPROVED |
| T15 | Appointment | Bank completes an approved donation | Status becomes COMPLETED and exactly one unit is added |
| T16 | Appointment | Attempt to complete the same appointment again | No additional stock is added |
| T17 | Recipient | Recipient opens bank search | Approved banks and recorded stock are displayed |
| T18 | Recipient | Recipient submits a valid blood request | Request is created with PENDING status |
| T19 | Request | Bank approves then fulfils with sufficient stock | Request becomes FULFILLED and stock decreases by requested units |
| T20 | Request | Bank attempts fulfilment without sufficient stock | Request is not fulfilled and inventory remains unchanged |
| T21 | Transfer | Sender creates a transfer to another approved bank | Transfer is created with PENDING status |
| T22 | Transfer | Receiver rejects a pending transfer | Transfer becomes REJECTED and stock is unchanged |
| T23 | Transfer | Receiver accepts with sufficient sender stock | Transfer becomes COMPLETED; sender decreases and receiver increases |
| T24 | Transfer | Receiver accepts when sender stock is insufficient | Transfer does not complete and neither inventory is changed |
| T25 | CSRF | Submit a protected POST without a valid CSRF token | Request is rejected with HTTP 403 |
| T26 | Authorization | A donor attempts to open bank-only or recipient-only routes | Request is rejected with HTTP 403 |
| T27 | Logout | Submit the sign-out form | Session is invalidated and user returns to the landing page |
| T28 | Bootstrap | Visit setup-admin after an admin exists | Setup endpoint is unavailable |

## Stock consistency checks

For a donation completion, verify the appointment transitions from APPROVED to COMPLETED and the corresponding bank/blood-group stock increases by one exactly once.

For a fulfilled recipient request, verify the request transitions from APPROVED to FULFILLED and the corresponding stock decreases by the requested quantity exactly once.

For an accepted transfer, verify the transfer transitions from PENDING to COMPLETED, the sending bank loses the requested quantity, and the receiving bank gains the same quantity.

## Build verification

From the repository root run:

```powershell
mvn clean package
```

A successful Maven build produces target/LifeLink.war. GitHub Actions performs the same Maven package build for changes pushed to main.
