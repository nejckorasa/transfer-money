package io.github.nejckorasa.account

import java.math.BigDecimal

data class CreateAccountRequest(val balance: BigDecimal) {
    fun destinationAccount(): Account = Account(null, balance)
}

data class AccountResponse(val accountId: Long, val balance: BigDecimal) {
    companion object {
        fun sourceAccount(account: Account): AccountResponse = AccountResponse(account.id!!, account.balance)
    }
}