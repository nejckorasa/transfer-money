package io.github.nejckorasa.account

import java.math.BigDecimal
import java.math.BigDecimal.ZERO
import javax.persistence.*

@Entity
@Table(name = "accounts")
data class Account(

    @Id
    @Column(name = "account_id")
    @GeneratedValue
    val id: Long? = null,

    @Column
    var balance: BigDecimal
) {
    fun adjustBalance(amount: BigDecimal) {
        val newBalance = balance + amount
        if (newBalance < ZERO) throw InsufficientBalanceException(accountId = id!!)
        this.balance = newBalance
    }
}