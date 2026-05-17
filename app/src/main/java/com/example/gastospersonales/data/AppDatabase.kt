package com.example.gastospersonales.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.gastospersonales.data.converters.DateConverter
import com.example.gastospersonales.data.dao.AccountDao
import com.example.gastospersonales.data.dao.MovementDao
import com.example.gastospersonales.data.dao.UserDao
import com.example.gastospersonales.data.entities.Account
import com.example.gastospersonales.data.entities.Movement
import com.example.gastospersonales.data.entities.User

@Database(entities = [User::class, Movement::class, Account::class], version = 5, exportSchema = false)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun movementDao(): MovementDao
    abstract fun userDao(): UserDao
    abstract fun accountDao(): AccountDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gastos_personales_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
