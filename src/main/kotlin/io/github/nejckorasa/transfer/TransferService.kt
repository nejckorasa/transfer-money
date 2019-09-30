package io.github.nejckorasa.transfer

import com.google.inject.Inject
import io.github.nejckorasa.account.AccountDao
import io.github.nejckorasa.dao.TransactionWrapper
import io.github.nejckorasa.transfer.TransferStatus.COMPLETED
import io.github.nejckorasa.transfer.TransferStatus.FAILED
import org.slf4j.Logger
import org.slf4j.LoggerFactory

open class TransferService @Inject constructor(
    private val transferDao: TransferDao,
    private val accountDao: AccountDao,
    private val tranWrap: TransactionWrapper
) {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(this::class.java.name)
    }

    fun findAll(): List<Transfer> = transferDao.findAll()

    fun executeTransfer(transferRequest: TransferRequest, onCompletedTransfer: () -> Unit = {}) {
        lateinit var exception: Exception

        val transfer = createOrUpdate(transferRequest.toTransfer())
        val transferStatus = try {
            tranWrap.inTransaction {
                val fromAccount = accountDao.getForUpdate(transfer.fromAccountId)
                val toAccount = accountDao.getForUpdate(transfer.toAccountId)
                accountDao.transfer(fromAccount, toAccount, transfer.amount)
                onCompletedTransfer()
            }
            logger.info("Completed transfer: ${transfer.id} of: ${transfer.amount}")
            COMPLETED
        } catch (ex: Exception) {
            logger.error("Failed transfer: ${transfer.id}", ex)
            exception = ex
            FAILED
        }

        createOrUpdate(transfer.apply { status = transferStatus })
        if (transferStatus == FAILED) throw exception
    }

    private fun createOrUpdate(transfer: Transfer) = tranWrap.inTransaction {
        transferDao.createOrUpdate(transfer)
    }
}