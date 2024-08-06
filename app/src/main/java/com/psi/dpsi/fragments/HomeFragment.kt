package com.psi.dpsi.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.psi.dpsi.model.CourseModel
import com.psi.dpsi.R
import com.psi.dpsi.activities.CourseDetailsActivity
import com.psi.dpsi.activities.WelcomeActivity
import com.psi.dpsi.adapter.CourseAdapter
import com.psi.dpsi.adslider.SliderAdapter
import com.psi.dpsi.adslider.SliderModel
import com.psi.dpsi.databinding.DialogExitBinding
import com.psi.dpsi.databinding.FragmentHomeBinding
import com.psi.dpsi.databinding.NavigationLayoutBinding
import com.psi.dpsi.factory.AuthViewModelFactory
import com.psi.dpsi.factory.MainViewModelFactory
import com.psi.dpsi.model.UserModel
import com.psi.dpsi.repository.AuthRepository
import com.psi.dpsi.repository.MainRepository
import com.psi.dpsi.utils.Constants
import com.psi.dpsi.utils.SharedPref
import com.psi.dpsi.utils.Utils
import com.psi.dpsi.utils.Utils.gone
import com.psi.dpsi.utils.Utils.visible
import com.psi.dpsi.viewmodel.AuthViewModel
import com.psi.dpsi.viewmodel.MainViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class HomeFragment : Fragment(), CourseAdapter.OnItemClickListener {
    private val binding by lazy { FragmentHomeBinding.inflate(layoutInflater) }
    private lateinit var user: UserModel
    private lateinit var bottomSheet: BottomSheetDialog
    private lateinit var adapter: CourseAdapter
    private lateinit var authViewModel: AuthViewModel
    private lateinit var mainViewModel: MainViewModel
    private lateinit var sliderList: ArrayList<SliderModel>

    private lateinit var database: FirebaseDatabase
    private lateinit var viewPager2: ViewPager2
    private lateinit var pageChangeListener: ViewPager2.OnPageChangeCallback
    private val params = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.WRAP_CONTENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    ).apply {
        setMargins(0,0,8,0)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        user = SharedPref.getUserData(requireContext()) ?: UserModel()
        bottomSheet = BottomSheetDialog(requireContext(), R.style.CustomBottomSheetStyle)

        val repository = AuthRepository(FirebaseAuth.getInstance(),  requireContext())
        val factory = AuthViewModelFactory(repository)
        authViewModel = ViewModelProvider(requireActivity(), factory)[AuthViewModel::class.java]

        val mainRepository = MainRepository(requireContext())
        val factory2 = MainViewModelFactory(mainRepository)
        mainViewModel = ViewModelProvider(requireActivity(), factory2)[MainViewModel::class.java]


        viewPager2 = binding.viewPager2
        database = FirebaseDatabase.getInstance()
        pageChangeListener = object : ViewPager2.OnPageChangeCallback(){}

        sliderList = ArrayList()

        fetchSliderData()
        mainViewModel.fetchCourses()

        binding.apply {

            btMenu.setOnClickListener {
                showMoreOptions()
            }

            tvName.text = "Hi, ${user.name}"
            adapter = CourseAdapter(this@HomeFragment)

            ivProfile.setOnClickListener {
                findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
            }

            tvName.setOnClickListener {
                findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
            }

            mainViewModel.courseList.observe(viewLifecycleOwner) { list ->
                if(list.isNotEmpty()) {
                    loadingLayout.gone()
                    homeMainLayout.visible()
                    rv.adapter = adapter
                    adapter.submitList(list)
                    search(list)
                } else {
                    loadingLayout.visible()
                    homeMainLayout.gone()
                    tvStatus.text = "No Course Found"
                }

            }

        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                if(bottomSheet.isShowing) {
                    bottomSheet.dismiss()
                } else {
                    showExitDialog()
                }
            }

        })

    }

    private fun fetchSliderData() {
        database.getReference(Constants.SLIDER_DOCUMENT).orderByChild("timestamp")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    sliderList.clear()
                    for (childSnapshot in dataSnapshot.children.reversed()) {
                        val sliders = childSnapshot.getValue(SliderModel::class.java)
                        if (sliders != null) {
                            sliderList.add(sliders)
                        }
                    }
                    if (sliderList.isNotEmpty()) {
                        createSlider()
                    } else {
                        binding.card.visibility = View.GONE
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle onCancelled event if needed
                }
            })
    }

    private fun createSlider() {
        val sliderAdapter = SliderAdapter(requireContext())
        val slideDotLL = binding.linearLay
        val viewPager2 = binding.viewPager2

        viewPager2.adapter = sliderAdapter

        val dotImage = Array(sliderList.size) { ImageView(requireContext()) }

        slideDotLL.removeAllViews()

        binding.card.visibility = View.VISIBLE
        dotImage.forEach {
            it.setImageResource(R.drawable.non_active_dot)
            slideDotLL.addView(it, params)
        }

        dotImage[0].setImageResource(R.drawable.active_dot)

        sliderAdapter.submitList(sliderList)

        val pageChangeListener = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                dotImage.mapIndexed { index, imageView ->
                    imageView.setImageResource(
                        if (position == index) R.drawable.active_dot else R.drawable.non_active_dot
                    )
                }
                super.onPageSelected(position)
            }
        }
        viewPager2.registerOnPageChangeCallback(pageChangeListener)

        val handler = Handler(Looper.getMainLooper())


        val runnable = object : Runnable {
            override fun run() {
                val currentItem = viewPager2.currentItem
                val nextItem = if (currentItem < sliderList.size - 1) currentItem + 1 else 0
                viewPager2.setCurrentItem(nextItem, true)
                handler.postDelayed(this, 3500)
            }
        }
        handler.removeCallbacks(runnable)
        handler.postDelayed(runnable, 3500)
    }

    private fun showMoreOptions() {
        bottomSheet = BottomSheetDialog(requireContext())
        val layout = NavigationLayoutBinding.inflate(layoutInflater)
        bottomSheet.setContentView(layout.root)
        bottomSheet.setCanceledOnTouchOutside(true)

        layout.apply {

            navProfile.setOnClickListener {
                bottomSheet.dismiss()
                findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
            }

            navHistory.setOnClickListener {
                bottomSheet.dismiss()
                findNavController().navigate(R.id.action_homeFragment_to_orderHistoryFragment)
            }

            navCart.setOnClickListener {
                bottomSheet.dismiss()
                findNavController().navigate(R.id.action_homeFragment_to_cartFragment)
            }

            navPendingOrders.setOnClickListener {
                bottomSheet.dismiss()
                findNavController().navigate(R.id.action_homeFragment_to_pendingOrdersFragment)
            }

            navShareApp.setOnClickListener {
                bottomSheet.dismiss()
            }

            navAboutUs.setOnClickListener {
                bottomSheet.dismiss()
                findNavController().navigate(R.id.action_homeFragment_to_aboutUsFragment)
            }

            navChatOnWhatsapp.setOnClickListener {
                bottomSheet.dismiss()
                Utils.openWhatsAppChat(requireContext(), "+919876543210")
            }

            navContactUs.setOnClickListener {
                bottomSheet.dismiss()
                Utils.openDialer(requireContext(), "+919876543210")
            }

            navFeedBack.setOnClickListener {
                bottomSheet.dismiss()
                Utils.openEmailClient(requireContext(), "contact@gmail.com")
            }

            navLogout.setOnClickListener {
                bottomSheet.dismiss()
                authViewModel.signOut()
                val intent = Intent(requireActivity(), WelcomeActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                requireActivity().finish()
            }

            btClose.setOnClickListener {
                bottomSheet.dismiss()
            }

        }


        bottomSheet.show()

    }


    private fun showExitDialog() {
        val bottomSheet = BottomSheetDialog(requireContext())
        val layout = DialogExitBinding.inflate(layoutInflater)
        bottomSheet.setContentView(layout.root)
        bottomSheet.setCanceledOnTouchOutside(true)
        layout.btExit.setOnClickListener {
            bottomSheet.dismiss()
            val intent = Intent(requireActivity(), WelcomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            requireActivity().finish()
        }
        layout.btCancel.setOnClickListener {
            bottomSheet.dismiss()
        }
        bottomSheet.show()
    }



    private fun search(list: List<CourseModel>) {
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

    private fun filteredList(newText: String?, list: List<CourseModel>) {
        val filteredList = ArrayList<CourseModel>()
        for (category in list) {
            if (category.courseName.contains(newText.orEmpty(), ignoreCase = true))
                filteredList.add(category)
        }
        adapter.submitList(filteredList)
        binding.rv.adapter = adapter

    }

    override fun onItemClick(courseModel: CourseModel) {
        val intent = Intent(requireActivity(), CourseDetailsActivity::class.java)
        intent.putExtra(Constants.COURSE_REF, courseModel)
        startActivity(intent)
    }

}