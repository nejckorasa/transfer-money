package io.github.nejckorasa.transfer

import io.github.nejckorasa.BaseFunctionalTest
import io.github.nejckorasa.account.Account
import io.github.nejckorasa.executeTransfer
import io.github.nejckorasa.getAccount
import io.github.nejckorasa.getTransfers
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.math.BigDecimal
import java.math.BigDecimal.ONE
import java.math.BigDecimal.ZERO
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
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

        executeTransfer(sourceAccount = accounts[0].id!!, destinationAccount = accounts[1].id!!, amount = BigDecimal(50)) {
            statusCode(201)
        }

        val sourceAccount = accountDao.findOrThrow(accounts[0].id!!)
        val destinationAccount = accountDao.findOrThrow(accounts[1].id!!)
        assertEquals(0, BigDecimal(50).compareTo(sourceAccount.balance))
        assertEquals(0, BigDecimal(250).compareTo(destinationAccount.balance))

        val transfers = transferDao.findAll()
        assertEquals(1, transfers.size)
        transfers[0].apply {
            assertEquals(accounts[0].id!!, sourceAccountId)
            assertEquals(accounts[1].id!!, destinationAccountId)
            assertEquals(TransferStatus.SUCCESS, status)
            assertEquals(0, BigDecimal(50).compareTo(amount))
            assertNotNull(createdAt)
        }
    }

    @Test
    fun `should return all transfers`() {
        val accounts = tranWrap.inTransaction {
            listOf(
                accountDao.createOrUpdate(Account(balance = BigDecimal(100))),
                accountDao.createOrUpdate(Account(balance = BigDecimal(200)))
            )
        }

        executeTransfer(sourceAccount = accounts[0].id!!, destinationAccount = accounts[1].id!!, amount = BigDecimal(10))
        executeTransfer(sourceAccount = accounts[0].id!!, destinationAccount = accounts[1].id!!, amount = BigDecimal(20))

        val transfers = getTransfers()
        assertEquals(2, transfers.size)
        transfers[0].apply {
            assertEquals(accounts[0].id!!, sourceAccountId)
            assertEquals(accounts[1].id!!, destinationAccountId)
            assertEquals(TransferStatus.SUCCESS, status)
            assertEquals(0, BigDecimal(10).compareTo(amount))
            assertNotNull(createdAt)
        }
        transfers[1].apply {
            assertEquals(accounts[0].id!!, sourceAccountId)
            assertEquals(accounts[1].id!!, destinationAccountId)
            assertEquals(TransferStatus.SUCCESS, status)
            assertEquals(0, BigDecimal(20).compareTo(amount))
            assertNotNull(createdAt)
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

        executeTransfer(sourceAccount = accounts[0].id!!, destinationAccount = accounts[1].id!!, amount = BigDecimal(50)) {
            statusCode(409)
            body("title", Matchers.containsString("Account ${accounts[0].id!!} has insufficient balance"))
        }

        val sourceAccount = getAccount(accounts[0].id)
        val destinationAccount = getAccount(accounts[1].id)
        assertEquals(0, BigDecimal(10).compareTo(sourceAccount.balance))
        assertEquals(0, BigDecimal(200).compareTo(destinationAccount.balance))

        val transfers = getTransfers()
        assertEquals(1, transfers.size)
        transfers[0].apply {
            assertEquals(accounts[0].id!!, sourceAccountId)
            assertEquals(accounts[1].id!!, destinationAccountId)
            assertNotEquals(TransferStatus.SUCCESS, status)
            assertEquals(0, BigDecimal(50).compareTo(amount))
            assertNotNull(createdAt)
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

        executeTransfer(sourceAccount = 999, destinationAccount = accounts[1].id!!, amount = BigDecimal(50)) {
            statusCode(404)
            body("title", Matchers.containsString("Account 999 cannot be found"))
        }

        val destinationAccount = getAccount(accounts[1].id)
        assertEquals(0, BigDecimal(200).compareTo(destinationAccount.balance))

        val transfers = getTransfers()
        assertEquals(1, transfers.size)
        transfers[0].apply {
            assertEquals(999, sourceAccountId)
            assertEquals(accounts[1].id!!, destinationAccountId)
            assertNotEquals(TransferStatus.SUCCESS, status)
            assertEquals(0, BigDecimal(50).compareTo(amount))
            assertNotNull(createdAt)
        }
    }

    @Test
    fun `should get error for negative transfer amount`() =
        executeTransfer(sourceAccount = 1, destinationAccount = 2, amount = ZERO) {
            statusCode(400)
            body("title", Matchers.containsString("Transfer amount must be greater than 0"))
        }

    @Test
    fun `should get error for same source and destination account`() =
        executeTransfer(sourceAccount = 1, destinationAccount = 1, amount = ONE) {
            statusCode(400)
            body("title", Matchers.containsString("Source and destination account must be different"))
        }
}
