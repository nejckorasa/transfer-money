# transfer-money

[![Build Status](https://travis-ci.com/nejckorasa/transfer-money.svg?token=pfWZRfNyzeRf4kWWpnbs&branch=master)](https://travis-ci.com/nejckorasa/transfer-money)

Sample app for money transfers between accounts. It supports account creation with initial balance and issuing money transfers between accounts. 

It is build using [Javalin](https://javalin.io/) with [Guice](https://github.com/google/guice) and [Kotlin](https://kotlinlang.org). [Hibernate](https://hibernate.org/orm/) with [H2](https://www.h2database.com/html/main.html) in-memory database.

Tested with [JUnit5](https://junit.org/junit5/) and [REST-assured](http://rest-assured.io/)

It includes examples for both [DB locking](src/main/kotlin/io/github/nejckorasa/transfer/DbLockingTransferService.kt) and [Thread locking](src/main/kotlin/io/github/nejckorasa/transfer/ThreadLockingTransferService.kt).

## Running

Server starts on: `http://localhost:7000`

To start the server:

```bash
./mvnw compile exec:java
```

Or build jar and run it:

```bash
./mvnw package && java -jar target/transfer-money-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## REST API
<details>
  <summary>Click to expand!</summary>

### Accounts

- `GET /api/accounts`  
  Get all accounts  
  Successful response: `200`  
  Example response:
  ```json
  [
    {
      "accountId": 1,
      "balance": 200.00
    },
    {
      "accountId": 2,
      "balance": 100.00
    }
  ]
  ```
  
- `GET /api/accounts/:id`  
  get account by id  
  Successful response: `200`  
  Example response:
  ```json
  {
    "accountId": 1,
    "balance": 200.00
  }
  ```
- `POST /api/accounts`  
  Create account  
  Request body:
  ```json
  {
    "balance": 200.00
  }
  ```
  Successful response: `201`  
  Example response:
  ```json
  {
    "accountId": 1,
    "balance": 200.00
  }
  ```
  
### Transfers

- `GET /api/transfers`  
  Get all transfers  
  Successful response: `200`  
  Example response:
  ```json
  [
    {
      "transferId": 1,
      "sourceAccountId": 1,
      "destinationAccountId": 2,
      "amount": 50.00,
      "createdAt": "2019-09-30T20:57:02.15",
      "status": "SUCCESS"
    },
    {
      "transferId": 2,
      "sourceAccountId": 1,
      "destinationAccountId": 2,
      "amount": 50.00,
      "createdAt": "2019-09-30T20:57:03.102",
      "status": "FAILED"
    },
    {
      "transferId": 3,
      "sourceAccountId": 1,
      "destinationAccountId": 2,
      "amount": 50.00,
      "createdAt": "2019-09-30T20:57:05.397",
      "status": "REQUESTED"
    }
  ]
  ```

- `POST /api/transfers`  
  Create transfer    
  Request body:
  ```json
  {
    "sourceAccountId": 1,
    "destinationAccountId": 2,
    "amount": 50.0
  }
  ```
  Successful response: `201`  
  Example response:
  ```json
  {
    "transferId": 1,
    "sourceAccountId": 1,
    "destinationAccountId": 2,
    "amount": 50.00,
    "createdAt": "2019-09-30T20:57:02.15",
    "status": "SUCCESS"
  }
  ```
</details>