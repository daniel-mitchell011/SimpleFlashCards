package com.example.simpleflashcards

import android.annotation.SuppressLint
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.simpleflashcards.databinding.FragmentAddCardsBinding
import com.example.simpleflashcards.db.Card
import com.example.simpleflashcards.db.CardDatabase
import java.time.LocalDate

class AddCards : Fragment() {

    private lateinit var binding: FragmentAddCardsBinding
    private lateinit var viewModel: CardViewModel
    private lateinit var adapter: CardRecyclerViewAdapter
    private lateinit var selectedCard: Card

    private var isCardItemClicked: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @RequiresApi(Build.VERSION_CODES.O)
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
                    clearInput()
                } else {
                    saveCardData()
                    clearInput()
                }
            }
            btnClearFields.setOnClickListener{
                if(isCardItemClicked){
                    deleteCardData()
                }
                clearInput()
            }
            btnReturnToHome.setOnClickListener{
                val bundle = bundleOf()
                it.findNavController().navigate(R.id.action_addCards_to_homeFragment, bundle)
            }
        }

        initRecyclerView()

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveCardData(){
        viewModel.insertCard(
            Card(0,
                false,
                true,
                false,
                binding.etFront.text.toString(),
                binding.etBack.text.toString(),
                1,
                LocalDate.now().toString()
                )
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
                binding.etBack.text.toString(),
                selectedCard.baseInterval,
                selectedCard.dateOfLastReview
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
                selectedCard.back,
                selectedCard.baseInterval,
                selectedCard.dateOfLastReview
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

    @SuppressLint("NotifyDataSetChanged")
    private fun displayCards() {
        viewModel.cards.observe(viewLifecycleOwner) {
            adapter.setList(it)
            adapter.notifyDataSetChanged()
        }
    }

    private fun clearInput() {
        binding.apply {
            etFront.setText(getString(R.string.empty_string))
            etBack.setText(getString(R.string.empty_string))
        }
    }

    private fun cardItemClicked(card:Card) {
        selectedCard = card
        isCardItemClicked = true
        binding.apply {
            btnAddCard.text = "Update"
            btnClearFields.text = "Delete"
            etFront.setText(selectedCard.front)
            etBack.setText(selectedCard.back)
        }
    }

}