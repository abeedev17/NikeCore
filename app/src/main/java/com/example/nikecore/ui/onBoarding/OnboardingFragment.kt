package com.example.nikecore.ui.onBoarding

import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.VideoView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.nikecore.R
import com.example.nikecore.sharedpreferences.AppPreferences
import kotlinx.android.synthetic.main.onboarding_fragment.*
import timber.log.Timber

class OnboardingFragment : Fragment() {


    private lateinit var viewModel: OnboardingViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel =
            ViewModelProvider(this).get(OnboardingViewModel::class.java)
        return inflater.inflate(R.layout.onboarding_fragment, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val list =
            listOf(
                R.string.OnBoarding_text_Use_map,
                R.string.OnBoarding_text_Maintain_your,
                R.string.OnBoarding_text_Collect_tickets
            )

        viewModel.i.observe(viewLifecycleOwner, {
            showMsgTxt.text = getString(list[it])
            val `in`: Animation = AlphaAnimation(0.0f, 1.0f)
            `in`.duration = 1000
            showMsgTxt.startAnimation(`in`)
        })

        if (AppPreferences.getIsFirstStart(requireContext())) {
            //startActivity(Intent(this, AskInfoActivity::class.java))
            findNavController().navigate(R.id.action_onboardingFragment_to_askinfoFragment)
        }
        val videoview = view.findViewById(R.id.videoView) as VideoView
        val uri: Uri = Uri.parse("android.resource://" + activity?.packageName + "/" + R.raw.nike)
        setDimension()
        videoview.setVideoURI(uri)
        videoview.start()
        videoView.setOnPreparedListener { mp -> //Start Playback
            videoView.start()
            //Loop Video
            mp!!.isLooping = true
            Timber.d("Video Started")
        }
        var audio = true
        val audioManager = this.activity?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, 0)
        imgBtn.setImageResource(R.drawable.ic_baseline_volume_up_24)

        imgBtn.setOnClickListener {
            audio = !audio
            if (audio) {
                audioManager.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_TOGGLE_MUTE,
                    0
                )
                imgBtn.setImageResource(R.drawable.ic_baseline_volume_off_24)

            } else {
                audioManager.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_RAISE,
                    0
                )
                imgBtn.setImageResource(R.drawable.ic_baseline_volume_up_24)
            }
        }

        finishRunBtn.setOnClickListener {
            AppPreferences.setIsFirstStart(requireContext(), true)
            findNavController().navigate(R.id.action_onboardingFragment_to_askinfoFragment)

        }


    }

    private fun setDimension() {
        // Adjust the size of the video
        // so it fits on the screen
        val videoProportion = getVideoProportion()
        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels
        val screenProportion = screenHeight.toFloat() / screenWidth.toFloat()
        val lp = videoView.layoutParams
        if (videoProportion < screenProportion) {
            lp.height = screenHeight
            lp.width = (screenHeight.toFloat() / videoProportion).toInt()
        } else {
            lp.width = screenWidth
            lp.height = (screenWidth.toFloat() * videoProportion).toInt()
        }
        videoView.layoutParams = lp
    }

    private fun getVideoProportion(): Float {
        return 2f
    }

}