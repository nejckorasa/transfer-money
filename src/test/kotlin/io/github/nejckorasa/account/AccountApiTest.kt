package io.github.nejckorasa.account

import io.github.nejckorasa.*
import org.hamcrest.Matchers.containsString
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.math.BigDecimal.ZERO
import kotlin.test.assertEquals

class AccountApiTest : BaseFunctionalTest() {

    @Test
    fun `should create account`() {
        val response: AccountResponse = createAccount(CreateAccountRequest(ZERO))
        accountDao.findOrThrow(response.accountId).apply {
            assertEquals(id, response.accountId)
            assertEquals(0, ZERO.compareTo(balance))
        }
    }

    @Test
    fun `should get error for negative balance`() {
        createAccountExpecting(CreateAccountRequest(BigDecimal(-1))) {
            statusCode(400)
            body("title", containsString("Account balance must be positive"))
        }
    }

    @Test
    fun `should get error for account not found`() {
        getAccountExpecting(999) {
            statusCode(404)
            body("title", containsString("Account 999 cannot be found"))
        }
    }

    @Test
    fun `should return account`() {
        val account = tranWrap.inTransaction {
            accountDao.createOrUpdate(Account(balance = BigDecimal(100)))
        }
        getAccount(account.id).apply {
            assertEquals(account.id, accountId)
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
        getAccounts().apply {
            assertEquals(2, size)
            assertEquals(accounts[0].id, get(0).accountId)
            assertEquals(0, get(0).balance.compareTo(BigDecimal(100)))
            assertEquals(accounts[1].id, get(1).accountId)
            assertEquals(0, get(1).balance.compareTo(BigDecimal(200)))
        }
    }
}