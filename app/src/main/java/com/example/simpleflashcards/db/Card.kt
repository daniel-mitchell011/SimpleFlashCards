package com.example.simpleflashcards.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "card_data_table")
data class Card(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "card_id")
    val id: Int = 0,
    @ColumnInfo(name = "isReview")
    val isReview: Boolean,
    @ColumnInfo(name = "isNew")
    val isNew: Boolean,
    @ColumnInfo(name = "isForgotten")
    val isForgotten: Boolean,
    @ColumnInfo(name = "card_front")
    val front: String,
    @ColumnInfo(name = "card_back")
    val back: String
) {
}
