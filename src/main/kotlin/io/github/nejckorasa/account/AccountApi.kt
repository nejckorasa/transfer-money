package io.github.nejckorasa.account

import java.math.BigDecimal

data class CreateAccountRequest(val balance: BigDecimal) {
    fun toAccount(): Account = Account(null, balance)
}

data class AccountResponse(val id: Long, val balance: BigDecimal) {
    companion object {
        fun fromAccount(account: Account): AccountResponse = AccountResponse(account.id!!, account.balance)
    }
}