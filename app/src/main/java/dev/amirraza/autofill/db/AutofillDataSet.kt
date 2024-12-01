package dev.amirraza.autofill.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.amirraza.autofill.Constants.DB_TABLE_NAME

@Entity(tableName = DB_TABLE_NAME)
data class AutofillDataSet(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String?,
    val username: String?,
    val password: String?,
    val identifier: String?
)
