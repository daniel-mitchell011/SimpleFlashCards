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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.simpleflashcards.databinding.FragmentReviewCardsBinding
import com.example.simpleflashcards.db.Card
import com.example.simpleflashcards.db.CardDatabase
import java.time.LocalDate
import java.util.Queue

class ReviewCards : Fragment() {

    val DEAD_CARD: Card = Card()
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
            setNewCardState(binding)
            loadNextCard()
        }
        binding.btnForgot.setOnClickListener {
            updateCardDatabase(forgotten = true)
            viewModel.forgottenQueue.value?.add(viewModel.currentCard)
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
                                    LocalDate.now().toString()))
            // Update the temporary variable for the current card so that the state is not
            // inconsistent with the database
           viewModel.currentCard = Card(
               viewModel.currentCard?.id ?: -1,
               false,
               false,
               true,
               viewModel.currentCard.front,
               viewModel.currentCard.back,
               srsCalculation (viewModel.currentCard.baseInterval, false),
               LocalDate.now().toString())
        } else {
            viewModel.updateCard(card = Card(
                                      viewModel.currentCard.id,
                                      false,
                                      false,
                                      false,
                                      viewModel.currentCard.front,
                                      viewModel.currentCard.back,
                                      srsCalculation(viewModel.currentCard.baseInterval, true),
                                      LocalDate.now().toString()))
            // Update the temporary variable for the current card so that the state is not
            // inconsistent with the database
            viewModel.currentCard = Card(
                viewModel.currentCard.id,
                false,
                false,
                false,
                viewModel.currentCard.front,
                viewModel.currentCard.back,
                srsCalculation(viewModel.currentCard.baseInterval, true),
                LocalDate.now().toString())
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
        viewModel.newQueue.observe(viewLifecycleOwner) { cards ->
            if (viewModel.currentCard.isNew) {
                binding.tvNewCard.text = "${cards.size + 1}"
            }
            else {
                binding.tvNewCard.text = "${cards.size}"
            }
        }

        viewModel.forgottenQueue.observe(viewLifecycleOwner) { cards ->
            if (viewModel.currentCard.isForgotten) {
                binding.tvForgot.text = "${cards.size + 1}"
            } else {
                binding.tvForgot.text = "${cards.size}"
            }
        }

        viewModel.reviewQueue.observe(viewLifecycleOwner) { cards ->
            if(viewModel.currentCard.isReview) {
                binding.tvReview.text = "${cards.size + 1}"
            } else {
                binding.tvReview.text = "${cards.size}"
            }
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
        if (noCardsLeft()) {
            setFinishedStateUI()
        } else { // There are cards left, so grab the next one and give it to the user.
            viewModel.currentCard = chooseCard() ?: DEAD_CARD
            viewModel.notifyQueues()
            binding.apply {
                tvFront.text = viewModel.currentCard?.front
                tvBack.text = viewModel.currentCard?.back
            }
            // Update underline on text counters for remaining cards.
            val card = viewModel.currentCard
            if(card!!.isReview){
                binding.tvReview.paintFlags = binding.tvReview.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            } else {
                binding.tvReview.paintFlags = binding.tvReview.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
            }
            if(card!!.isNew){
                binding.tvNewCard.paintFlags = binding.tvNewCard.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            } else {
                binding.tvNewCard.paintFlags = binding.tvNewCard.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
            }
            if(card!!.isForgotten){
                binding.tvForgot.paintFlags = binding.tvForgot.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            } else {
                binding.tvForgot.paintFlags = binding.tvForgot.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
            }
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
        if (!(rev.isNullOrEmpty())) {
            c = viewModel.pollFromReviewQueue()
        } else if(!(forgot.isNullOrEmpty())) {
            c = viewModel.pollFromForgetQueue()
        } else if(!(newQ.isNullOrEmpty())) {
            c = viewModel.pollFromNewQueue()
        } else {
            c = null
        }
        return c
    }

    /*
    *  Determine whether any of the three queues has any cards left.
    *  Return true when no cards remain in any of the queues, false otherwise.
    */
    private fun noCardsLeft(): Boolean {
        return ((viewModel.reviewQueue.value.isNullOrEmpty()) and
                (viewModel.newQueue.value.isNullOrEmpty()) and
                (viewModel.forgottenQueue.value.isNullOrEmpty()))

    }

    /* Apply an action for each case depending on whether the card is new, to be reviewed, or forgotten */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun applyToCountText(reviewAction: () -> Unit,
                                 newAction: () -> Unit,
                                 forgottenAction: () -> Unit) {
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
        if(increaseFlag) {
            return (interval * 2) + 1
        } else {
            return interval / 2
        }

    }

}