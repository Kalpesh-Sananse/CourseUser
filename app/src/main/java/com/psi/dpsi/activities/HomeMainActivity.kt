package com.psi.dpsi.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.Fragment
import com.psi.dpsi.R
import com.psi.dpsi.databinding.ActivityHomeMainBinding
import com.psi.dpsi.fragments.CartFragment
import com.psi.dpsi.fragments.HomeFragment
import com.psi.dpsi.fragments.NotesFragment
import com.psi.dpsi.fragments.PendingOrdersFragment
import com.psi.dpsi.fragments.ProfileFragment
import com.psi.dpsi.utils.Constants

class HomeMainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityHomeMainBinding.inflate(layoutInflater) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        enableEdgeToEdge()

//        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
//            insets
//        }

        binding.apply {

            val fragment = intent.getStringExtra(Constants.GO_TO_CART)

            if(fragment != null) {
                when (fragment) {
                    Constants.CART_REF -> {
                        replaceFragment(CartFragment())
                        bottomNavigation.selectedItemId = R.id.navigation_cart
                    }
                    Constants.NOTES_REF -> {
                        replaceFragment(NotesFragment())
                        bottomNavigation.selectedItemId = R.id.navigation_notes
                    } else -> {

                        replaceFragment(HomeFragment())
                    bottomNavigation.selectedItemId = R.id.navigation_home
                    }

                }
            } else {
                replaceFragment(HomeFragment())
            }



            bottomNavigation.setOnItemSelectedListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.navigation_home -> {
                        replaceFragment(HomeFragment())
                        true
                    }

                    R.id.navigation_Profile -> {
                        replaceFragment(ProfileFragment())
                        true
                    }

                    R.id.navigation_cart -> {
                        replaceFragment(CartFragment())
                        true
                    }

                    R.id.navigation_notes -> {
                        replaceFragment(NotesFragment())
                        true
                    }

                    R.id.navigation_my_course -> {
                        replaceFragment(PendingOrdersFragment())
                        true
                    }

                    else -> false
                }

            }

        }

    }


    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_view_home, fragment)
            .commit()
    }

}