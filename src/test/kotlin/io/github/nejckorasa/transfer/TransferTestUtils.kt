package io.github.nejckorasa.transfer

import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import io.restassured.response.ValidatableResponse
import java.math.BigDecimal


fun executeTransfer(
    fromAccount: Long,
    toAccount: Long,
    amount: BigDecimal,
    then: ValidatableResponse.() -> Unit = {}
) {
    Given {
        body(TransferRequest(fromAccount, toAccount, amount))
        contentType(ContentType.JSON)
        accept(ContentType.JSON)
    } When {
        post("/transfers")
    } Then {
        then()
    }
}
