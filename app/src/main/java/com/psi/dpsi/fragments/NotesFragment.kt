package com.psi.dpsi.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.psi.dpsi.activities.NotesDetailsActivity
import com.psi.dpsi.adapter.NotesAdapter
import com.psi.dpsi.databinding.FragmentNotesBinding
import com.psi.dpsi.factory.MainViewModelFactory
import com.psi.dpsi.model.NotesModel
import com.psi.dpsi.repository.MainRepository
import com.psi.dpsi.utils.Constants
import com.psi.dpsi.utils.Utils.gone
import com.psi.dpsi.utils.Utils.visible
import com.psi.dpsi.viewmodel.MainViewModel


class NotesFragment : Fragment(), NotesAdapter.OnItemClickListener {

    private val binding by lazy { FragmentNotesBinding.inflate(layoutInflater) }
    private lateinit var mainViewModel: MainViewModel
    private lateinit var adapter: NotesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainRepository = MainRepository(requireContext())
        val factory2 = MainViewModelFactory(mainRepository)
        mainViewModel = ViewModelProvider(requireActivity(), factory2)[MainViewModel::class.java]

        adapter = NotesAdapter(this@NotesFragment)

        mainViewModel.fetchNotes()

        binding.apply {

            mainViewModel.notesList.observe(viewLifecycleOwner) { list ->
                if(list.isNotEmpty()) {
                    loadingLayout.gone()
                    mainLayout.visible()
                    rv.adapter = adapter
                    adapter.submitList(list)
                    search(list)
                } else {
                    loadingLayout.visible()
                    mainLayout.gone()
                    tvStatus.text = "No Notes Found"
                }

            }

        }


    }

    private fun search(list: List<NotesModel>) {
        binding.searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if(list.isNotEmpty()) {
                    filteredList(newText, list)
                }
                return true
            }

        })

    }

    private fun filteredList(newText: String?, list: List<NotesModel>) {
        val filteredList = ArrayList<NotesModel>()
        for (category in list) {
            if (category.name.contains(newText.orEmpty(), ignoreCase = true))
                filteredList.add(category)
        }
        binding.rv.adapter = adapter
        adapter.submitList(filteredList)

    }

    override fun onItemClick(model: NotesModel) {
        try {
            val intent = Intent(requireActivity(), NotesDetailsActivity::class.java)
            intent.putExtra(Constants.NOTES_REF, model)
            startActivity(intent)
        } catch (e: Exception) {
            print(e.stackTrace)
        }

    }

}