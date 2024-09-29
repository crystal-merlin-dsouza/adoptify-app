package com.example.adoptify

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class ItmAdapter(private val itmList: ArrayList<DataSet>) :
    RecyclerView.Adapter<ItmAdapter.ItmHolder>() {

    private lateinit var mListener: OnItemClickListener

    interface OnItemClickListener {
        fun onItemClick(position: Int)
        fun onAdoptClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        mListener = listener
    }

    class ItmHolder(itmView: View, listener: OnItemClickListener) :
        RecyclerView.ViewHolder(itmView) {
        val itmname: TextView = itmView.findViewById(R.id.tv_name)
        val itmbreed: TextView = itmView.findViewById(R.id.tv_breed)
        val itmage: TextView = itmView.findViewById(R.id.tv_age)
        val itmgender: TextView = itmView.findViewById(R.id.tv_gender)
        val itmloc: TextView = itmView.findViewById(R.id.tv_location)
        val itmimg: ImageView = itmView.findViewById(R.id.tv_image)

        init {
            itmView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItmHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.item,
            parent, false
        )
        return ItmHolder(itemView, mListener)
    }

    override fun getItemCount(): Int {
        return itmList.size
    }

    override fun onBindViewHolder(holder: ItmHolder, position: Int) {
        val currentItem = itmList[position]
        holder.itmname.text = currentItem.petname
        holder.itmbreed.text = currentItem.petbreed ?: "Unknown Breed"
        holder.itmage.text = currentItem.petage.toString()
        holder.itmgender.text = currentItem.petgender
        holder.itmloc.text = currentItem.petlocation

        val imageData = currentItem.petphoto
        Log.d("ItmAdapter", "Image data size: ${imageData.size} bytes")

        if (imageData.isNotEmpty()) {
            val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
            holder.itmimg.setImageBitmap(bitmap)
        }

        // Set click listener to open PetDetailActivity
        holder.itemView.setOnClickListener {
            val imagePath = saveImageToFileSystem(holder.itemView.context, currentItem.petphoto)
            val intent = Intent(holder.itemView.context, PetDetailsActivity::class.java).apply {
                putExtra("pet_name", currentItem.petname)
                putExtra("pet_breed", currentItem.petbreed)
                putExtra("pet_age", currentItem.petage)
                putExtra("pet_gender", currentItem.petgender)
                putExtra("pet_contact", currentItem.petcontact.toString())
                putExtra("pet_location", currentItem.petlocation)
                putExtra("pet_photo_path", imagePath)
            }
            holder.itemView.context.startActivity(intent)
        }
    }


    private fun saveImageToFileSystem(context: Context, imageData: ByteArray): String {
        val file = File(context.filesDir, "pet_image_${System.currentTimeMillis()}.png")
        file.writeBytes(imageData)
        return file.absolutePath
    }
}
