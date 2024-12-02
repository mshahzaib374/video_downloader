package com.example.a4kvideodownloaderplayer.helper

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.WindowManager
import com.example.a4kvideodownloaderplayer.databinding.DownloadingDialogBinding
import com.example.a4kvideodownloaderplayer.fragments.home.viewmodel.VideoViewModel

class DownloadDialogHelper(val callBack : () -> Unit) {
    private var mAdLoadingDialog: Dialog? = null
    private var binding: DownloadingDialogBinding? = null


    fun showDownloadDialog(context: Context) {
        val ctx = context as Activity
        if (!ctx.isDestroyed && !ctx.isFinishing) {
            binding = DownloadingDialogBinding.inflate(LayoutInflater.from(context))
            mAdLoadingDialog = Dialog(context)
            mAdLoadingDialog?.apply {
                setContentView(binding!!.root)
                setCancelable(false)
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                window?.setDimAmount(0.7f)
                window?.setLayout(
                    (ctx.resources.displayMetrics.widthPixels * 0.9).toInt(),  // 90% of screen width
                    WindowManager.LayoutParams.WRAP_CONTENT                    // Adjust height as needed
                )
                binding?.crossIv?.setOnClickListener {
                    dismissDialog()
                }
                binding?.cancelDownloadTv?.setOnClickListener {
                    callBack.invoke()
                    dismissDialog()
                }
                show()
            }
        }
    }

    /*fun initListeners(context: Context) {
        binding?.apply {
            crossIv.setOnClickListener {
                mAdLoadingDialog?.dismiss()
                binding = null
                mAdLoadingDialog = null
                //viewModel.cancelDownload(context)
            }
        }
    }*/


    fun updateProgress(progress: Int) {
        binding?.progressTv?.text = "$progress%" // Assuming you have a TextView for showing percentage
    }

    fun updateText(statusText: String) {
        binding?.textview?.text = statusText
    }


    fun dismissDialog() {
        mAdLoadingDialog?.dismiss()
        binding = null
        mAdLoadingDialog = null
    }
}
