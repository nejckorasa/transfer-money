package io.github.nejckorasa.account

import io.github.nejckorasa.BaseFunctionalTest
import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.math.BigDecimal
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AccountApiTest : BaseFunctionalTest() {

    @Test
    fun `should create account`() {

        val createAccountResponse: AccountResponse = Given {
            body(CreateAccountRequest(BigDecimal.ZERO))
            contentType(ContentType.JSON)
            accept(ContentType.JSON)
        } When {
            post("/accounts")
        } Then {
            statusCode(201)
        } Extract {
            `as`(AccountResponse::class.java)
        }

        accountDao.findOrThrow(createAccountResponse.id).apply {
            assertEquals(id, createAccountResponse.id)
            assertEquals(0, BigDecimal.ZERO.compareTo(balance))
        }
    }

    @Test
    fun `should get error for negative balance`() {

        Given {
            body(CreateAccountRequest(BigDecimal(-1)))
            contentType(ContentType.JSON)
            accept(ContentType.JSON)
        } When {
            post("/accounts")
        } Then {
            statusCode(400)
            body("title", containsString("Account balance must be positive"))
        }
    }

    @Test
    fun `should get error for account not found`() {

        Given {
            body(CreateAccountRequest(BigDecimal(-1)))
            contentType(ContentType.JSON)
            accept(ContentType.JSON)
        } When {
            get("/accounts/999")
        } Then {
            statusCode(404)
            body("title", containsString("Account 999 cannot be found"))
        }
    }

    @Test
    fun `should return account`() {

        val account = tranWrap.inTransaction {
            accountDao.createOrUpdate(Account(balance = BigDecimal(100)))
        }

        val accountResponse = Given {
            contentType(ContentType.JSON)
            accept(ContentType.JSON)
        } When {
            get("/accounts/${account.id}")
        } Then {
            statusCode(200)
        } Extract {
            `as`(AccountResponse::class.java)
        }

        accountResponse.apply {
            assertEquals(account.id, id)
            assertEquals(0, BigDecimal(100).compareTo(balance))
        }
    }

    @Test
    fun `should return all accounts`() {

        val accounts = tranWrap.inTransaction {
            listOf(
                accountDao.createOrUpdate(Account(balance = BigDecimal(100))),
                accountDao.createOrUpdate(Account(balance = BigDecimal(200)))
            )
        }

        val accountResponses: Array<AccountResponse> = Given {
            contentType(ContentType.JSON)
            accept(ContentType.JSON)
        } When {
            get("/accounts")
        } Then {
            statusCode(200)
        } Extract {
            `as`(Array<AccountResponse>::class.java)
        }

        accountResponses.apply {
            assertEquals(2, size)
            assertEquals(accounts[0].id, get(0).id)
            assertEquals(0, get(0).balance.compareTo(BigDecimal(100)))
            assertEquals(accounts[1].id, get(1).id)
            assertEquals(0, get(1).balance.compareTo(BigDecimal(200)))
        }
    }
}