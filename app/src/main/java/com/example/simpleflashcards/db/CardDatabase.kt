package com.example.simpleflashcards.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Card::class], version = 1, exportSchema = false)
abstract class CardDatabase: RoomDatabase() {
    abstract fun cardDao(): CardDao

    companion object {
        @Volatile
        private var INSTANCE: CardDatabase? = null
        fun getInstance(context: Context): CardDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (null == instance) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        CardDatabase::class.java,
                        "card_data_database"
                    ).build()
                }
                return instance
            }
        }
    }
}