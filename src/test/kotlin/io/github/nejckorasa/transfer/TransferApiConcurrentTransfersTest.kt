package io.github.nejckorasa.transfer

import io.github.nejckorasa.BaseFunctionalTest
import io.github.nejckorasa.account.Account
import io.restassured.http.ContentType
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import io.restassured.response.ValidatableResponse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.math.BigDecimal
import kotlin.concurrent.thread
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransferApiConcurrentTransfersTest : BaseFunctionalTest() {

    @Test
    fun `should correctly execute concurrent transfers`() {

        val accounts = tranWrap.inTransaction {
            listOf(
                accountDao.createOrUpdate(Account(balance = BigDecimal(100))),
                accountDao.createOrUpdate(Account(balance = BigDecimal(200))),
                accountDao.createOrUpdate(Account(balance = BigDecimal(200)))
            )
        }

        runConcurrently(10) {
            executeTransfer(fromAccount = accounts[0].id!!, toAccount = accounts[1].id!!, amount = BigDecimal(20))
            executeTransfer(fromAccount = accounts[2].id!!, toAccount = accounts[1].id!!, amount = BigDecimal(20))
        }

        val fromAccount1 = accountDao.findOrThrow(accounts[0].id!!)
        val fromAccount2 = accountDao.findOrThrow(accounts[2].id!!)
        val toAccount = accountDao.findOrThrow(accounts[1].id!!)

        assertEquals(0, BigDecimal(0).compareTo(fromAccount1.balance))
        assertEquals(0, BigDecimal(0).compareTo(fromAccount2.balance))
        assertEquals(0, BigDecimal(500).compareTo(toAccount.balance))

        val transfers = transferDao.findAll()
        assertEquals(20, transfers.size)
        assertEquals(15, transfers.filter { it.status == TransferStatus.SUCCESS }.size)
        assertEquals(5, transfers.filter { it.status != TransferStatus.SUCCESS }.size)
    }

    private fun executeTransfer(
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

    private fun runConcurrently(numOfThreads: Int, action: () -> Unit) {
        1.rangeTo(numOfThreads)
            .map { thread { action() } }
            .forEach { it.join() }
    }
}
