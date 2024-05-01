package com.example.nikecore.ui.askinfo

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.nikecore.R
import com.example.nikecore.others.Constants.KEY_FIRST_TIME_TOGGLE
import com.example.nikecore.others.Constants.KEY_HEIGHT
import com.example.nikecore.others.Constants.KEY_NAME
import com.example.nikecore.others.Constants.KEY_WEIGHT
import com.example.nikecore.sharedpreferences.AppPreferences
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.askinfo_fragment.*
import www.sanju.motiontoast.MotionToast
import javax.inject.Inject

@AndroidEntryPoint
class AskinfoFragment : Fragment() {

    @Inject
    lateinit var sharedPref: SharedPreferences

    private lateinit var viewModel: AskinfoViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.askinfo_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val isFirstAppOpen =
            AppPreferences.getBoolean(requireContext(), KEY_FIRST_TIME_TOGGLE, true)
        if (!isFirstAppOpen) {

            findNavController().navigate(
                R.id.action_askinfoFragment_to_navigation_run,
                savedInstanceState
            )
        }

        getStartedAskBtn.setOnClickListener {
            val success = writePersonalDataToSharedPref()
            if (success) {
                findNavController().navigate(R.id.action_askinfoFragment_to_navigation_run)
            } else {
                MotionToast.darkToast(
                    requireActivity(),
                    getString(R.string.info),
                    getString(R.string.please_enter_all),
                    MotionToast.TOAST_ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.SHORT_DURATION,
                    ResourcesCompat.getFont(requireContext(), R.font.helvetica_regular)
                )

            }
        }
    }

    private fun writePersonalDataToSharedPref(): Boolean {
        val name = editTextPersonName.text.toString()
        val weight = editTextWeight.text.toString()
        val height = editTextHeight.text.toString()
        if (name.isEmpty() || weight.isEmpty() || height.isEmpty()) {
            return false
        }
        sharedPref.edit()
            .putString(KEY_NAME, name)
            .putFloat(KEY_WEIGHT, weight.toFloat())
            .putFloat(KEY_HEIGHT, height.toFloat())
            .apply()
        AppPreferences.setBoolean(requireContext(), KEY_FIRST_TIME_TOGGLE, false)
        return true
    }

}