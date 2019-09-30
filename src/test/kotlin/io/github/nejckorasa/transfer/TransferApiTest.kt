package io.github.nejckorasa.transfer

import io.github.nejckorasa.BaseFunctionalTest
import io.github.nejckorasa.account.Account
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransferApiTest : BaseFunctionalTest() {

    @Test
    fun `should correctly execute transfer`() {

        val accounts = tranWrap.inTransaction {
            listOf(
                accountDao.createOrUpdate(Account(balance = BigDecimal(100))),
                accountDao.createOrUpdate(Account(balance = BigDecimal(200)))
            )
        }

        executeTransfer(fromAccount = accounts[0].id!!, toAccount = accounts[1].id!!, amount = BigDecimal(50)) {
            statusCode(201)
        }

        val fromAccount = accountDao.findOrThrow(accounts[0].id!!)
        val toAccount = accountDao.findOrThrow(accounts[1].id!!)
        assertEquals(0, BigDecimal(50).compareTo(fromAccount.balance))
        assertEquals(0, BigDecimal(250).compareTo(toAccount.balance))

        val transfers = transferDao.findAll()
        assertEquals(1, transfers.size)
        transfers[0].apply {
            assertEquals(accounts[0].id!!, fromAccountId)
            assertEquals(accounts[1].id!!, toAccountId)
            assertEquals(TransferStatus.COMPLETED, status)
            assertEquals(0, BigDecimal(50).compareTo(amount))
            assertNotNull(created)
        }
    }

    @Test
    fun `should get error for insufficient funds`() {

        val accounts = tranWrap.inTransaction {
            listOf(
                accountDao.createOrUpdate(Account(balance = BigDecimal(10))),
                accountDao.createOrUpdate(Account(balance = BigDecimal(200)))
            )
        }

        executeTransfer(fromAccount = accounts[0].id!!, toAccount = accounts[1].id!!, amount = BigDecimal(50)) {
            statusCode(409)
            body("title", Matchers.containsString("Account ${accounts[0].id!!} has insufficient balance"))
        }

        val fromAccount = accountDao.findOrThrow(accounts[0].id!!)
        val toAccount = accountDao.findOrThrow(accounts[1].id!!)
        assertEquals(0, BigDecimal(10).compareTo(fromAccount.balance))
        assertEquals(0, BigDecimal(200).compareTo(toAccount.balance))

        val transfers = transferDao.findAll()
        assertEquals(1, transfers.size)
        transfers[0].apply {
            assertEquals(accounts[0].id!!, fromAccountId)
            assertEquals(accounts[1].id!!, toAccountId)
            assertEquals(TransferStatus.FAILED, status)
            assertEquals(0, BigDecimal(50).compareTo(amount))
            assertNotNull(created)
        }
    }

    @Test
    fun `should get error for account not found`() {

        val accounts = tranWrap.inTransaction {
            listOf(
                accountDao.createOrUpdate(Account(balance = BigDecimal(10))),
                accountDao.createOrUpdate(Account(balance = BigDecimal(200)))
            )
        }

        executeTransfer(fromAccount = 999, toAccount = accounts[1].id!!, amount = BigDecimal(50)) {
            statusCode(404)
            body("title", Matchers.containsString("Account 999 cannot be found"))
        }

        val toAccount = accountDao.findOrThrow(accounts[1].id!!)
        assertEquals(0, BigDecimal(200).compareTo(toAccount.balance))

        val transfers = transferDao.findAll()
        assertEquals(1, transfers.size)
        transfers[0].apply {
            assertEquals(999, fromAccountId)
            assertEquals(accounts[1].id!!, toAccountId)
            assertEquals(TransferStatus.FAILED, status)
            assertEquals(0, BigDecimal(50).compareTo(amount))
            assertNotNull(created)
        }
    }

    @Test
    fun `should get error for negative account id`() {

        executeTransfer(fromAccount = -1, toAccount = 2, amount = BigDecimal(40)) {
            statusCode(400)
            body("title", Matchers.containsString("From account id must be positive"))
        }
    }

    @Test
    fun `should get error for negative transfer amount`() {

        executeTransfer(fromAccount = 1, toAccount = 2, amount = BigDecimal(-40)) {
            statusCode(400)
            body("title", Matchers.containsString("Transfer amount must be positive"))
        }
    }
}
