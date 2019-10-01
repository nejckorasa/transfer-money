package io.github.nejckorasa.transfer

import com.google.inject.Inject
import io.github.nejckorasa.account.AccountDao
import io.github.nejckorasa.dao.TransactionWrapper
import io.github.nejckorasa.transfer.TransferStatus.COMPLETED
import io.github.nejckorasa.transfer.TransferStatus.FAILED
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

private const val TRANSFER_LOCK_TIMEOUT = 500L
private const val TRANSFER_REQUEST_TIMEOUT = 1000L

open class ThreadLockingTransferService @Inject constructor(
    transferDao: TransferDao,
    private val accountDao: AccountDao,
    private val tranWrap: TransactionWrapper
) : TransferService(transferDao) {

    companion object {
        val logger: Logger = LoggerFactory.getLogger(this::class.java.name)
    }

    private val lock = ReentrantLock()

    override fun executeTransfer(transferRequest: TransferRequest): Transfer {
        val transfer = createOrUpdate(transferRequest.toTransfer())
        val transferSuccess = repeatUntilTrue(atMostTimes = 3, timeoutMs = TRANSFER_REQUEST_TIMEOUT) { index ->
            executeTransfer(transfer)
        }
        return createOrUpdate(transfer.apply { status = if (transferSuccess) COMPLETED else FAILED })
    }

    private fun executeTransfer(transfer: Transfer): Boolean {
        try {
            if (lock.tryLock(TRANSFER_LOCK_TIMEOUT, TimeUnit.MILLISECONDS)) {
                try {
                    tranWrap.inTransaction {
                        val fromAccount = accountDao.findOrThrow(transfer.fromAccountId)
                        val toAccount = accountDao.findOrThrow(transfer.toAccountId)
                        accountDao.transfer(fromAccount, toAccount, transfer.amount)
                        logger.info("Completed transfer: ${transfer.id}")
                    }
                    return true
                } finally {
                    lock.unlock()
                }
            }
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            throw RuntimeException(e)
        }
        return false
    }

    /**
     * Repeatedly executes the given function [action] until one of the conditions below is met:
     * - function [action] returned `true`
     * - function [action] was executed specified number of times [atMostTimes]
     * - timeout [timeoutMs] has occurred
     */
    private fun repeatUntilTrue(atMostTimes: Int, timeoutMs: Long, action: (Int) -> Boolean): Boolean {
        val stopTimeMs = System.currentTimeMillis() + timeoutMs
        for (index in 0 until atMostTimes) {
            if (action(index) || System.currentTimeMillis() > stopTimeMs) {
                return true
            }
        }
        return false
    }
}