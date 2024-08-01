package com.example.simpleflashcards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.simpleflashcards.db.CardDao

class CardViewModelFactory(
    private val dao: CardDao
): ViewModelProvider.Factory {
    override fun<T: ViewModel> create(modelClass: Class<T>): T{
        if(modelClass.isAssignableFrom(CardViewModel::class.java)) {
            return CardViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown View Model Class")
    }
}