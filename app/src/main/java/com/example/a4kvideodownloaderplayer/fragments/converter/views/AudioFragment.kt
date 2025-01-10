package com.example.a4kvideodownloaderplayer.fragments.converter.views

import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.example.a4kvideodownloaderplayer.databinding.AudioFragmentBinding
import com.example.a4kvideodownloaderplayer.fragments.converter.viewmodel.AudioViewModel
import com.example.a4kvideodownloaderplayer.fragments.converter.views.adapter.AudioAdapter
import com.example.a4kvideodownloaderplayer.fragments.premium.PremiumFragment
import com.example.a4kvideodownloaderplayer.helper.AudioConverter
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class AudioFragment : Fragment() {
    private val audioViewModel: AudioViewModel by activityViewModels()
    private var binding: AudioFragmentBinding? = null
    private var audioAdapter: AudioAdapter? = null



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
                    files,
                    navigate = {},
                )
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

    private fun clickEvents() {
        binding?.apply {
            premiumIcon.setOnClickListener {
                PremiumFragment().show(parentFragmentManager, "AudioFragment")

            }
            addVideoBtn.setOnClickListener { openFilePicker() }
        }
    }

    private fun openFilePicker() {

        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))

    }


    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // video picker.
            uri?.let {
                convertToAudio(it)
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
        AudioConverter()
            .extractAudio(
                getRealPathFromURI(videoUri) ?: "",
                file.path,
                onSuccess = {
                    Log.e("Khan", "onSuccess")
                },
                onFailed = {
                    Log.e("Khan", "onFailed")
                },
                above2Min = {
                    Log.e("Khan", "above5Min")
                },
                noAudioFound = {
                    Log.e("Khan", "noAudioFound")
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



}