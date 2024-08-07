package com.example.simpleflashcards.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface CardDao {

    @Insert
    suspend fun insertCard(card: Card)

    @Update
    suspend fun updateCard(card: Card)

    @Delete
    suspend fun deleteCard(card: Card)

    @Query("SELECT * FROM card_data_table")
    fun getAllCards(): LiveData<List<Card>>

    @Query("SELECT * FROM card_data_table WHERE isForgotten = 0 AND isNew = 0")
    fun getCardsForReview(): LiveData<List<Card>>

    @Query("SELECT * FROM card_data_table WHERE isForgotten = 1")
    fun getAllForgottenCards(): LiveData<List<Card>>

    @Query("SELECT * FROM card_data_table WHERE isNew = 1")
    fun getAllNew(): LiveData<List<Card>>

}