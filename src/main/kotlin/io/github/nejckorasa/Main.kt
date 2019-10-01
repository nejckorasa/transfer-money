package io.github.nejckorasa

import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.persist.PersistService
import com.google.inject.persist.jpa.JpaPersistModule
import io.github.nejckorasa.account.AccountDao
import io.github.nejckorasa.account.AccountService
import io.github.nejckorasa.dao.TransactionWrapper
import io.github.nejckorasa.transfer.ThreadLockingTransferService
import io.github.nejckorasa.transfer.TransferDao
import io.github.nejckorasa.transfer.TransferService

fun main() {
    Guice.createInjector(TransferMoneyModule()).apply {
        getInstance(PersistService::class.java).start()
        getInstance(Starter::class.java).start(7000)
    }
}

class TransferMoneyModule : AbstractModule() {
    override fun configure() {
        install(JpaPersistModule("money-transfer"))
        bind(Starter::class.java).asEagerSingleton()
        bind(TransactionWrapper::class.java).asEagerSingleton()
        bind(AccountDao::class.java).asEagerSingleton()
        bind(AccountService::class.java).asEagerSingleton()
        bind(TransferDao::class.java).asEagerSingleton()
        bind(TransferService::class.java).to(ThreadLockingTransferService::class.java)
    }
}