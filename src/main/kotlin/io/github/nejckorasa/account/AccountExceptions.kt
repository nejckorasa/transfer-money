package io.github.nejckorasa.account

import io.javalin.http.HttpResponseException
import org.eclipse.jetty.http.HttpStatus.CONFLICT_409
import org.eclipse.jetty.http.HttpStatus.NOT_FOUND_404

class AccountNotFoundException(accountId: Long) :
    HttpResponseException(NOT_FOUND_404, "Account $accountId cannot be found")

class InsufficientBalanceException(accountId: Long) :
    HttpResponseException(CONFLICT_409, "Account $accountId has insufficient balance")
