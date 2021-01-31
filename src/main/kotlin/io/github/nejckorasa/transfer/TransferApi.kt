package io.github.nejckorasa.transfer

import io.github.nejckorasa.transfer.TransferStatus.REQUESTED
import java.math.BigDecimal
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME

data class TransferRequest(val sourceAccountId: Long, val destinationAccountId: Long, val amount: BigDecimal) {
    fun toTransfer(): Transfer = Transfer(
        sourceAccountId = sourceAccountId,
        destinationAccountId = destinationAccountId,
        amount = amount,
        status = REQUESTED
    )
}

data class TransferResponse(
    val transferId: Long,
    val sourceAccountId: Long,
    val destinationAccountId: Long,
    val amount: BigDecimal,
    val createdAt: String,
    val status: TransferStatus
) {
    companion object {
        fun fromTransfer(transfer: Transfer): TransferResponse = TransferResponse(
            transferId = transfer.id!!,
            sourceAccountId = transfer.sourceAccountId,
            destinationAccountId = transfer.destinationAccountId,
            createdAt = transfer.createdAt!!.format(ISO_LOCAL_DATE_TIME),
            amount = transfer.amount,
            status = transfer.status
        )
    }
}