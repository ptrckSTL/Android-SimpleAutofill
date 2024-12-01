package dev.amirraza.autofill.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import dev.amirraza.autofill.Constants.DB_NAME

@Database(entities = [AutofillDataSet::class], version = 1, exportSchema = false)
abstract class AutofillDatabase : RoomDatabase() {

    abstract fun autofillDataDao(): AutofillDataDao

    companion object {
        @Volatile
        private var INSTANCE: AutofillDatabase? = null

        fun getDatabase(context: Context): AutofillDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AutofillDatabase::class.java,
                    DB_NAME
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}