package com.example.simpleflashcards

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.simpleflashcards.db.Card
import com.example.simpleflashcards.db.CardDao
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.LinkedList
import java.util.Queue
import java.time.Duration
import kotlin.random.Random

class CardViewModel(
    private val dao: CardDao): ViewModel() {
     // Card that should show up on screen next to be reviewed
     // private val _currentCard = MutableLiveData<Card>()
     // val currentCard: LiveData<Card> get() = _currentCard

    private var populatedNewCardQueue: Boolean = false
    private var populatedForgottenCardQueue: Boolean = false
    private var populatedReviewCardQueue: Boolean = false

    @RequiresApi(Build.VERSION_CODES.O)
    var currentCard: Card = Card()

    private val _countForgotten = MutableLiveData<Int>()
    val countForgotten: LiveData<Int> get() = _countForgotten
    private val _countReview    = MutableLiveData<Int>()
    val countReview: LiveData<Int> get()     = _countReview
    private val _countNew       = MutableLiveData<Int>()
    val cards          = dao.getAllCards()
    val newCards = dao.getAllNew()
    val forgottenCards = dao.getAllForgottenCards()
    val reviewCards = dao.getCardsForReview()
    val countNew: LiveData<Int> get() = _countNew
    private val _reviewQueue = MutableLiveData<Queue<Card>>()
    val reviewQueue: LiveData<Queue<Card>> get() = _reviewQueue
    private val _newQueue = MutableLiveData<Queue<Card>>()
    val newQueue: LiveData<Queue<Card>> get() = _newQueue
    private val _forgottenQueue = MutableLiveData<Queue<Card>>()
    val forgottenQueue: LiveData<Queue<Card>> get() = _forgottenQueue

   // val reviewQueue: Queue<Card> = LinkedList()
   // val newQueue: Queue<Card> = LinkedList()
   // val forgottenQueue: Queue<Card> = LinkedList()

    private val newCardObserver = Observer<List<Card>> { cardList ->
        if (!populatedNewCardQueue){
            val queue: Queue<Card> = LinkedList()
            queue.addAll(cardList)
            _newQueue.value = LinkedList()
            _newQueue.value?.clear()
            _newQueue.value?.addAll(queue)
            populatedNewCardQueue = true
        }
    }

    private val forgottenCardObserver = Observer<List<Card>> { cardList ->
        if(!populatedForgottenCardQueue) {
            val queue: Queue<Card> = LinkedList()
            queue.addAll(cardList)
            _forgottenQueue.value = LinkedList()
            _forgottenQueue.value?.clear()
            _forgottenQueue.value?.addAll(queue)
            populatedForgottenCardQueue = true
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private val reviewCardObserver = Observer<List<Card>> { cardList ->
        if(!populatedReviewCardQueue) {
            val queue: Queue<Card> = LinkedList()
            val listToRandomize: MutableList<Card> = mutableListOf()
            queue.addAll(cardList)
            _reviewQueue.value = LinkedList()
            _reviewQueue.value?.clear()
            for (card in queue) {
                val today = LocalDate.now()
                val time_of_last_review = LocalDate.parse(card.dateOfLastReview)
                val numDays = Duration.between(today.atStartOfDay(), time_of_last_review.atStartOfDay()).toDays()
                if(numDays >= card.baseInterval) {
                    // Make sure the card is a review card
                    val cardToAdd: Card = Card(
                        card.id,
                        true,
                        card.isNew,
                        card.isForgotten,
                        card.front,
                        card.back,
                        card.baseInterval,
                        card.dateOfLastReview
                    )
                    // Add the card to the review queue.
                    listToRandomize.add(cardToAdd)
                }
            }

            while(listToRandomize.isNotEmpty()) {
                val randomIndex = Random.nextInt(0, listToRandomize.size)
                val card: Card = listToRandomize.removeAt(randomIndex)
                _reviewQueue.value?.add(card)
            }
            populatedReviewCardQueue = true
        }
    }



     init {
         viewModelScope.launch {
             newCards.observeForever(newCardObserver)
             forgottenCards.observeForever(forgottenCardObserver)
             reviewCards.observeForever(reviewCardObserver)
         }
     }


    fun pollFromNewQueue(): Card? {
        val newValue = _newQueue.value?.poll()
        return newValue
    }

    fun pollFromReviewQueue(): Card? {
        val reviewValue = _reviewQueue.value?.poll()
        return reviewValue
    }

    fun pollFromForgetQueue(): Card? {
        val forgetValue = _forgottenQueue.value?.poll()
        return forgetValue
    }

    fun notifyQueues() {
        notifyReviewQueueOfChange()
        notifyForgetQueueOfChange()
        notifyNewQueueOfChange()
    }

    private fun notifyNewQueueOfChange() {
        _newQueue.value = _newQueue.value
    }

    private fun notifyReviewQueueOfChange() {
        _reviewQueue.value = _reviewQueue.value
    }

    private fun notifyForgetQueueOfChange() {
        _forgottenQueue.value = _forgottenQueue.value
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