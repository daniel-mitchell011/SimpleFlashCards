package com.example.simpleflashcards

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simpleflashcards.db.Card
import com.example.simpleflashcards.db.CardDao
import kotlinx.coroutines.launch
import java.util.LinkedList
import java.util.Queue

class CardViewModel(
    private val dao: CardDao): ViewModel() {
     // Card that should show up on screen next to be reviewed
     // private val _currentCard = MutableLiveData<Card>()
     // val currentCard: LiveData<Card> get() = _currentCard

     @RequiresApi(Build.VERSION_CODES.O)
     var currentCard: Card = Card()

     private val _countForgotten = MutableLiveData<Int>()
     val countForgotten: LiveData<Int> get() = _countForgotten
     private val _countReview    = MutableLiveData<Int>()
     val countReview: LiveData<Int> get()     = _countReview
     private val _countNew       = MutableLiveData<Int>()
     val cards          = dao.getCardsForReview()
     val countNew: LiveData<Int> get() = _countNew
     private val _reviewQueue = MutableLiveData<Queue<Card>>()
     val reviewQueue: LiveData<Queue<Card>> get() = _reviewQueue
     private val _newQueue = MutableLiveData<Queue<Card>>()
     val newQueue: LiveData<Queue<Card>> get() = _newQueue
     private val _forgottenQueue = MutableLiveData<Queue<Card>>()
     val forgottenQueue: LiveData<Queue<Card>> get() = _forgottenQueue

     init {
         viewModelScope.launch {
             dao.getAllNew().observeForever(){ cardList ->
                 val queue: Queue<Card> = LinkedList()
                 queue.addAll(cardList)
                 _newQueue.value = queue
             }
             dao.getAllForgottenCards().observeForever(){ cardList ->
                 val queue: Queue<Card> = LinkedList()
                 queue.addAll(cardList)
                 _forgottenQueue.value = queue
             }
             dao.getCardsForReview().observeForever(){ cardList ->
                 val queue: Queue<Card> = LinkedList()
                 queue.addAll(cardList)
                 _reviewQueue.value = queue
                 // TODO: Remove any cards that are not ready for review
                 // TODO: Randomize order for cards to be reviewed
             }
         }
     }

     fun incrementReviewCount() {
         val count = countReview.value ?: 0
         _countReview.value = count + 1
     }

    fun decrementReviewCount() {
        val count = countReview.value ?: 0
        _countReview.value = count - 1
    }

    fun incrementNewCount() {
        val count = countNew.value ?: 0
        _countNew.value = count + 1
    }

    fun decrementNewCount() {
        val count = countNew.value ?: 0
        _countNew.value = count - 1
    }

    fun incrementForgottenCount() {
        val count = countForgotten.value ?: 0
        _countForgotten.value = count + 1
    }

    fun decrementForgottenCount() {
        val count = countForgotten.value ?: 0
        _countForgotten.value = count - 1
    }

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