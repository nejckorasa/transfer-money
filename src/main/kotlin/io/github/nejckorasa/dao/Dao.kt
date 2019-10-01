package io.github.nejckorasa.dao

import javax.inject.Provider
import javax.persistence.EntityManager

abstract class Dao constructor(private val emProvider: Provider<EntityManager>) {

    fun em(): EntityManager = emProvider.get()
}