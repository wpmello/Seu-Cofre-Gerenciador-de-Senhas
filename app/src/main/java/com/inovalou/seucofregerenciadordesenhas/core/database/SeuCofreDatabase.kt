package com.inovalou.seucofregerenciadordesenhas.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.inovalou.seucofregerenciadordesenhas.feature.categories.data.local.CategoryDao
import com.inovalou.seucofregerenciadordesenhas.feature.categories.data.local.CategoryEntity
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.local.PasswordDao
import com.inovalou.seucofregerenciadordesenhas.feature.passwords.data.local.PasswordEntity

@Database(
    entities = [CategoryEntity::class, PasswordEntity::class],
    version = 4,
    exportSchema = true
)
abstract class SeuCofreDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao

    abstract fun passwordDao(): PasswordDao
}
