package com.example.simpleflashcards.db

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/*
* Represents one card to be reviewed in our deck of flash cards.
* The values isNew, isForgotten, and isReview are kept in a
* one-hot encoding, i.e., only one can be true at a time.
* Any other use of the class is a misuse and will lead to
* undefined behavior. The other valid state is for all states
* to be false, in which case the card may or may not be ready
* to be reviewed.
 */
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
    val back: String,
    @ColumnInfo(name = "base_interval")
    val baseInterval: Int,
    @ColumnInfo(name = "date_of_last_review")
    val dateOfLastReview: String
) {
    constructor() : this(0, false, true, false, "empty", "empty", 1,  LocalDate.now().toString()){

    }
}
