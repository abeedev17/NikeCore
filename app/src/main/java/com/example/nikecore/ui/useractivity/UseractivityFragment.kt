package com.example.nikecore.ui.useractivity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nikecore.adapters.RunAdapter
import com.example.nikecore.databinding.FragmentUseractivityBinding
import com.example.nikecore.others.SortType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_useractivity.*

@AndroidEntryPoint
class UseractivityFragment : Fragment() {

    private var _binding: FragmentUseractivityBinding? = null
    private val useractivityViewModel: UseractivityViewModel by viewModels()


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var runAdapter: RunAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentUseractivityBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()

        when (useractivityViewModel.sortType) {
            SortType.DATE -> spFilter.setSelection(0)
            SortType.RUNNING_TIME -> spFilter.setSelection(1)
            SortType.DISTANCE -> spFilter.setSelection(2)
            SortType.AVG_SPEED -> spFilter.setSelection(3)
            SortType.CALORIES_BURNED -> spFilter.setSelection(4)
        }

        spFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}

            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                pos: Int,
                id: Long
            ) {
                when (pos) {
                    0 -> useractivityViewModel.sortRuns(SortType.DATE)
                    1 -> useractivityViewModel.sortRuns(SortType.RUNNING_TIME)
                    2 -> useractivityViewModel.sortRuns(SortType.DISTANCE)
                    3 -> useractivityViewModel.sortRuns(SortType.AVG_SPEED)
                    4 -> useractivityViewModel.sortRuns(SortType.CALORIES_BURNED)
                }
            }
        }

        useractivityViewModel.runs.observe(viewLifecycleOwner, {
            runAdapter.submitList(it)
        })
    }

    private fun setupRecyclerView() = rvRuns.apply {
        runAdapter = RunAdapter()
        adapter = runAdapter
        layoutManager = LinearLayoutManager(requireContext())
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}