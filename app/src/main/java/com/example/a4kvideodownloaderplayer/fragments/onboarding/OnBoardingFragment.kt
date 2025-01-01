package com.example.a4kvideodownloaderplayer.fragments.onboarding

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.a4kvideodownloaderplayer.R
import com.example.a4kvideodownloaderplayer.ads.advert.banner_onboarding_bottom
import com.example.a4kvideodownloaderplayer.ads.advert.banner_onboarding_med
import com.example.a4kvideodownloaderplayer.ads.advert.banner_onboarding_top
import com.example.a4kvideodownloaderplayer.ads.banner_ads.BannerAdType
import com.example.a4kvideodownloaderplayer.ads.banner_ads.BannerAdUtils
import com.example.a4kvideodownloaderplayer.ads.banner_ads.BannerCallback
import com.example.a4kvideodownloaderplayer.databinding.OnBoardingScreenBinding
import com.example.a4kvideodownloaderplayer.helper.AppUtils.logFirebaseEvent
import com.example.a4kvideodownloaderplayer.helper.InternetManager
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

class OnBoardingFragment : Fragment() {
    private var binding: OnBoardingScreenBinding? = null
    private var permissionRequestCount = 0 // Counter for permission requests
    private var isRequestInProgress = false // Flag to track if a request is in progress
    private var internetManager: InternetManager? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        internetManager =
            InternetManager(context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = OnBoardingScreenBinding.inflate(inflater, container, false)
        return binding?.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        context?.logFirebaseEvent("on_boarding_fragment", "screen_opened")

        attachClickListeners()
        loadBannerAdTop()
        loadBannerAdMed()
        loadBannerAdBottom()
    }

    private fun loadBannerAdTop() {
        binding?.adsOnboardBannerPlaceHolderTop?.visibility = View.VISIBLE
        binding?.shimmerLayoutTop?.root?.visibility = View.VISIBLE
        BannerAdUtils(activity ?: return).loadBannerAd(
            adId = getString(R.string.bannerOnBoardingAd),
            remote = banner_onboarding_top,
            container = binding?.adsOnboardBannerPlaceHolderTop ?: return,
            adLoadingOrShimmer = binding?.shimmerLayoutTop?.root,
            adType = BannerAdType.DEFAULT_BANNER,
            callback = object : BannerCallback() {
                override fun onAdValidate() {
                    super.onAdValidate()
                    binding?.adsOnboardBannerPlaceHolderTop?.visibility = View.GONE
                    binding?.shimmerLayoutTop?.root?.visibility = View.GONE

                }
            },
        )

    }

    private fun loadBannerAdMed() {
        binding?.adsOnboardBannerPlaceHolderMed?.visibility = View.VISIBLE
        binding?.shimmerLayoutMed?.root?.visibility = View.VISIBLE
        BannerAdUtils(activity ?: return).loadBannerAd(
            adId = getString(R.string.bannerOnBoardingAd),
            remote = banner_onboarding_med,
            container = binding?.adsOnboardBannerPlaceHolderMed ?: return,
            adLoadingOrShimmer = binding?.shimmerLayoutMed?.root,
            adType = BannerAdType.DEFAULT_BANNER,
            callback = object : BannerCallback() {
                override fun onAdValidate() {
                    super.onAdValidate()
                    binding?.adsOnboardBannerPlaceHolderMed?.visibility = View.GONE
                    binding?.shimmerLayoutMed?.root?.visibility = View.GONE

                }
            },
        )
    }

    private fun loadBannerAdBottom() {
        binding?.adsOnboardBannerPlaceHolderBottom?.visibility = View.VISIBLE
        binding?.shimmerLayoutBottom?.root?.visibility = View.VISIBLE
        BannerAdUtils(activity ?: return).loadBannerAd(
            adId = getString(R.string.bannerOnBoardingAd),
            remote = banner_onboarding_bottom,
            container = binding?.adsOnboardBannerPlaceHolderBottom ?: return,
            adLoadingOrShimmer = binding?.shimmerLayoutBottom?.root,
            adType = BannerAdType.DEFAULT_BANNER,
            callback = object : BannerCallback() {
                override fun onAdValidate() {
                    super.onAdValidate()
                    binding?.adsOnboardBannerPlaceHolderBottom?.visibility = View.GONE
                    binding?.shimmerLayoutBottom?.root?.visibility = View.GONE

                }
            },
        )

    }


    private fun attachClickListeners() {
        binding?.onBoardingScreenNextButton?.setOnClickListener {
            context?.logFirebaseEvent("on_boarding_fragment", "next_button_clicked")

            if (arePermissionsGranted()) {
                navigateToDisclaimerScreen() // Proceed to the next screen if permissions are granted
            } else {
                requestPermissions() // Request permissions if not granted
            }
        }
    }

    private fun navigateToDisclaimerScreen() {
        if (findNavController().currentDestination?.id == R.id.onBoardingFragment) {
            findNavController().navigate(R.id.action_onBoardingFragment_to_disclaimerFragment)
        }
    }


    private fun arePermissionsGranted(): Boolean {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_VIDEO, // for Android 13+
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
        return permissions.all {
            context?.let { it1 ->
                ContextCompat.checkSelfPermission(
                    it1,
                    it
                )
            } == PackageManager.PERMISSION_GRANTED
        }
    }


    private fun requestPermissions() {
        if (isRequestInProgress) return // Prevent multiple requests at the same time

        isRequestInProgress = true
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }

        Dexter.withContext(context ?: return)
            .withPermissions(*permissions)
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(multiplePermissionsReport: MultiplePermissionsReport) {
                    isRequestInProgress = false // Reset the flag after checking

                    // Reset the permission request count on success
                    if (multiplePermissionsReport.areAllPermissionsGranted()) {
                        permissionRequestCount = 0  // Reset count if permissions are granted
                        navigateToDisclaimerScreen() // Navigate if all permissions granted
                    } else {
                        // Handle denied permissions
                        handleDeniedPermissions(multiplePermissionsReport)
                    }
                }


                override fun onPermissionRationaleShouldBeShown(
                    list: List<PermissionRequest>,
                    permissionToken: PermissionToken
                ) {
                    permissionToken.continuePermissionRequest() // Continue the permission request process
                }
            })
            .withErrorListener {
                Toast.makeText(
                    context ?: return@withErrorListener,
                    "Error occurred!",
                    Toast.LENGTH_SHORT
                ).show()
                isRequestInProgress = false // Reset the flag on error
            }
            .onSameThread().check()
    }


    private fun handleDeniedPermissions(multiplePermissionsReport: MultiplePermissionsReport) {
        var anyPermanentlyDenied = false
        var anyDenied = false

        // Check for denied permissions
        for (result in multiplePermissionsReport.deniedPermissionResponses) {
            if (result.isPermanentlyDenied) {
                anyPermanentlyDenied = true
                permissionRequestCount++ // Increment count for permanently denied permissions
            } else {
                anyDenied = true // At least one permission was denied but not permanently
            }
        }

        // If some permissions were denied but not permanently, simply return
        if (anyDenied && !anyPermanentlyDenied) {
            return
        }

        /* // Show settings dialog only if count exceeds threshold
         if (anyPermanentlyDenied && permissionRequestCount >= 3) {
             showSettingsDialog()
         }*/
    }

}