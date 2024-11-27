package com.example.a4kvideodownloaderplayer.fragments.home.views

import android.app.ProgressDialog
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.a4kvideodownloaderplayer.R
import com.example.a4kvideodownloaderplayer.ads.advert.native_home_l
import com.example.a4kvideodownloaderplayer.ads.app_open_ad.OpenAppAd
import com.example.a4kvideodownloaderplayer.ads.native_ads.NativeAdCallback
import com.example.a4kvideodownloaderplayer.ads.native_ads.NativeAdItemsModel
import com.example.a4kvideodownloaderplayer.ads.native_ads.NativeAdUtils
import com.example.a4kvideodownloaderplayer.ads.native_ads.ad_types.NativeAdType
import com.example.a4kvideodownloaderplayer.databinding.HomeFragmentBinding
import com.example.a4kvideodownloaderplayer.databinding.NativeAdLayoutBinding
import com.example.a4kvideodownloaderplayer.fragments.home.viewmodel.VideoViewModel
import com.example.a4kvideodownloaderplayer.fragments.main.MainFragment
import com.example.a4kvideodownloaderplayer.helper.AppUtils.logFirebaseEvent
import com.example.a4kvideodownloaderplayer.helper.DownloadDialogHelper
import com.google.android.gms.ads.LoadAdError
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var binding: HomeFragmentBinding? = null
    private val videoViewModel: VideoViewModel by activityViewModels()
    private var progressDialog: ProgressDialog? = null
    private var downloadDialog: DownloadDialogHelper? = null
    private var fakeProgressJob: Job? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = HomeFragmentBinding.inflate(inflater, container, false)
        return binding?.root!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        context?.logFirebaseEvent("home_fragment", "screen_opened")

        binding?.apply {
            /* premiumIcon.setOnClickListener {
                 if (findNavController().currentDestination?.id == R.id.homeFragment) {
                     findNavController().navigate(R.id.action_homeFragment_to_premiumFragment)
                 }
             }*/

            input.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    // No action needed
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // No action needed
                }

                override fun afterTextChanged(s: Editable?) {
                    // Move the cursor to the end of the text
                    input.setSelection(input.text?.length ?: 0)
                }
            })



            cardViewDashboardHomeUpper.setBackgroundResource(R.drawable.rounded_corners_home_fragment_card)
            //titleHomeFragment.text = getString(R.string._4k_video_downloader_player)

            downloadTv.setOnClickListener {
                context?.logFirebaseEvent("home_fragment", "download_button_clicked")

                val downloadUrl = input.text.toString().trim()
                if (!URLUtil.isValidUrl(downloadUrl)) {
                    Toast.makeText(
                        context ?: return@setOnClickListener,
                        getString(R.string.please_enter_a_valid_url), Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                checkForYoutubeLink(downloadUrl) {
                    if (downloadUrl.isNotEmpty()) {
                        //progressDialog?.show()
                        downloadDialog = DownloadDialogHelper()
                        downloadDialog?.showDownloadDialog(context ?: return@checkForYoutubeLink)
                        videoViewModel.downloadVideo(downloadUrl)
                        var fakeProgress = 1
                        fakeProgressJob = lifecycleScope.launch {
                            while (fakeProgress < 10) {  // Show quick fake progress initially (0% to 10%)
                                delay(1000)  // Small delay to simulate progress increment
                                //_downloadProgress.postValue(fakeProgress)
                                Log.d("TAG", "fake: $fakeProgress")
                                downloadDialog?.updateProgress(fakeProgress)
                                fakeProgress += 1
                            }
                        }
                    }
                }

            }

            pasteLinkTv.setOnClickListener {
                context?.logFirebaseEvent("home_fragment", "paste_button_clicked")

                pasteClipboardText()
            }
        }

        setUpProgressDialog()
        loadDefaultNativeAd()

        videoViewModel.downloadStatus.observe(viewLifecycleOwner) { status ->
            binding?.input?.text?.clear()
            when (status) {
                "SUCCESS" -> {
                    Toast.makeText(
                        context ?: return@observe,
                        getString(R.string.video_downloaded_successfully),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                "ERROR" -> {
                    Toast.makeText(
                        context ?: return@observe,
                        getString(R.string.downloading_failed), Toast.LENGTH_SHORT
                    ).show()
                }

                else -> {
                    Toast.makeText(
                        context ?: return@observe,
                        getString(R.string.downloading_failed),
                        Toast.LENGTH_SHORT
                    ).show()

                }
            }
            Toast.makeText(context ?: return@observe, status, Toast.LENGTH_SHORT).show()
            // DialogHelper.hideDialog(context ?: return@observe)
            downloadDialog?.dismissDialog()
        }

        videoViewModel.downloadProgress.observe(viewLifecycleOwner) { progress ->
            //downloadDialogbinding?.progressTv?.text = "$progress%"
            //Log.d("TAG", "onViewCreated: $progress%")
            fakeProgressJob?.cancel()
            downloadDialog?.updateText(getString(R.string.your_video_is_being_download_nplease_wait))
            downloadDialog?.updateProgress(progress)

        }

        OpenAppAd.adEventListener = object : OpenAppAd.Companion.AdEventListener {
            override fun onAdShown() {
                binding?.nativeContainer?.visibility = View.INVISIBLE

            }

            override fun onAdDismissed() {
                binding?.nativeContainer?.visibility = View.VISIBLE
            }
        }

        MainFragment.dialogEventListener = object : MainFragment.Companion.DialogEventListener {
            override fun onDialogShown() {
                binding?.nativeContainer?.visibility = View.INVISIBLE
            }

            override fun onDialogDismissed() {
                binding?.nativeContainer?.visibility = View.VISIBLE
            }

        }

    }

    private fun checkForYoutubeLink(downloadUrl: String, downloadStart: () -> Unit = {}) {
        val youtubePattern = "^(https?://)?(www\\.)?(youtube|youtu|youtube-nocookie)\\.(com|be)/.*$"
        val isNotYouTubeLink = !Regex(youtubePattern).matches(downloadUrl)
        if (isNotYouTubeLink) {
            downloadStart.invoke()
        } else {
            Toast.makeText(
                context ?: return,
                getString(R.string.youtube_is_legally_restricted), Toast.LENGTH_SHORT
            )
                .show()
        }

    }

    private fun pasteClipboardText() {
        val clipboard = activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        if (clipboard.hasPrimaryClip()) {
            val clip = clipboard.primaryClip
            val item = clip?.getItemAt(0)
            val copiedText = item?.text.toString()
            binding?.input?.setText(copiedText)
        }
    }


    private fun loadDefaultNativeAd() {
        val bind = NativeAdLayoutBinding.inflate(layoutInflater)
        bind.apply {

            val nativeAdModel = NativeAdItemsModel(
                root,
                adHeadline = tvHeadline,
                adBody = tvBody,
                adIcon = appIcon,
                mediaView = mediaView,
                adCTA = adCTA
            )

            NativeAdUtils().loadNativeAd(
                activity?.application ?: return@apply,
                getString(R.string.homeNativeAd),
                adRemote = native_home_l,
                binding?.nativeContainer ?: return,
                nativeAdModel, object : NativeAdCallback() {

                    override fun adFailed(error: LoadAdError?) {
                        super.adFailed(error)
                        //binding?.nativeContainer?.visibility = View.INVISIBLE
                        //binding?.shimmerHomeLayout?.root?.visibility = View.INVISIBLE
                    }


                    override fun adLoaded() {
                        super.adLoaded()
                        binding?.nativeContainer?.visibility = View.VISIBLE
                        binding?.shimmerHomeLayout?.root?.visibility = View.GONE
                    }

                    override fun adValidate() {
                        binding?.nativeContainer?.visibility = View.INVISIBLE
                        binding?.shimmerHomeLayout?.root?.visibility = View.INVISIBLE
                    }


                }, NativeAdType.DEFAULT_AD
            )
        }

    }


    private fun setUpProgressDialog() {
        progressDialog = ProgressDialog(context ?: return)
        progressDialog?.setCancelable(false)
        progressDialog?.setCanceledOnTouchOutside(false)
        progressDialog?.setMessage(getString(R.string.downloading))
    }

    override fun onDestroyView() {
        if (progressDialog?.isShowing == true) {
            progressDialog?.dismiss()
        }
        downloadDialog?.dismissDialog()
        super.onDestroyView()
    }


}







