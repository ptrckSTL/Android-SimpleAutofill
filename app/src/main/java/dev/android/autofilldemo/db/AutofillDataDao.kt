package dev.android.autofilldemo.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.android.autofilldemo.Constants.DB_TABLE_NAME

@Dao
interface AutofillDataDao {

    @Query("SELECT * FROM $DB_TABLE_NAME")
    suspend fun getAllRecords(): List<AutofillDataSet>

    @Query("SELECT * FROM $DB_TABLE_NAME WHERE identifier = :identifier")
    suspend fun getAllRecordsByIdentifier(identifier: String): List<AutofillDataSet>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(entity: AutofillDataSet)
}