package io.github.nejckorasa.account

import com.google.inject.Inject
import com.google.inject.persist.Transactional
import java.math.BigDecimal

open class AccountService @Inject constructor(private val accountDao: AccountDao) {

    fun find(accountId: Long) = accountDao.findOrThrow(accountId)

    fun findAll() = accountDao.findAll()

    @Transactional
    open fun create(createAccountRequest: CreateAccountRequest): Long =
        accountDao.createOrUpdate(createAccountRequest.toAccount()).id!!

    @Transactional
    open fun transfer(from: Account, to: Account, amount: BigDecimal) =
        accountDao.transfer(from, to, amount)
}