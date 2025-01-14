package com.example.a4kvideodownloaderplayer.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.WindowManager
import com.example.a4kvideodownloaderplayer.databinding.ConvertingDialogBinding

class ConverterDialog(activity : Activity) : Dialog(activity){
    private val inflater = activity.getSystemService(
        Context.LAYOUT_INFLATER_SERVICE
    ) as LayoutInflater
    private val binding = ConvertingDialogBinding.inflate(inflater)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        this.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        this.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
    }
}