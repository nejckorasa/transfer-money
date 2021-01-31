package io.github.nejckorasa.transfer

import io.github.nejckorasa.BaseFunctionalTest
import io.github.nejckorasa.account.Account
import io.github.nejckorasa.executeTransfer
import io.github.nejckorasa.transfer.TransferStatus.SUCCESS
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.concurrent.thread
import kotlin.test.assertEquals

class TransferApiConcurrentTransfersTest : BaseFunctionalTest() {

    @Test
    fun `should correctly execute concurrent transfers`() {
        val accounts = tranWrap.inTransaction {
            listOf(
                accountDao.createOrUpdate(Account(balance = BigDecimal(100))),
                accountDao.createOrUpdate(Account(balance = BigDecimal(200))),
                accountDao.createOrUpdate(Account(balance = BigDecimal(200))),

                // Accounts to execute mirror transfers on
                accountDao.createOrUpdate(Account(balance = BigDecimal(1000))),
                accountDao.createOrUpdate(Account(balance = BigDecimal(1000)))
            )
        }
        runInParallel(10) {
            executeTransfer(sourceAccount = accounts[0].id!!, destinationAccount = accounts[2].id!!, amount = BigDecimal(20))
            executeTransfer(sourceAccount = accounts[1].id!!, destinationAccount = accounts[2].id!!, amount = BigDecimal(20))

            // Mirror transfers
            executeTransfer(sourceAccount = accounts[3].id!!, destinationAccount = accounts[4].id!!, amount = BigDecimal(50))
            executeTransfer(sourceAccount = accounts[4].id!!, destinationAccount = accounts[3].id!!, amount = BigDecimal(50))
        }

        val sourceAccount1 = accountDao.findOrThrow(accounts[0].id!!)
        val sourceAccount2 = accountDao.findOrThrow(accounts[1].id!!)
        val destinationAccount = accountDao.findOrThrow(accounts[2].id!!)

        assertEquals(0, BigDecimal(0).compareTo(sourceAccount1.balance))
        assertEquals(0, BigDecimal(0).compareTo(sourceAccount2.balance))
        assertEquals(0, BigDecimal(500).compareTo(destinationAccount.balance))

        val mirrorTransferAccount1 = accountDao.findOrThrow(accounts[3].id!!)
        val mirrorTransferAccount2 = accountDao.findOrThrow(accounts[4].id!!)

        assertEquals(0, BigDecimal(1000).compareTo(mirrorTransferAccount1.balance))
        assertEquals(0, BigDecimal(1000).compareTo(mirrorTransferAccount2.balance))

        val transfers = transferDao.findAll()
        assertEquals(40, transfers.size)
        assertEquals(35, transfers.filter { it.status == SUCCESS }.size)
        assertEquals(5, transfers.filter { it.status != SUCCESS }.size)
    }

    private fun runInParallel(numOfThreads: Int, action: () -> Unit) {
        1.rangeTo(numOfThreads)
            .map { thread { action() } }
            .forEach { it.join() }
    }
}
