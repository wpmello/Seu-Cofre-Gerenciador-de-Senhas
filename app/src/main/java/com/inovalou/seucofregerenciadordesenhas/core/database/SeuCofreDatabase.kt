package com.inovalou.seucofregerenciadordesenhas.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.inovalou.seucofregerenciadordesenhas.feature.categories.data.local.CategoryDao
import com.inovalou.seucofregerenciadordesenhas.feature.categories.data.local.CategoryEntity

@Database(
    entities = [CategoryEntity::class],
    version = 2,
    exportSchema = true
)
abstract class SeuCofreDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
}
