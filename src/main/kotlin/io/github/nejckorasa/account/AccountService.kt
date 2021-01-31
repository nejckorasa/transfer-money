package io.github.nejckorasa.account

import com.google.inject.Inject
import com.google.inject.persist.Transactional

open class AccountService @Inject constructor(private val accountDao: AccountDao) {

    fun find(accountId: Long) = accountDao.findOrThrow(accountId)

    fun findAll() = accountDao.findAll()

    @Transactional
    open fun create(createAccountRequest: CreateAccountRequest): Account =
        accountDao.createOrUpdate(createAccountRequest.destinationAccount())
}