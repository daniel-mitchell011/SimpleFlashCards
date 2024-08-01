package com.example.simpleflashcards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simpleflashcards.db.Card
import com.example.simpleflashcards.db.CardDao
import kotlinx.coroutines.launch

class CardViewModel(
    private val dao: CardDao): ViewModel() {

        val cards = dao.getAllCards()

        fun insertCard(card: Card)=viewModelScope.launch {
            dao.insertCard(card)
        }

    fun updateCard(card: Card)=viewModelScope.launch {
        dao.updateCard(card)
    }

    fun deleteCard(card: Card)=viewModelScope.launch {
        dao.deleteCard(card)
    }
}