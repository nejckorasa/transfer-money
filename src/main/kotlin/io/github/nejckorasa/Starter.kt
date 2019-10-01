package io.github.nejckorasa

import com.google.inject.Inject
import io.github.nejckorasa.account.AccountResponse
import io.github.nejckorasa.account.AccountService
import io.github.nejckorasa.account.CreateAccountRequest
import io.github.nejckorasa.transfer.TransferRequest
import io.github.nejckorasa.transfer.TransferResponse
import io.github.nejckorasa.transfer.TransferService
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import org.eclipse.jetty.http.HttpStatus.CREATED_201
import java.math.BigDecimal

class Starter @Inject constructor(
    private val accountService: AccountService,
    private val transferService: TransferService
) {
    fun start(port: Int): Javalin {
        return Javalin.create()
            .routes { buildRoutes() }
            .start(port)
    }

    private fun buildRoutes() {
        path("api") {
            path("accounts") {
                get { ctx ->
                    ctx.json(accountService.findAll().map { AccountResponse.fromAccount(it) })
                }
                get(":accountId") { ctx ->
                    val accountId = ctx.pathParam("accountId").toLong()
                    val account = accountService.find(accountId)
                    ctx.json(AccountResponse.fromAccount(account))
                }
                post { ctx ->
                    val createAccountRequest = ctx.bodyValidator<CreateAccountRequest>()
                        .check({ it.balance >= BigDecimal.ZERO }, "Account balance must be positive")
                        .get()

                    val account = accountService.create(createAccountRequest)
                    ctx.json(AccountResponse.fromAccount(account))
                    ctx.status(CREATED_201)
                }
            }
            path("transfers") {
                get { ctx ->
                    val transfers = transferService.findAll()
                    ctx.json(transfers.map { TransferResponse.fromTransfer(it) })
                }
                post { ctx ->
                    val transferRequest = ctx.bodyValidator<TransferRequest>()
                        .check({ it.amount >= BigDecimal.ZERO }, "Transfer amount must be positive")
                        .check({ it.fromAccountId > 0 }, "From account id must be positive")
                        .check({ it.toAccountId > 0 }, "To account id must be positive")
                        .get()

                    val transfer = transferService.executeTransfer(transferRequest)
                    ctx.status(CREATED_201)
                    ctx.json(TransferResponse.fromTransfer(transfer))
                }
            }
        }
    }
}