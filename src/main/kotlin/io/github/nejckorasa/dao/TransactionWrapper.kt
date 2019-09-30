package io.github.nejckorasa.dao

import com.google.inject.persist.Transactional

open class TransactionWrapper {

    @Transactional
    open fun <R> inTransaction(block: () -> R): R = block()
}