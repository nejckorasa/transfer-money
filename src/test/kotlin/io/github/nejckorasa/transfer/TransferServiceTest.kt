package io.github.nejckorasa.transfer

import io.github.nejckorasa.BaseFunctionalTest
import io.github.nejckorasa.account.Account
import io.github.nejckorasa.runConcurrently
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.math.BigDecimal
import kotlin.concurrent.thread
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransferServiceTest : BaseFunctionalTest() {

    @Test
    fun `should only complete first concurrent transaction`() {

        val accounts = tranWrap.inTransaction {
            listOf(
                accountDao.createOrUpdate(Account(balance = BigDecimal(100))),
                accountDao.createOrUpdate(Account(balance = BigDecimal(200)))
            )
        }

        runConcurrently(5) {
            transferService.executeTransfer(
                TransferRequest(
                    fromAccountId = accounts[0].id!!,
                    toAccountId = accounts[1].id!!,
                    amount = BigDecimal(20)
                ),
                onCompletedTransfer = {
                    Thread.sleep(4000)
                })
        }

        val transfers = transferDao.findAll()
        assertEquals(5, transfers.size)
        val account = accountDao.findOrThrow(accounts[0].id!!)
        assertEquals(0, BigDecimal(80).compareTo(account.balance))
    }
}