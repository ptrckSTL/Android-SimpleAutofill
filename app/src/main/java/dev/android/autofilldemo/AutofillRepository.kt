package dev.android.autofilldemo

import dev.android.autofilldemo.db.AutofillDataDao
import dev.android.autofilldemo.db.AutofillDataSet

class AutofillRepository(private val autofillDataDao: AutofillDataDao) {

    suspend fun getAllRecords(): List<AutofillDataSet> {
        return autofillDataDao.getAllRecords()
    }

    suspend fun insertRecord(dataSet: AutofillDataSet) {
        autofillDataDao.insertRecord(dataSet)
    }
}