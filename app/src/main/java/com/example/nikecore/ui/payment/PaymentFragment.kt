package com.example.nikecore.ui.payment

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.nikecore.R
import com.example.nikecore.databinding.FragmentPaymentBinding
import com.example.nikecore.ui.nfc.NfcViewModel
import kotlinx.android.synthetic.main.fragment_payment.*
import www.sanju.motiontoast.MotionToast

class PaymentFragment : Fragment() {

    private lateinit var paymentViewModel: PaymentViewModel
    private val nfcViewModel: NfcViewModel by activityViewModels()

    private var _binding: FragmentPaymentBinding? = null
    private var userMoney = 0
    private var userTicket = 0

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        paymentViewModel =
            ViewModelProvider(this).get(PaymentViewModel::class.java)

        _binding = FragmentPaymentBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val settings: SharedPreferences = requireContext().getSharedPreferences("user_Balance", 0)
        userMoney = settings.getInt("USER_MONEY", 0) //0 is the default value

        val ticketSharedPrefs: SharedPreferences =
            requireContext().getSharedPreferences("user_Ticket", 0)
        userTicket = ticketSharedPrefs.getInt("USER_TICKET", 0) //0 is the default value

        balanceValueTxt.text = resources.getString(R.string.string_euro, userMoney.toString())
        ticketValueTxt.text = userTicket.toString()

        startPaymentBtn.setOnClickListener {
            if (enterAmount.text.isBlank()) {

                MotionToast.darkToast(
                    requireActivity(),
                    getString(R.string.info),
                    getString(R.string.payment_msg),
                    MotionToast.TOAST_INFO,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.SHORT_DURATION,
                    ResourcesCompat.getFont(requireContext(), R.font.helvetica_regular)
                )

            } else if (enterAmount.text.isDigitsOnly() && enterAmount.text.toString()
                    .toInt() > 0 && userMoney >= enterAmount.text.toString().toInt()
            ) {
                nfcViewModel.enterAmount.postValue(enterAmount.text.toString().toInt())
                findNavController().navigate(R.id.action_navigation_payment_to_nfcFragment)
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}