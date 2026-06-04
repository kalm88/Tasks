package com.punchlist.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.punchlist.app.data.local.dao.PunchItemDao
import com.punchlist.app.data.local.entity.PunchItemEntity

@Database(
    entities = [PunchItemEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PunchlistDatabase : RoomDatabase() {
    abstract fun punchItemDao(): PunchItemDao
}
