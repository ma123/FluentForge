package com.identic.fluentforge.dataReader.local

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.identic.fluentforge.common.Constants
import com.identic.fluentforge.dataReader.local.library.LibraryDao
import com.identic.fluentforge.dataReader.local.library.LibraryItem
import com.identic.fluentforge.dataReader.local.reader.ReaderDao
import com.identic.fluentforge.dataReader.local.reader.ReaderItem

@Database(
    entities = [LibraryItem::class, ReaderItem::class],
    version = 1,
    exportSchema = true
)
abstract class FluentForgeDatabase : RoomDatabase() {

    abstract fun getLibraryDao(): LibraryDao
    abstract fun getReaderDao(): ReaderDao

    companion object {

        @Volatile
        private var INSTANCE: FluentForgeDatabase? = null

        fun getInstance(context: Context): FluentForgeDatabase {
            /*
            if the INSTANCE is not null, then return it,
            if it is, then create the database and save
            in instance variable then return it.
            */
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FluentForgeDatabase::class.java,
                    Constants.DATABASE_NAME
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }

}