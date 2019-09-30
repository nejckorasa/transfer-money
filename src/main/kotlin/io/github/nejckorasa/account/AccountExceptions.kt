package io.github.nejckorasa.account

import io.javalin.http.HttpResponseException
import org.eclipse.jetty.http.HttpStatus

class AccountNotFoundException(accountId: Long) :
    HttpResponseException(HttpStatus.NOT_FOUND_404, "Account $accountId cannot be found")

class InsufficientBalanceException(accountId: Long) :
    HttpResponseException(HttpStatus.CONFLICT_409, "Account $accountId has insufficient balance")
