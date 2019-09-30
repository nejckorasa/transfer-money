package io.github.nejckorasa.transfer


import com.google.inject.Inject
import io.github.nejckorasa.dao.Dao
import javax.inject.Provider
import javax.persistence.EntityManager

class TransferDao @Inject constructor(emProvider: Provider<EntityManager>) : Dao(emProvider) {

    fun findAll(): List<Transfer> {
        return em().createQuery("select t from Transfer t order by created", Transfer::class.java).resultList
    }

    fun createOrUpdate(transfer: Transfer): Transfer {
        return em().merge(transfer)
    }
}