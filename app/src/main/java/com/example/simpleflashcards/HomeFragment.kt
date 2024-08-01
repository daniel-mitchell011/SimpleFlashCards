package com.example.simpleflashcards

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import com.example.simpleflashcards.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.btnStudy.setOnClickListener{
           val bundle = bundleOf()
           it.findNavController().navigate(R.id.action_homeFragment_to_reviewCards, bundle)
        }
        binding.btnAdd.setOnClickListener{
            val bundle = bundleOf()
            it.findNavController().navigate(R.id.action_homeFragment_to_addCards, bundle)
        }
        return binding.root
    }
}