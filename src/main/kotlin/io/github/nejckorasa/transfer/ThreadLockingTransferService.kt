package io.github.nejckorasa.transfer

import com.google.inject.Inject
import io.github.nejckorasa.account.AccountDao
import io.github.nejckorasa.dao.TransactionWrapper
import io.github.nejckorasa.transfer.TransferStatus.SUCCESS
import io.github.nejckorasa.transfer.TransferStatus.FAILED
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

private const val TRANSFER_LOCK_TIMEOUT_MS = 100L
private const val TRANSFER_REQUEST_TIMEOUT_MS = 1000L

private val logger: Logger = LoggerFactory.getLogger(ThreadLockingTransferService::class.java)

open class ThreadLockingTransferService @Inject constructor(
    transferDao: TransferDao,
    private val accountDao: AccountDao,
    private val tranWrap: TransactionWrapper
) : TransferService(transferDao) {

    private val lock = ReentrantLock()

    override fun executeTransfer(transferRequest: TransferRequest): Transfer {
        val transfer = createOrUpdate(transferRequest.toTransfer())
        val transferSuccess = repeatUntil(timeoutMs = TRANSFER_REQUEST_TIMEOUT_MS) {
            executeTransfer(transfer)
        }
        return createOrUpdate(transfer.apply { status = if (transferSuccess) SUCCESS else FAILED })
    }

    private fun executeTransfer(transfer: Transfer): Boolean {
        try {
            if (lock.tryLock(TRANSFER_LOCK_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
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
     * Repeatedly executes the given function [action] until one of the following conditions is met:
     *
     * - function [action] returns `true`
     * - timeout [timeoutMs] is reached
     *
     * @return `true` or `false` whether [action] was successful
     */
    private fun repeatUntil(timeoutMs: Long, action: () -> Boolean): Boolean {
        val stopTimeMs = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < stopTimeMs) {
            if (action()) return true
        }
        return false
    }
}