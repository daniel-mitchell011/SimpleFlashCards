package com.example.simpleflashcards

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.simpleflashcards.databinding.FragmentReviewCardsBinding
import com.example.simpleflashcards.db.Card
import com.example.simpleflashcards.db.CardDatabase
import java.time.LocalDate

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

        viewModel.cards.observe(viewLifecycleOwner) {list ->
            val currentCard = list.get(0)
            binding.apply {
                tvFront.text = currentCard?.front
                tvBack.text = currentCard?.back
            }
        }


        binding.btnOk.setOnClickListener {
            updateCardDatabase(forgotten = false)
            setNewCardState(binding)
        }
        binding.btnForgot.setOnClickListener {
            updateCardDatabase(forgotten = true)
            setNewCardState(binding)
        }
        binding.btnReveal.setOnClickListener {
            setCardRevealedState(binding)
        }

        return binding.root
    }

    private fun updateCardDatabase(forgotten: Boolean) {
        if(forgotten) {
            // TODO
        }
        else {
            // TODO
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

}