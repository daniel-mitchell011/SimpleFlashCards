package com.example.simpleflashcards

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.viewModelScope
import com.example.simpleflashcards.db.Card
import com.example.simpleflashcards.db.CardDao
import kotlinx.coroutines.launch
import java.util.PriorityQueue

class CardViewModel(
    private val dao: CardDao): ViewModel() {
        // Card that should show up on screen next to be reviewed
        // private val _currentCard = MutableLiveData<Card>()
        var currentCard: Card? = null
        // val currentCard: LiveData<Card> get() = _currentCard
        // private val _cardsQueue = MutableLiveData<PriorityQueue<Card>>()
         private val cardComparator = Comparator<Card> { card1, card2 ->
            card1.remainingDays.compareTo(card2.remainingDays)
        }
        // val priorityQueue = PriorityQueue<Card>(cardComparator)
        // val cardsQueue: LiveData<PriorityQueue<Card>> get() = _cardsQueue
        private val _countForgotten = MutableLiveData<Int>()
        val countForgotten: LiveData<Int> get() = _countForgotten
        private val _countReview    = MutableLiveData<Int>()
        val countReview: LiveData<Int> get()     = _countReview
        private val _countNew       = MutableLiveData<Int>()
        val cards = dao.getAllCards()
        val countNew: LiveData<Int> get() = _countNew
        private val _priorityQueue = MutableLiveData<PriorityQueue<Card>>()
        val priorityQueue: LiveData<PriorityQueue<Card>> get() = _priorityQueue

        init {
            viewModelScope.launch {
                dao.getAllCards().observeForever(){ cardList ->
                    val queue = PriorityQueue(cardComparator)
                    queue.addAll(cardList)
                    _priorityQueue.value = queue
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