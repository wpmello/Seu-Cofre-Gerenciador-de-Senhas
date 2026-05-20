package com.inovalou.seucofregerenciadordesenhas.core.database

import androidx.room.withTransaction
import javax.inject.Inject

interface DatabaseTransactionRunner {
    suspend fun <T> runInTransaction(block: suspend () -> T): T
}

class RoomDatabaseTransactionRunner @Inject constructor(
    private val database: SeuCofreDatabase
) : DatabaseTransactionRunner {

    override suspend fun <T> runInTransaction(block: suspend () -> T): T =
        database.withTransaction {
            block()
        }
}
