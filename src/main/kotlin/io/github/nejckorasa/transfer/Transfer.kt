package io.github.nejckorasa.transfer

import org.hibernate.annotations.CreationTimestamp
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.persistence.*
import javax.persistence.EnumType.STRING

@Entity
@Table(name = "transfers")
data class Transfer(

    @Id
    @Column(name = "transfer_id")
    @GeneratedValue
    val id: Long? = null,

    @Column
    @CreationTimestamp
    val createdAt: LocalDateTime? = null,

    @Column
    val sourceAccountId: Long,

    @Column
    val destinationAccountId: Long,

    @Column
    val amount: BigDecimal,

    @Column
    @Enumerated(STRING)
    var status: TransferStatus
) {
    fun getAccounts() = listOf(sourceAccountId, destinationAccountId)
}