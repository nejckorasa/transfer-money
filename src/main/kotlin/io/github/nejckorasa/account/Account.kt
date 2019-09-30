package io.github.nejckorasa.account

import java.math.BigDecimal
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
    fun deposit(amount: BigDecimal) {
        this.balance += amount
    }

    fun withdraw(amount: BigDecimal) {
        if (balance < amount) throw InsufficientBalanceException(accountId = id!!)
        this.balance -= amount
    }
}