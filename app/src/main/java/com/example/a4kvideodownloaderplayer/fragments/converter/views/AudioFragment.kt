package com.example.a4kvideodownloaderplayer.fragments.converter.views

import android.annotation.SuppressLint
import android.app.Activity
import android.app.RecoverableSecurityException
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.a4kvideodownloaderplayer.R
import com.example.a4kvideodownloaderplayer.databinding.AudioFragmentBinding
import com.example.a4kvideodownloaderplayer.dialogs.ConverterDialog
import com.example.a4kvideodownloaderplayer.fragments.converter.viewmodel.AudioViewModel
import com.example.a4kvideodownloaderplayer.fragments.converter.views.adapter.AudioAdapter
import com.example.a4kvideodownloaderplayer.fragments.howToUse.views.UseDialogFragment
import com.example.a4kvideodownloaderplayer.fragments.premium.PremiumFragment
import com.example.a4kvideodownloaderplayer.helper.AppUtils.logFirebaseEvent
import com.example.a4kvideodownloaderplayer.helper.AudioConverter
import com.example.a4kvideodownloaderplayer.helper.shareFile
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class AudioFragment : Fragment() {
    private val audioViewModel: AudioViewModel by activityViewModels()
    private var binding: AudioFragmentBinding? = null
    private var audioAdapter: AudioAdapter? = null
    private var converterDialog: ConverterDialog? = null
    private var urii: Uri ?= null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        converterDialog = ConverterDialog(activity ?: return)
        recoverableIntentLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Refresh the adapter after permission is granted
                urii?.let {
                    val rowsDeleted = context?.contentResolver?.delete(it, null, null) ?: 0
                    if (rowsDeleted > 0){
                        audioAdapter?.notifyDataSetChanged()
                        Toast.makeText(
                            context ?: return@let,
                            context?.getString(R.string.video_deleted),
                            Toast.LENGTH_SHORT
                        ).show()
                        loadAudioFiles()
                    }

                }

            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = AudioFragmentBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadAudioFiles()
        clickEvents()
    }

    private fun recyclerviewScrollListener() {
        binding?.rv?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (dy > 10 && binding?.addVideoBtn?.isShown == true) {
                    // binding?.addVideoBtn?.hide()
                    binding?.addVideoBtn?.shrink()
                }

                if (dy < -10 && binding?.addVideoBtn?.isShown == false) {
                    binding?.addVideoBtn?.show()
                    binding?.addVideoBtn?.extend()
                }

                if (!recyclerView.canScrollVertically(-1)) {
                    binding?.addVideoBtn?.show()
                    binding?.addVideoBtn?.extend()
                }
            }
        })
    }

    private fun loadAudioFiles() {
        audioViewModel.loadAudioFiles(context ?: return)

        audioViewModel.audioFiles.observe(viewLifecycleOwner) { files ->
            if (files.isNotEmpty()) {
                audioAdapter = AudioAdapter(
                    context ?: return@observe,
                    files,
                    navigate = {
                        if (findNavController().currentDestination?.id == R.id.mainFragment) {
                            Bundle().apply {
                                putString("videoUri", it.contentUri.toString())
                                putString("videoName", it.fileName)
                                putString("videoPath", it.filePath)
                                findNavController().navigate(
                                    R.id.action_mainFragment_to_AudioPlayerFragment,
                                    this
                                )
                            }
                        }
                    },
                    shareAudio = {
                        context?.logFirebaseEvent("audio_fragment", "share_button_clicked")
                        context?.shareFile(it.contentUri, "audio/mp3")
                    },
                    deleteAudio = {
                        deleteImage(it.contentUri)
                    }
                )
                audioAdapter?.notifyDataSetChanged()
                binding?.apply {
                    rv.adapter = audioAdapter
                    rv.visibility = View.VISIBLE
                    mp3TitleTV.visibility = View.GONE
                    mp3IV.visibility = View.GONE
                    recyclerviewScrollListener()
                }

            } else {
                binding?.apply {
                    rv.visibility = View.GONE
                    mp3TitleTV.visibility = View.VISIBLE
                    mp3IV.visibility = View.VISIBLE
                }
                //binding.progressBar.visibility = View.GONE
                // binding.noFilesTextView.visibility = View.VISIBLE
            }
        }

    }

    private fun deleteImage(contentUri: Uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val rowsDeleted = context?.contentResolver?.delete(contentUri, null, null) ?: 0
                if (rowsDeleted > 0) {
                   audioAdapter?.notifyDataSetChanged()
                    loadAudioFiles()
                    Toast.makeText(
                        context?:return,
                        context?.getString(R.string.audio_deleted),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            catch (e : RecoverableSecurityException){
                handleRecoverableException(e, contentUri)

            }
        } else {
            val isFileDeleted = File(contentUri.path ?: "").delete()
            if (isFileDeleted) {
                audioAdapter?.notifyDataSetChanged()
                Toast.makeText(
                    context?:return,
                    context?.getString(R.string.audio_deleted),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    @SuppressLint("NewApi")
    private fun handleRecoverableException(e: RecoverableSecurityException, uri : Uri) {
        urii = uri
        val intentSender = e.userAction.actionIntent.intentSender
        val request = IntentSenderRequest.Builder(intentSender).build()
        recoverableIntentLauncher.launch(request)
    }

    private lateinit var recoverableIntentLauncher: ActivityResultLauncher<IntentSenderRequest>



    private fun clickEvents() {
        binding?.apply {
            howUseIcon.setOnClickListener {
                context?.logFirebaseEvent("audio_fragment", "how_use_button_clicked")
                UseDialogFragment().show(parentFragmentManager, "HowToUseFragment")
            }
            premiumIcon.setOnClickListener {
                PremiumFragment().show(parentFragmentManager, "AudioFragment")
            }
            addVideoBtn.setOnClickListener { openFilePicker() }
        }
    }

    private fun openFilePicker() {

        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))

    }


    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // video picker.
            uri?.let {
                viewLifecycleOwner.lifecycleScope.launch {
                    converterDialog?.show()
                    delay(2000)
                    convertToAudio(it)
                }

            }
        }

    private fun convertToAudio(videoUri: Uri) {
        val downloadsFolder = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS
        ).absolutePath

        // Create a subfolder named "4kVideoDownloader"
        val subFolder = File(downloadsFolder, "4kVideoDownloader")
        if (!subFolder.exists()) {
            subFolder.mkdirs()
        }

        val timestamp =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName =
            "audio_$timestamp.mp3" // Replace with `uniqueName.mp4` for UUID option
        // Create the file in the subfolder
        val file = File(
            subFolder,
            fileName
        )
        AudioConverter(context?:return)
            .extractAudio(
                getRealPathFromURI(videoUri, context ?: return) ?: "",
                file.path,
                onSuccess = {
                    converterDialog?.dismiss()
                    loadAudioFiles()
                },
                onFailed = {
                    if (file.exists()) {
                        file.delete()
                    }
                    converterDialog?.dismiss()
                },
                above2Min = {
                    if (file.exists()) {
                        file.delete()
                    }
                    converterDialog?.dismiss()
                },
                noAudioFound = {
                    if (file.exists()) {
                        file.delete()
                    }
                    converterDialog?.dismiss()
                }
            )
    }

    private fun getRealPathFromURI(contentURI: Uri): String? {
        val result: String?
        val cursor: Cursor? = context?.contentResolver?.query(contentURI, null, null, null, null)
        if (cursor == null) {
            result = contentURI.path
        } else {
            cursor.moveToFirst()
            val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            result = cursor.getString(idx)
            cursor.close()
        }
        return result
    }

    fun getRealPathFromURI(uri: Uri, context: Context): String? {
        val returnCursor = context.contentResolver.query(uri, null, null, null, null)
        val nameIndex =  returnCursor!!.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        val name = returnCursor.getString(nameIndex)
        val file = File(context.cacheDir, name)
        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(file)
            var read = 0
            val maxBufferSize = 1 * 1024 * 1024
            val bytesAvailable: Int = inputStream?.available() ?: 0
            val bufferSize = Math.min(bytesAvailable, maxBufferSize)
            val buffers = ByteArray(bufferSize)
            while (inputStream?.read(buffers).also {
                    if (it != null) {
                        read = it
                    }
                } != -1) {
                outputStream.write(buffers, 0, read)
            }
            inputStream?.close()
            outputStream.close()

        } catch (e: java.lang.Exception) {
            Log.e("Exception", e.message!!)
        }
        return file.path
    }



}