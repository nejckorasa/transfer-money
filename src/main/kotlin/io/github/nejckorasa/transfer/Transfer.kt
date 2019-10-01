package io.github.nejckorasa.transfer

import org.hibernate.annotations.CreationTimestamp
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "transfers")
data class Transfer(

    @Id
    @Column(name = "transfer_id")
    @GeneratedValue
    val id: Long? = null,

    @Column
    @CreationTimestamp
    val created: LocalDateTime? = null,

    @Column
    val fromAccountId: Long,

    @Column
    val toAccountId: Long,

    @Column
    val amount: BigDecimal,

    @Column
    @Enumerated(EnumType.STRING)
    var status: TransferStatus

)