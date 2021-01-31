package io.github.nejckorasa

import io.github.nejckorasa.account.AccountResponse
import io.github.nejckorasa.account.CreateAccountRequest
import io.github.nejckorasa.transfer.TransferRequest
import io.github.nejckorasa.transfer.TransferResponse
import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import io.restassured.response.ValidatableResponse
import java.math.BigDecimal


fun createAccount(
    createAccountRequest: CreateAccountRequest,
    then: ValidatableResponse.() -> Unit = {}
): AccountResponse = createAccountExpecting(createAccountRequest, then) Extract {
    `as`(AccountResponse::class.java)
}

fun createAccountExpecting(
    createAccountRequest: CreateAccountRequest,
    then: ValidatableResponse.() -> Unit = {}
): ValidatableResponse =
    Given {
        body(createAccountRequest)
        contentType(ContentType.JSON)
        accept(ContentType.JSON)
    } When {
        post("/accounts")
    } Then {
        then()
    }

fun getAccount(
    accountId: Long?,
    then: ValidatableResponse.() -> Unit = {}
): AccountResponse = getAccountExpecting(accountId, then) Extract {
    `as`(AccountResponse::class.java)
}

fun getAccountExpecting(
    accountId: Long?,
    then: ValidatableResponse.() -> Unit = {}
): ValidatableResponse =
    Given {
        contentType(ContentType.JSON)
        accept(ContentType.JSON)
    } When {
        get("/accounts/${accountId}")
    } Then {
        then()
    }

fun getAccounts(): Array<AccountResponse> =
    Given {
        contentType(ContentType.JSON)
        accept(ContentType.JSON)
    } When {
        get("/accounts")
    } Then {
        statusCode(200)
    } Extract {
        `as`(Array<AccountResponse>::class.java)
    }

fun getTransfers(): Array<TransferResponse> =
    Given {
        contentType(ContentType.JSON)
        accept(ContentType.JSON)
    } When {
        get("/transfers")
    } Then {
        statusCode(200)
    } Extract {
        `as`(Array<TransferResponse>::class.java)
    }

fun executeTransfer(
    sourceAccount: Long,
    destinationAccount: Long,
    amount: BigDecimal,
    then: ValidatableResponse.() -> Unit = {}
) {
    Given {
        body(TransferRequest(sourceAccount, destinationAccount, amount))
        contentType(ContentType.JSON)
        accept(ContentType.JSON)
    } When {
        post("/transfers")
    } Then {
        then()
    }
}