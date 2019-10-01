package io.github.nejckorasa

import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.persist.PersistService
import io.github.nejckorasa.account.AccountDao
import io.github.nejckorasa.dao.TransactionWrapper
import io.github.nejckorasa.transfer.TransferDao
import io.javalin.Javalin
import io.restassured.RestAssured
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import javax.persistence.EntityManager

open class BaseFunctionalTest(private val port: Int = 7000) {

    private lateinit var injector: Injector
    private lateinit var javalin: Javalin

    private val entityManager: EntityManager by lazy { injector.getInstance(EntityManager::class.java) }

    val accountDao: AccountDao by lazy { injector.getInstance(AccountDao::class.java) }
    val transferDao: TransferDao by lazy { injector.getInstance(TransferDao::class.java) }
    val tranWrap: TransactionWrapper by lazy { injector.getInstance(TransactionWrapper::class.java) }

    @BeforeAll
    fun setUp() {
        injector = Guice.createInjector(TransferMoneyModule()).apply {
            getInstance(PersistService::class.java).start()
            javalin = getInstance(Starter::class.java).start(port)
        }
        RestAssured.baseURI = "http://localhost/api"
        RestAssured.port = port
    }

    @AfterAll
    fun tearDown() {
        javalin.stop()
    }

    @AfterEach
    fun cleanDatabase() {
        entityManager.apply {
            transaction.begin()
            createQuery("delete from Account").executeUpdate()
            createQuery("delete from Transfer ").executeUpdate()
            transaction.commit()
        }
    }
}
