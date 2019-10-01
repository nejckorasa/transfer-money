package io.github.nejckorasa.transfer

import com.google.inject.Inject
import io.github.nejckorasa.account.AccountDao
import io.github.nejckorasa.dao.TransactionWrapper
import io.github.nejckorasa.transfer.TransferStatus.SUCCESS
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger(DbRowLockingTransferService::class.java)

open class DbRowLockingTransferService @Inject constructor(
    transferDao: TransferDao,
    private val accountDao: AccountDao,
    private val tranWrap: TransactionWrapper
) : TransferService(transferDao) {

    override fun executeTransfer(transferRequest: TransferRequest): Transfer {
        val transfer = createOrUpdate(transferRequest.toTransfer())
        tranWrap.inTransaction {
            val fromAccount = accountDao.getForUpdate(transfer.fromAccountId)
            val toAccount = accountDao.getForUpdate(transfer.toAccountId)
            accountDao.transfer(fromAccount, toAccount, transfer.amount)
        }
        logger.info("Completed transfer: ${transfer.id}")
        return createOrUpdate(transfer.apply { status = SUCCESS })
    }
}