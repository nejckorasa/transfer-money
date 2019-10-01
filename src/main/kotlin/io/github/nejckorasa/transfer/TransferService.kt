package io.github.nejckorasa.transfer

import com.google.inject.persist.Transactional

abstract class TransferService(private val transferDao: TransferDao) {

    abstract fun executeTransfer(transferRequest: TransferRequest): Transfer

    fun findAll(): List<Transfer> = transferDao.findAll()

    @Transactional
    open fun createOrUpdate(transfer: Transfer) = transferDao.createOrUpdate(transfer)
}
