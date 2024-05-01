package com.example.nikecore.ui.settings.editinfo

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.nikecore.R
import com.example.nikecore.others.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.edit_info_fragment.*
import www.sanju.motiontoast.MotionToast
import javax.inject.Inject

@AndroidEntryPoint
class EditInfoFragment : Fragment() {
    @Inject
    lateinit var sharedPreferences: SharedPreferences
    private lateinit var viewModel: EditInfoViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.edit_info_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadFieldsFromSharedPref()
        saveChangesBtn.setOnClickListener {
            val success = applyChangesToSharedPref()
            if (success) {
                MotionToast.darkToast(
                    requireActivity(),
                    getString(R.string.info),
                    getString(R.string.saved_changes),
                    MotionToast.TOAST_SUCCESS,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.SHORT_DURATION,
                    ResourcesCompat.getFont(requireContext(), R.font.helvetica_regular)
                )
                findNavController().navigate(R.id.action_editInfoFragment_to_navigation_run)
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

    private fun loadFieldsFromSharedPref() {
        val name = sharedPreferences.getString(Constants.KEY_NAME, "")
        val weight = sharedPreferences.getFloat(Constants.KEY_WEIGHT, 80f)
        editTextPersonNameEdit.setText(name)
        editTextWeightEdit.setText(weight.toString())
    }

    private fun applyChangesToSharedPref(): Boolean {
        val nameText = editTextPersonNameEdit.text.toString()
        val weightText = editTextWeightEdit.text.toString()
        val heightText = editTextHeightEdit.text.toString()

        if (nameText.isEmpty() || weightText.isEmpty() || heightText.isEmpty()) {
            return false
        }
        sharedPreferences.edit()
            .putString(Constants.KEY_NAME, nameText)
            .putFloat(Constants.KEY_WEIGHT, weightText.toFloat())
            .apply()
        return true
    }
}