package com.psi.dpsi.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.psi.dpsi.adapter.OrderHistoryAdapter
import com.psi.dpsi.databinding.FragmentPendingOrdersBinding
import com.psi.dpsi.factory.MainViewModelFactory
import com.psi.dpsi.repository.MainRepository
import com.psi.dpsi.utils.Utils.gone
import com.psi.dpsi.utils.Utils.visible
import com.psi.dpsi.viewmodel.MainViewModel


class PendingOrdersFragment : Fragment() {
    private val binding by lazy { FragmentPendingOrdersBinding.inflate(layoutInflater) }
    private lateinit var mainViewModel: MainViewModel
    private lateinit var adapter: OrderHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = MainRepository(requireContext())
        val factory = MainViewModelFactory(repository)
        mainViewModel = ViewModelProvider(requireActivity(), factory) [MainViewModel::class.java]
        adapter = OrderHistoryAdapter(requireContext())

        binding.apply {

            mainViewModel.pendingOrders.observe(viewLifecycleOwner) { list ->
                if (list.isNotEmpty()) {
                    loadingLayout.gone()
                    mainLayout.visible()
                    rv.adapter = adapter
                    adapter.submitList(list)
                    //search(list)
                } else {
                    loadingLayout.visible()
                    mainLayout.gone()
                    tvStatus.text = "No Purchase Found"
                }


            }
        }
    }



}