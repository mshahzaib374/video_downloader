package com.example.a4kvideodownloaderplayer.fragments.langugage.views.adapter

import android.app.Application
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.a4kvideodownloaderplayer.R
import com.example.a4kvideodownloaderplayer.ads.advert.native_language_l
import com.example.a4kvideodownloaderplayer.ads.app_open_ad.OpenAppAd
import com.example.a4kvideodownloaderplayer.ads.native_ads.NativeAdCallback
import com.example.a4kvideodownloaderplayer.ads.native_ads.NativeAdItemsModel
import com.example.a4kvideodownloaderplayer.ads.native_ads.NativeAdUtils
import com.example.a4kvideodownloaderplayer.ads.native_ads.ad_types.NativeAdType
import com.example.a4kvideodownloaderplayer.databinding.LanguageLayoutItemBinding
import com.example.a4kvideodownloaderplayer.databinding.NativeAdLayoutBinding
import com.example.a4kvideodownloaderplayer.databinding.RecyclerviewNativeAdBinding
import com.example.a4kvideodownloaderplayer.fragments.langugage.model.Languages
import com.google.android.gms.ads.LoadAdError

class LanguagesAdapter(
    private val application : Application,
    private val context: Context,
    private var selectedPosition: Int,
    private var selectedLanguage: (Languages, position: Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_LANGUAGE = 0
    private val VIEW_TYPE_AD = 1

    private val items = mutableListOf<Any>() // Can hold both Languages and a placeholder for ads

    fun setItems(languages: List<Languages>) {
        items.clear()
        for ((index, language) in languages.withIndex()) {
            items.add(language)
            if (index == 4 /*&& !AppPrefs(context).getBoolean("isFirstTime")*/) {
                items.add("AD_PLACEHOLDER") // Placeholder for the ad
            }
        }
        notifyDataSetChanged()
    }

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
            } else {
                binding.radioIV.visibility = View.GONE
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

    inner class AdViewHolder(private val binding: RecyclerviewNativeAdBinding, private val nativeBind:NativeAdLayoutBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            OpenAppAd.adEventListener = object : OpenAppAd.Companion.AdEventListener {
                override fun onAdShown() {
                    binding.nativeContainer.visibility = View.INVISIBLE
                }

                override fun onAdDismissed() {
                    binding.nativeContainer.visibility = View.VISIBLE

                }
            }
            val nativeAdModel = NativeAdItemsModel(
                nativeBind.root,
                adHeadline = nativeBind.tvHeadline,
                adBody = nativeBind.tvBody,
                adIcon = nativeBind.appIcon,
                mediaView = nativeBind.mediaView,
                adCTA = nativeBind.adCTA
            )

            NativeAdUtils().loadNativeAd(
                application,
                context.getString(R.string.languageNativeAd),
                adRemote = native_language_l,
                binding.nativeContainer,
                nativeAdModel, object : NativeAdCallback() {

                    override fun adFailed(error: LoadAdError?) {
                        super.adFailed(error)
                        binding.nativeContainer.visibility = View.GONE
                        binding.shimmerHomeLayout.root.visibility = View.GONE
                    }

                    override fun adLoaded() {
                        super.adLoaded()
                        binding.nativeContainer.visibility = View.VISIBLE
                        binding.shimmerHomeLayout.root.visibility = View.GONE
                    }

                    override fun adValidate() {
                        binding.nativeContainer.visibility = View.GONE
                        binding.shimmerHomeLayout.root.visibility = View.GONE
                    }
                }, NativeAdType.DEFAULT_AD
            )
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position] is Languages) VIEW_TYPE_LANGUAGE else VIEW_TYPE_AD
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_LANGUAGE) {
            val binding = LanguageLayoutItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            LanguagesViewHolder(binding)
        } else {
            val binding = RecyclerviewNativeAdBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            val adBind = NativeAdLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            AdViewHolder(binding, adBind)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is LanguagesViewHolder) {
            val language = items[position] as Languages
            holder.bind(language, position == selectedPosition)
        } else if (holder is AdViewHolder) {
            Log.d("TAG", "AdViewHolder: ")
            holder.bind()
        }


    }

    override fun getItemCount(): Int = items.size
}
