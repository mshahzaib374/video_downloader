package com.example.a4kvideodownloaderplayer.fragments.langugage.views.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.a4kvideodownloaderplayer.R
import com.example.a4kvideodownloaderplayer.databinding.LanguageLayoutItemBinding
import com.example.a4kvideodownloaderplayer.fragments.langugage.model.Languages

class LanguagesAdapter(
    private val context: Context,
    private var selectedPosition: Int,
    private var selectedLanguage: (Languages, position: Int) -> Unit
) : ListAdapter<Languages, LanguagesAdapter.LanguagesViewHolder>(LanguagesDiffCallback()) {


    inner class LanguagesViewHolder(
        private val binding: LanguageLayoutItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(language: Languages, isSelected: Boolean) {
            binding.englishUnitedStatesTv.text = language.name
            Glide.with(context).load(language.flag).into(binding.americaLogo)
            if (isSelected) {
                binding.radioIV.visibility = android.view.View.VISIBLE
                binding.englishUnitedStatesTv.setTextColor(context.resources.getColor(R.color.white))
                binding.langugageView.background = ResourcesCompat.getDrawable(
                    context.resources,
                    R.drawable.drawable_langugage_selector,
                    null
                )
            }else{
                binding.radioIV.visibility = android.view.View.GONE
                binding.englishUnitedStatesTv.setTextColor(context.resources.getColor(R.color.language_item_text_color))
                binding.langugageView.background = ResourcesCompat.getDrawable(
                    context.resources,
                    R.drawable.drawable_langugage_item,
                    null
                )
            }

            binding.root.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = adapterPosition
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                selectedLanguage.invoke(language, selectedPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguagesViewHolder {
        val binding = LanguageLayoutItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LanguagesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LanguagesViewHolder, position: Int) {
        holder.bind(getItem(position), position == selectedPosition)
    }

    class LanguagesDiffCallback : DiffUtil.ItemCallback<Languages>() {
        override fun areItemsTheSame(oldItem: Languages, newItem: Languages): Boolean {
            // Assuming each language has a unique ID for identification, otherwise use a unique field
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: Languages, newItem: Languages): Boolean {
            return oldItem == newItem
        }
    }
}
