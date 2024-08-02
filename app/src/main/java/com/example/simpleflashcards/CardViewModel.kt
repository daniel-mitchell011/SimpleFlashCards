package com.example.simpleflashcards

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simpleflashcards.db.Card
import com.example.simpleflashcards.db.CardDao
import kotlinx.coroutines.launch

class CardViewModel(
    private val dao: CardDao): ViewModel() {
        // Card that should show up on screen next to be reviewed
        private val _currentCard = MutableLiveData<Card>()
        val currentCard: LiveData<Card> get() = _currentCard
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