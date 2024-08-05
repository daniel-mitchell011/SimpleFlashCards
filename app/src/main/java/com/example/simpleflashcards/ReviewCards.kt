package com.example.simpleflashcards

import android.graphics.Paint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.simpleflashcards.databinding.FragmentReviewCardsBinding
import com.example.simpleflashcards.db.Card
import com.example.simpleflashcards.db.CardDatabase
import java.time.LocalDate
import java.util.PriorityQueue

class ReviewCards : Fragment() {

    private lateinit var binding: FragmentReviewCardsBinding

    private lateinit var viewModel: CardViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

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

        // Get a card from the db
        // viewModel.cards.observe(viewLifecycleOwner) {list ->
        //     binding.apply {
        //         tvFront.text = currentCard?.front
        //         tvBack.text = currentCard?.back
        //     }
        // }

        binding.btnStart.setOnClickListener{
            initializeUI()
            loadNewCard()
            binding.apply {
                btnStart.visibility = View.INVISIBLE
                tvFront.visibility = View.VISIBLE
                btnReveal.visibility = View.VISIBLE
            }
        }

        binding.btnOk.setOnClickListener {
            // updateCardDatabase(forgotten = false)
            applyToCountText({viewModel.decrementReviewCount()},
                {viewModel.decrementNewCount()},
                {viewModel.decrementForgottenCount()})
            loadNewCard()
            setNewCardState(binding)
        }
        binding.btnForgot.setOnClickListener {
            // updateCardDatabase(forgotten = true)
            viewModel.priorityQueue.value?.add(viewModel.currentCard)
            applyToCountText({viewModel.decrementReviewCount()},
                {viewModel.decrementNewCount()},
                {viewModel.decrementForgottenCount()})

            viewModel.incrementForgottenCount()
            loadNewCard()
            setNewCardState(binding)
        }
        binding.btnReveal.setOnClickListener {
            setCardRevealedState(binding)
        }

        return binding.root
    }

    private fun updateCardDatabase(forgotten: Boolean) {
        if(forgotten) {
            viewModel.updateCard(card = Card(
                                    viewModel.currentCard?.id ?: -1,
                                    false,
                                    false,
                                    true,
                                    viewModel.currentCard?.front ?: "",
                                    viewModel.currentCard?.back ?: "",
                                    (viewModel.currentCard?.baseInterval ?: 0) / 2,
                                    viewModel.currentCard?.remainingDays ?: 0,
                                    viewModel.currentCard?.timestamp ?: ""
            ))
        } else {
            viewModel.updateCard(card = Card(
                viewModel.currentCard?.id ?: -1,
                true,
                false,
                false,
                viewModel.currentCard?.front ?: "",
                viewModel.currentCard?.back ?: "",
                ((viewModel.currentCard?.baseInterval ?: 0) * 2) + 1,
                viewModel.currentCard?.remainingDays ?: 0,
                viewModel.currentCard?.timestamp ?: ""
            ))
        }
    }

    private fun setNewCardState(binding: FragmentReviewCardsBinding) {
        binding.btnReveal.visibility = View.VISIBLE
        binding.tvBack.visibility    = View.INVISIBLE
        binding.btnOk.visibility     = View.INVISIBLE
        binding.btnForgot.visibility = View.INVISIBLE
    }

    private fun setCardRevealedState(binding: FragmentReviewCardsBinding) {
        binding.btnReveal.visibility = View.INVISIBLE
        binding.tvBack.visibility    = View.VISIBLE
        binding.btnOk.visibility     = View.VISIBLE
        binding.btnForgot.visibility = View.VISIBLE
    }

    private fun updateScreen() {
        updateReviewCount()
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

        for (card in viewModel.priorityQueue!!.value!!) {
            if (card.isReview)
            {
                viewModel.incrementReviewCount()
            }
            if(card.isForgotten) {
                viewModel.incrementForgottenCount()
            }
            if(card.isNew){
                viewModel.incrementNewCount()
            }
        }

        binding.apply {
            tvReview.visibility = View.VISIBLE
            tvNewCard.visibility = View.VISIBLE
            tvForgot.visibility = View.VISIBLE
        }
    }

    /*
    * Updates the values displayed that show how many cards are left
    * to review.
    * Red -- Forgotten
    * Green -- Review
    * Blue -- New
     */
    private fun updateReviewCount() {

    }

    private fun updateDisplayedCard() {

    }

    private fun getCurrentCard(cards: List<Card>)  {

    }

    private fun loadNewCard() {
        println("\n\n********\n${viewModel.priorityQueue.value?.size}\n\n******\n\n")
        if (viewModel.priorityQueue.value!!.isEmpty()) {
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
        } else {
            viewModel.currentCard = viewModel.priorityQueue.value?.poll()
            binding.apply {
                tvFront.text = viewModel.currentCard?.front
                tvBack.text = viewModel.currentCard?.back
            }
            applyToCountText({binding.tvReview.paintFlags = binding.tvReview.paintFlags or Paint.UNDERLINE_TEXT_FLAG},
                {binding.tvNewCard.paintFlags = binding.tvNewCard.paintFlags or Paint.UNDERLINE_TEXT_FLAG},
                {binding.tvForgot.paintFlags = binding.tvForgot.paintFlags or Paint.UNDERLINE_TEXT_FLAG})
        }
    }

    /* Apply an action for each case depending on whether the card is new, to be reviewed, or forgotten */
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
}