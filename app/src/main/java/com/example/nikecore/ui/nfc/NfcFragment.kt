package com.example.nikecore.ui.nfc

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Intent
import android.content.SharedPreferences
import android.nfc.NfcAdapter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.nikecore.R
import kotlinx.android.synthetic.main.nfc_fragment.*
import timber.log.Timber

class NfcFragment : Fragment() {


    private lateinit var nfcAdapter: NfcAdapter
    private val nfcViewModel: NfcViewModel by activityViewModels()
    private var userMoney = 0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        nfcAdapter = NfcAdapter.getDefaultAdapter(requireContext())
        return inflater.inflate(R.layout.nfc_fragment, container, false)
    }

    override fun onResume() {
        super.onResume()
        val pendingIntent = PendingIntent.getActivity(
            requireContext(),
            0,
            Intent(requireContext(), javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            FLAG_IMMUTABLE or 0
        )
        nfcAdapter.enableForegroundDispatch(requireActivity(), pendingIntent, null, null)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter.disableForegroundDispatch(requireActivity())
        nfcAdapter.disableReaderMode(requireActivity())
    }

    override fun onStart() {
        super.onStart()
        nfcAdapter.enableReaderMode(
            requireActivity(),
            {
                Timber.d("nfc tag")



                requireActivity().runOnUiThread {
                    nfcAnimationView.visibility = View.GONE
                    acceptedAnimationView.visibility = View.VISIBLE

                    nfcViewModel.enterAmount.observe(viewLifecycleOwner, {
                        val settings: SharedPreferences =
                            requireContext().getSharedPreferences("user_Balance", 0)
                        userMoney = settings.getInt("USER_MONEY", 0)
                        val amount = userMoney - it
                        val editor = settings.edit()
                        editor.putInt("USER_MONEY", amount)
                        editor.apply()
                    })
                    Handler(Looper.getMainLooper()).postDelayed({
                        findNavController().popBackStack(R.id.navigation_payment, true)
                    }, 2500)

                }

            },
            NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_B or NfcAdapter.FLAG_READER_NFC_F or NfcAdapter.FLAG_READER_NFC_V or NfcAdapter.FLAG_READER_NFC_BARCODE,
            null
        )
    }

    override fun onStop() {
        super.onStop()
        Timber.d("nfc stop")
        nfcAdapter.disableReaderMode(requireActivity())
    }

}