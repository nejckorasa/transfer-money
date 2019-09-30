package io.github.nejckorasa.account

import com.google.inject.Inject
import io.github.nejckorasa.dao.Dao
import java.math.BigDecimal
import javax.inject.Provider
import javax.persistence.EntityManager
import javax.persistence.LockModeType
import javax.persistence.TypedQuery

open class AccountDao @Inject constructor(emProvider: Provider<EntityManager>) : Dao(emProvider) {

    fun findOrThrow(accountId: Long): Account {
        return queryAccount(accountId)
            .resultStream
            .findFirst()
            .orElseThrow { AccountNotFoundException(accountId) }
    }

    fun getForUpdate(accountId: Long): Account {
        return queryAccount(accountId)
            .setLockMode(LockModeType.PESSIMISTIC_WRITE)
            .resultStream
            .findFirst()
            .orElseThrow { AccountNotFoundException(accountId) }
    }

    fun findAll(): List<Account> {
        return em().createQuery("select a from Account a order by a.id", Account::class.java).resultList
    }

    fun createOrUpdate(account: Account): Account {
        return em().merge(account)
    }

    fun transfer(from: Account, to: Account, amount: BigDecimal) {
        createOrUpdate(from.apply { withdraw(amount) })
        createOrUpdate(to.apply { deposit(amount) })
    }

    private fun queryAccount(accountId: Long): TypedQuery<Account> = em()
        .createQuery("select a from Account a where a.id = :id", Account::class.java)
        .setParameter("id", accountId)
}
