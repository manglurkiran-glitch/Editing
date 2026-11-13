package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.model.TransactionRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [TransactionRecord::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "recharge_pay.db"
                )
                    .addCallback(AppDatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class AppDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.transactionDao())
                    }
                }
            }

            private suspend fun populateInitialData(dao: TransactionDao) {
                val now = System.currentTimeMillis()
                val oneDayAgo = now - 86400000L
                val threeDaysAgo = now - (86400000L * 3)

                dao.insertTransaction(
                    TransactionRecord(
                        transactionId = "TXN984102941",
                        operatorRefId = "OPR-77291038",
                        phoneNumber = "9876543210",
                        operatorName = "Jio 5G",
                        planPrice = 349.00,
                        discountAmount = 50.0,
                        finalAmountPaid = 299.00,
                        paymentMethodName = "Visa •••• 4242",
                        paymentType = "CARD",
                        status = "SUCCESS",
                        timestamp = oneDayAgo,
                        planDetails = "2.5 GB/Day + Unlimited 5G Data & Calls",
                        validity = "28 Days"
                    )
                )

                dao.insertTransaction(
                    TransactionRecord(
                        transactionId = "TXN812391024",
                        operatorRefId = "OPR-38190241",
                        phoneNumber = "9123456789",
                        operatorName = "Airtel",
                        planPrice = 239.00,
                        discountAmount = 0.0,
                        finalAmountPaid = 239.00,
                        paymentMethodName = "Google Pay",
                        paymentType = "UPI_WALLET",
                        status = "SUCCESS",
                        timestamp = threeDaysAgo,
                        planDetails = "1.5 GB/Day Truly Unlimited Voice",
                        validity = "28 Days"
                    )
                )
            }
        }
    }
}
