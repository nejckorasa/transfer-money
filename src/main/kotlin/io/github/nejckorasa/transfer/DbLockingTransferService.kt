package io.github.nejckorasa.transfer

import com.google.inject.Inject
import io.github.nejckorasa.account.AccountDao
import io.github.nejckorasa.dao.TransactionWrapper
import io.github.nejckorasa.transfer.TransferStatus.SUCCESS
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger(ThreadLockingTransferService::class.java)

open class DbLockingTransferService @Inject constructor(
    transferDao: TransferDao,
    private val accountDao: AccountDao,
    private val tranWrap: TransactionWrapper
) : TransferService(transferDao) {

    override fun makeTransfer(transferRequest: TransferRequest): Transfer {
        val transfer = createOrUpdate(transferRequest.toTransfer())
        tranWrap.inTransaction {
            val accounts = transfer.getAccounts().sorted().map { it to accountDao.getForUpdate(it) }.toMap()
            accountDao.makeTransfer(
                accounts[transfer.sourceAccountId]!!,
                accounts[transfer.destinationAccountId]!!,
                transfer.amount
            )
            logger.info("Completed transfer: ${transfer.id}")
        }
        return createOrUpdate(transfer.apply { status = SUCCESS })
    }
}