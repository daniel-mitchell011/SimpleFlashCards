package com.example.simpleflashcards

import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.simpleflashcards.databinding.FragmentReviewCardsBinding
import com.example.simpleflashcards.db.Card
import com.example.simpleflashcards.db.CardDatabase
import java.time.LocalDate
import java.util.PriorityQueue
import java.util.LinkedList

class ReviewCards : Fragment() {

    private val DEAD_CARD: Card = Card()
    private lateinit var binding: FragmentReviewCardsBinding

    private lateinit var viewModel: CardViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentReviewCardsBinding.inflate(inflater, container, false)
        binding.btnReturn.setOnClickListener{
            val bundle = bundleOf()
            it.findNavController().navigate(R.id.action_reviewCards_to_homeFragment, bundle)
        }

        // Get database and manipulate it via viewmodel
        val dao = CardDatabase.getInstance(requireContext()).cardDao()
        val factory = CardViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory).get(CardViewModel::class.java)

        binding.btnStart.setOnClickListener{
            initializeUI()
            loadNextCard()
        }

        binding.btnOk.setOnClickListener {
            updateCardDatabase(forgotten = false)
            applyToCountText({ viewModel.decrementReviewCount()    },
                             { viewModel.decrementNewCount()       },
                             { viewModel.decrementForgottenCount() })
            setNewCardState(binding)
            loadNextCard()
        }
        binding.btnForgot.setOnClickListener {
            updateCardDatabase(forgotten = true)
            viewModel.forgottenQueue.value?.add(viewModel.currentCard)
            applyToCountText({ viewModel.decrementReviewCount()    },
                             { viewModel.decrementNewCount()       },
                             { viewModel.decrementForgottenCount() })
            viewModel.incrementForgottenCount()
            setNewCardState(binding)
            loadNextCard()
        }
        binding.btnReveal.setOnClickListener {
            setCardRevealedState(binding)
        }

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateCardDatabase(forgotten: Boolean) {
        if(forgotten) {
            viewModel.updateCard(card = Card(
                                    viewModel.currentCard?.id ?: -1,
                                    false,
                                    false,
                                    true,
                                    viewModel.currentCard.front,
                                    viewModel.currentCard.back,
                                    srsCalculation (viewModel.currentCard.baseInterval, false),
                                    viewModel.currentCard.dateOfLastReview))
        } else {
            viewModel.updateCard(card = Card(
                                      viewModel.currentCard.id,
                                      true,
                                      false,
                                      false,
                                      viewModel.currentCard.front,
                                      viewModel.currentCard.back,
                                      srsCalculation(viewModel.currentCard.baseInterval, true),
                                      viewModel.currentCard.dateOfLastReview))
        }
    }

    /*
    * Change the UI state so that it is appropriate for the next card to be reviewed
    */
    private fun setNewCardState(binding: FragmentReviewCardsBinding) {
        binding.btnReveal.visibility = View.VISIBLE
        binding.tvBack.visibility    = View.INVISIBLE
        binding.btnOk.visibility     = View.INVISIBLE
        binding.btnForgot.visibility = View.INVISIBLE
    }

    /*
    * Change the UI state so that it accomodates the card that the user just revealed
    * the answer to
    */
    private fun setCardRevealedState(binding: FragmentReviewCardsBinding) {
        binding.btnReveal.visibility = View.INVISIBLE
        binding.tvBack.visibility    = View.VISIBLE
        binding.btnOk.visibility     = View.VISIBLE
        binding.btnForgot.visibility = View.VISIBLE
    }


    private fun initializeUI() {
        viewModel.countReview.observe(viewLifecycleOwner) {value ->
            binding.tvReview.text = "${value}"
        }
        viewModel.countNew.observe(viewLifecycleOwner) {value ->
            binding.tvNewCard.text = "${value}"
        }
        viewModel.countForgotten.observe(viewLifecycleOwner) {value ->
            binding.tvForgot.text = "${value}"
        }

        for (card in viewModel.reviewQueue.value!!.iterator()) {
            if(card.isReview) { viewModel.incrementReviewCount() }
            if(card.isForgotten) { viewModel.incrementForgottenCount() }
            if(card.isNew){ viewModel.incrementNewCount() }
        }

        for (card in viewModel.forgottenQueue.value!!.iterator()) {
            if(card.isReview) { viewModel.incrementReviewCount() }
            if(card.isForgotten) { viewModel.incrementForgottenCount() }
            if(card.isNew){ viewModel.incrementNewCount() }
        }

        for (card in viewModel.newQueue.value!!.iterator()) {
            if(card.isReview) { viewModel.incrementReviewCount() }
            if(card.isForgotten) { viewModel.incrementForgottenCount() }
            if(card.isNew){ viewModel.incrementNewCount() }
        }

        binding.apply {
            tvReview.visibility = View.VISIBLE
            tvNewCard.visibility = View.VISIBLE
            tvForgot.visibility = View.VISIBLE
            btnStart.visibility = View.INVISIBLE
            tvFront.visibility = View.VISIBLE
            btnReveal.visibility = View.VISIBLE
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadNextCard() {
        if (noCardsLeft() or (DEAD_CARD == viewModel.currentCard)) {
            setFinishedStateUI()
        } else { // There are cards left, so grab the next one and give it to the user.
            viewModel.currentCard = chooseCard() ?: DEAD_CARD
            binding.apply {
                tvFront.text = viewModel.currentCard?.front
                tvBack.text = viewModel.currentCard?.back
            }
            // Update underline on text counters for remaining cards.
            applyToCountText({ binding.tvReview.paintFlags = binding.tvReview.paintFlags or Paint.UNDERLINE_TEXT_FLAG   },
                             { binding.tvNewCard.paintFlags = binding.tvNewCard.paintFlags or Paint.UNDERLINE_TEXT_FLAG },
                             { binding.tvForgot.paintFlags = binding.tvForgot.paintFlags or Paint.UNDERLINE_TEXT_FLAG   })
        }
    }

    /*
    * Chooses a card from one of the queues reviewQueue, newQueue, or forgottenQueue in priority order:
    * 1) Forgotten queue
    * 2) Review queue
    * 3) newQueue
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun chooseCard(): Card? {
        val rev = viewModel.reviewQueue.value
        val newQ = viewModel.newQueue.value
        val forgot = viewModel.forgottenQueue.value
        var c: Card?
        if ((null != forgot) and !(forgot!!.isEmpty())) {
            c = rev!!.poll()
        } else if((null != forgot) and !(forgot.isEmpty())) {
            c = forgot.poll()
        } else if((null != newQ) and !(newQ!!.isEmpty())) {
            c = newQ.poll()
        } else {
            c = null
        }
        return c
    }

    private fun noCardsLeft(): Boolean {
        return ((viewModel.reviewQueue.value?.isEmpty() != false) and
                (viewModel.newQueue.value?.isEmpty() != false) and
                (viewModel.forgottenQueue.value?.isEmpty() != false))
    }

    /* Apply an action for each case depending on whether the card is new, to be reviewed, or forgotten */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun applyToCountText(reviewAction: () -> Unit,
                                 newAction: () -> Unit,
                                 forgottenAction: () -> Unit) {
        val card = viewModel.currentCard
        if(card!!.isReview){
            reviewAction()
        }
        if(card!!.isNew){
            newAction()
        }
        if(card!!.isForgotten){
            forgottenAction()
        }
    }

    private fun setFinishedStateUI() {
        val itemsToHide = listOf(
            binding.tvReview,
            binding.tvNewCard,
            binding.tvForgot,
            binding.tvBack,
            binding.tvFront,
            binding.btnOk,
            binding.btnForgot,
            binding.btnReveal
        )
        itemsToHide.forEach {
            it.visibility = View.INVISIBLE
        }
        binding.tvFinished.visibility = View.VISIBLE

    }

    /*
    *  Calculate the next review interval for the card.
    *  If the increaseFlag value is set, then increase the interval;
    *  otherwise, decrease it.
     */
    private fun srsCalculation(interval: Int, increaseFlag: Boolean): Int {
        if(increaseFlag) { // Increase interval using Collatz conjecture function
            return (interval * 3) + 1
        } else {
            return interval / 2
        }

    }

}