package io.github.nejckorasa.transfer

import java.math.BigDecimal
import java.time.format.DateTimeFormatter

data class TransferRequest(val fromAccountId: Long, val toAccountId: Long, val amount: BigDecimal) {
    fun toTransfer(): Transfer = Transfer(
        fromAccountId = fromAccountId,
        toAccountId = toAccountId,
        amount = amount,
        status = TransferStatus.NEW
    )
}

data class TransferResponse(
    val id: Long,
    val fromAccountId: Long,
    val toAccountId: Long,
    val amount: BigDecimal,
    val created: String,
    val status: TransferStatus
) {
    companion object {
        fun fromTransfer(transfer: Transfer): TransferResponse = TransferResponse(
            id = transfer.id!!,
            fromAccountId = transfer.fromAccountId,
            toAccountId = transfer.toAccountId,
            created = transfer.created!!.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            amount = transfer.amount,
            status = transfer.status
        )
    }
}