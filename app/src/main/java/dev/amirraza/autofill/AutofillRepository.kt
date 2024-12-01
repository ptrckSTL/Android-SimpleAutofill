package dev.amirraza.autofill

import dev.amirraza.autofill.db.AutofillDataDao
import dev.amirraza.autofill.db.AutofillDataSet

class AutofillRepository(private val autofillDataDao: AutofillDataDao) {

    suspend fun getAllRecords(): List<AutofillDataSet> {
        return autofillDataDao.getAllRecords()
    }

    suspend fun insertRecord(dataSet: AutofillDataSet) {
        autofillDataDao.insertRecord(dataSet)
    }
}