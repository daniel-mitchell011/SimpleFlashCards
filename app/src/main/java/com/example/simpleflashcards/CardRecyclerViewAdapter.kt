package com.example.simpleflashcards

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.simpleflashcards.databinding.CardBinding
import com.example.simpleflashcards.db.Card

class CardRecyclerViewAdapter(
    private val clickListener: (Card) -> Unit
): RecyclerView.Adapter<CardViewHolder>() {

    private val cardList = ArrayList<Card>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val binding = CardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CardViewHolder(binding)
    }
    override fun getItemCount(): Int {
        return cardList.size
    }

    override fun onBindViewHolder(
        holder: CardViewHolder,
        position: Int
    ) {
        holder.bind(cardList[position], clickListener)
    }

    fun setList(students: List<Card>) {
        cardList.clear()
        cardList.addAll(students)
    }

}

class CardViewHolder(
    private val binding: CardBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(card: Card, clickListener: (Card) -> Unit) {
            binding.apply {
                tvDataFrnt.text = card.front
                tvDataBck.text = card.back
            }
        }
}