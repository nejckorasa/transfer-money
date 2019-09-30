# transfer-money

[![Build Status](https://travis-ci.com/nejckorasa/transfer-money.svg?token=pfWZRfNyzeRf4kWWpnbs&branch=master)](https://travis-ci.com/nejckorasa/transfer-money)

Sample app for money transfers between accounts

It supports account creation with initial balance and issuing money transfers between accounts. 

For the sake of this sample data is stored in in-memory database. That can be changed if needed.

It is build using:

- [Javalin](https://javalin.io/) web framework with [Guice](https://github.com/google/guice) and [Kotlin](https://kotlinlang.org)
- [Hibernate](https://hibernate.org/orm/) with [H2](https://www.h2database.com/html/main.html) in memory database

And tested with:

- [JUnit5](https://junit.org/junit5/)
- [REST-assured](http://rest-assured.io/)


## Running

Server starts on: `http://localhost:7000`

To start the server:

```sh
mvn compile exec:java
```

Or build jar and run it:

```sh
mvn package

java -jar target/transfer-money-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## REST API

### Accounts

- `GET /api/accounts`: get all accounts

  Successful response: `200`
  
  Example response:
  ```json
  [
    {
      "id": 1,
      "balance": 200.00
    },
    {
      "id": 2,
      "balance": 100.00
    }
  ]
  ```
- `GET /api/accounts/:id`: get account by id

  Successful response: `200`
  
  Example response:
  ```json
  {
    "id": 1,
    "balance": 200.00
  }
  ```
- `POST /api/accounts`: create account

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
    "id": 1,
    "balance": 200.00
  }
  ```
  
### Transfers

- `GET /api/transfers`: get all transfers

  Successful response: `200`
  
  Example response:
  ```json
  [
    {
      "id": 1,
      "fromAccountId": 1,
      "toAccountId": 2,
      "amount": 50.00,
      "created": "2019-09-30T20:57:02.15",
      "status": "COMPLETED"
    },
    {
      "id": 2,
      "fromAccountId": 1,
      "toAccountId": 2,
      "amount": 50.00,
      "created": "2019-09-30T20:57:03.102",
      "status": "FAILED"
    },
    {
      "id": 3,
      "fromAccountId": 1,
      "toAccountId": 2,
      "amount": 50.00,
      "created": "2019-09-30T20:57:05.397",
      "status": "PENDING"
    }
  ]
  ```

- `POST /api/transfers`: create transfer

  Description: Transfer provided amount from one account to another

  Request body:

  ```json
  {
    "fromAccountId": 1,
    "toAccountId": 2,
    "amount": 50.0
  }
  ```
  
  Successful response: `201`
  
  Example response:
  ```json
  {
    "id": 1,
    "fromAccountId": 1,
    "toAccountId": 2,
    "amount": 50.00,
    "created": "2019-09-30T20:57:02.15",
    "status": "COMPLETED"
  }
  ```
