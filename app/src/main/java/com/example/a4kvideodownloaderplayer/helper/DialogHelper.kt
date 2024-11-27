package com.example.aitutor.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.example.a4kvideodownloaderplayer.databinding.DownloadingDialogBinding
import com.example.a4kvideodownloaderplayer.databinding.LoadingDialogBinding

object DialogHelper {
    private var mDialog: AlertDialog? = null
    private var mAdLoadingDialog: Dialog? = null
    private var binding: DownloadingDialogBinding? = null




/*
    fun showExitDialog(activity: Activity, onDialogShow: (nativeCard: MaterialCardView, container: FrameLayout) -> Unit) {
        val binding = ExitDialogBinding.inflate(activity.layoutInflater)
        val builder = AlertDialog.Builder(activity)

        builder.setView(binding.root)
            .setCancelable(false)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()


        onDialogShow.invoke(binding.nativeAdCard, binding.nativeContainer)

        binding.yes.setOnClickListener {
            activity.finish()
        }

        binding.no.setOnClickListener {
            dialog.dismiss()
        }
    }*/


    fun AdShowDialog(context: Context) {
        val ctx = context as Activity
        if (!ctx.isDestroyed && !ctx.isFinishing) {
            val binding = LoadingDialogBinding.inflate(LayoutInflater.from(context))
            mAdLoadingDialog = Dialog(context)
            mAdLoadingDialog?.apply {
                setContentView(binding.root)
                setCancelable(false)
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                window?.setDimAmount(0.9f)
                show()
            }
        }
    }

    fun DownloadDialog(context: Context) {
        val ctx = context as Activity
        if (!ctx.isDestroyed && !ctx.isFinishing) {
            binding = DownloadingDialogBinding.inflate(LayoutInflater.from(context))
            mAdLoadingDialog = Dialog(context)
            mAdLoadingDialog?.apply {
                setContentView(binding?.root?:return)
                setCancelable(false)
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                window?.setDimAmount(0.5f)
                show()
            }
        }
    }

    fun updateProgress(progress: Int) {
        binding?.progressTv?.text = "$progress%"
    }





    fun hideDialog(context: Context) {
        val ctx = context as Activity
        if (!ctx.isDestroyed && !ctx.isFinishing) {
            if (mAdLoadingDialog != null) {
                mAdLoadingDialog?.dismiss()
            }
        }
    }

    fun dismissDialog() {
        if (mDialog?.isShowing == true) {
            mDialog?.dismiss()
        }
    }
}