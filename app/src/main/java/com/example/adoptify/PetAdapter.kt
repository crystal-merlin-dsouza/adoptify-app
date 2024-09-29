package com.example.adoptify.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.adoptify.PetData
import com.example.adoptify.R

// ListedPetsAdapter.kt
class PetAdapter(
    private val petList: List<PetData>, // Use PetData here
    private val onAdoptedClick: (PetData) -> Unit // Change type to PetData
) : RecyclerView.Adapter<PetAdapter.PetViewHolder>() {

    class PetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val petName: TextView = itemView.findViewById(R.id.petNameTextView)
        val adoptButton: Button = itemView.findViewById(R.id.adoptButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_item_pet, parent, false)
        return PetViewHolder(view)
    }

    override fun onBindViewHolder(holder: PetViewHolder, position: Int) {
        val pet = petList[position]
        holder.petName.text = pet.petName
        holder.adoptButton.setOnClickListener {
            onAdoptedClick(pet) // Pass PetData when clicked
        }
    }

    override fun getItemCount(): Int {
        return petList.size
    }
}
