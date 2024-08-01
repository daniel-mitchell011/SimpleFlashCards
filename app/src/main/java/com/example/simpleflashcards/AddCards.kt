package com.example.simpleflashcards

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.simpleflashcards.databinding.FragmentAddCardsBinding
import com.example.simpleflashcards.db.Card
import com.example.simpleflashcards.db.CardDatabase

class AddCards : Fragment() {

    private lateinit var binding: FragmentAddCardsBinding
    private lateinit var viewModel: CardViewModel
    private lateinit var adapter: CardRecyclerViewAdapter
    private lateinit var selectedCard: Card

    private var isCardItemClicked: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAddCardsBinding.inflate(inflater, container, false)

        val dao = CardDatabase.getInstance(requireContext()).cardDao()
        val factory = CardViewModelFactory(dao)
        viewModel = ViewModelProvider(this, factory).get(CardViewModel::class.java)

        binding.apply {
            btnAddCard.setOnClickListener{
                if(isCardItemClicked) {
                    updateCardData()
                } else {
                    saveCardData()
                }
            }
            btnClearFields.setOnClickListener{
                if(isCardItemClicked){
                    deleteCardData()
                }
                clearInput()
            }
        }

        initRecyclerView()

        return binding.root
    }

    private fun saveCardData(){
        viewModel.insertCard(
            Card(0,
                false,
                true,
                false,
                binding.etFront.text.toString(),
                binding.etBack.text.toString())
        )
    }

    private fun updateCardData() {
        viewModel.updateCard(
            card = Card(
                selectedCard.id,
                selectedCard.isReview,
                selectedCard.isNew,
                selectedCard.isForgotten,
                binding.etFront.text.toString(),
                binding.etBack.text.toString()
            )
        )
        binding.apply{
            btnAddCard.text = "Save"
            btnClearFields.text = "Clear"
            isCardItemClicked = false
        }
    }

    private fun deleteCardData() {
        viewModel.deleteCard(
            card = Card(
                selectedCard.id,
                selectedCard.isReview,
                selectedCard.isNew,
                selectedCard.isForgotten,
                selectedCard.front,
                selectedCard.back
            )
        )
        binding.apply {
            btnAddCard.text     = "Save"
            btnClearFields.text = "Clear"
            isCardItemClicked   = false
        }

    }

    private fun initRecyclerView() {
        binding.rvCards.layoutManager = LinearLayoutManager(this.context)
        adapter = CardRecyclerViewAdapter {
            selectedCard: Card -> cardItemClicked(selectedCard)
        }
        binding.rvCards.adapter = adapter
        displayCards()
    }

    private fun displayCards() {
        viewModel.cards.observe(viewLifecycleOwner, {
            adapter.setList(it)
            adapter.notifyDataSetChanged()
        })
    }

    private fun clearInput() {
        binding.apply {
            etFront.setText("")
            etBack.setText("")
        }
    }

    private fun cardItemClicked(card:Card) {
        binding.apply {
            selectedCard = card
            btnAddCard.text = "Update"
            btnClearFields.text = "Delete"
            isCardItemClicked = true
            etFront.setText(selectedCard.front)
            etBack.setText(selectedCard.back)
        }
    }

}