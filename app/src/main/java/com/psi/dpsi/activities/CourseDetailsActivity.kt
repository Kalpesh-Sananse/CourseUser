package com.psi.dpsi.activities

import android.content.Intent
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import coil.load
import com.psi.dpsi.R
import com.psi.dpsi.databinding.ActivityCourseDetailsBinding
import com.psi.dpsi.factory.MainViewModelFactory
import com.psi.dpsi.model.CartModel
import com.psi.dpsi.model.CourseModel
import com.psi.dpsi.model.UserModel
import com.psi.dpsi.repository.MainRepository
import com.psi.dpsi.utils.Constants
import com.psi.dpsi.utils.SharedPref
import com.psi.dpsi.utils.Utils
import com.psi.dpsi.utils.Utils.gone
import com.psi.dpsi.utils.Utils.visible
import com.psi.dpsi.viewmodel.MainViewModel
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class CourseDetailsActivity : AppCompatActivity() {
    private val binding by lazy { ActivityCourseDetailsBinding.inflate(layoutInflater) }
    private lateinit var course: CourseModel
    private lateinit var mainViewModel: MainViewModel
    private lateinit var user: UserModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        course = intent.getParcelableExtra<CourseModel>(Constants.COURSE_REF) ?: CourseModel()


        setDetails(course)
        setupCounter("1", binding.tvQty, binding.btIncrease, binding.btDecrease)
        user = SharedPref.getUserData(this) ?: UserModel()


        val repository = MainRepository(this)
        val factory = MainViewModelFactory(repository)
        mainViewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        onBackPressedDispatcher.addCallback(this@CourseDetailsActivity, object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                val intent = Intent(this@CourseDetailsActivity, HomeMainActivity::class.java)
                intent.putExtra(Constants.GO_TO_CART, Constants.COURSE)
                startActivity(intent)
                finish()
            }

        })

        binding.apply {

            loadingLayout.visible()
            mainLayout.gone()


            mainViewModel.isInCart(user.userId, course.id)

            mainViewModel.inCartProduct.observe(this@CourseDetailsActivity) { cartItem ->
                setupCounter(cartItem.qty, binding.tvQty, binding.btIncrease, binding.btDecrease)
                tvQty.text = cartItem.qty

                btAddToCart.setOnClickListener {
                    val intent = Intent(this@CourseDetailsActivity, HomeMainActivity::class.java)
                    intent.putExtra(Constants.GO_TO_CART, Constants.CART_REF)
                    if(course.offerPrice == "0") {
                        val intents = Intent(this@CourseDetailsActivity, EnrollCourseActivity::class.java)
                        intents.putExtra(Constants.COURSE_REF, course.id)
                        startActivity(intents)
                    } else if(cartItem.qty != tvQty.text && btAddToCart.text == Constants.GO_TO_CART) {
                        mainViewModel.updateQty(user.userId, cartItem.productId, tvQty.text.toString())
                        startActivity(intent)
                    } else if(btAddToCart.text == Constants.GO_TO_CART) {
                        startActivity(intent)
                    } else {
                        val cartId = Firebase.database.getReference(Constants.CART_REF).push().key
                        val cart = CartModel(cartId!!, course.id, course.image,
                            course.courseName, tvQty.text.toString(),
                            course.offerPrice, course.originalPrice, type = Constants.COURSE)

                        mainViewModel.addToCart(user.userId, cart)
                    }
                }

            }

            mainViewModel.isInCart.observe(this@CourseDetailsActivity) { success ->
                if (success) {
                    mainViewModel.isInCart(user.userId, course.id)
                } else {
                    btAddToCart.text = Constants.ADD_TO_CART
                    Utils.showMessage(this@CourseDetailsActivity, "Something went wrong")
                }
                if(course.offerPrice == "0") {
                    btAddToCart.text = "Open Course"
                }
            }

            mainViewModel.alreadyInCart.observe(this@CourseDetailsActivity) { success ->
                if (success) {
                    loadingLayout.gone()
                    mainLayout.visible()
                    btAddToCart.text = Constants.GO_TO_CART
                    btAddToCart.isEnabled = true
                } else {
                    loadingLayout.gone()
                    mainLayout.visible()
                    btAddToCart.text = Constants.ADD_TO_CART
                    btAddToCart.isEnabled = true
                }

                if(course.offerPrice == "0") {
                    btAddToCart.text = "Open Course"
                }
            }


            btAddToCart.setOnClickListener {
                val intent = Intent(this@CourseDetailsActivity, HomeMainActivity::class.java)
                intent.putExtra(Constants.GO_TO_CART, Constants.CART_REF)
                if(btAddToCart.text == "Open Course") {
                    val intents = Intent(this@CourseDetailsActivity, EnrollCourseActivity::class.java)
                    intents.putExtra(Constants.COURSE_REF, course.id)
                    startActivity(intents)
                } else if(btAddToCart.text == Constants.GO_TO_CART) {
                    startActivity(intent)
                } else {
                    val cartId = Firebase.database.getReference(Constants.CART_REF).push().key
                    val cart = CartModel(cartId!!, course.id, course.image,
                        course.courseName, tvQty.text.toString(),
                        course.offerPrice, course.originalPrice, type = Constants.COURSE)

                    mainViewModel.addToCart(user.userId, cart)
                }
            }


        }

    }

    private fun setDetails(model: CourseModel) {
        binding.apply {
            ivImage.load(model.image) {
                placeholder(R.drawable.placeholder)
                error(R.drawable.placeholder)
            }

            tvOriginalPrice.paintFlags = tvOriginalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            tvTitle.text = model.courseName
            if(model.startDate.isEmpty()) tvCourseDate.gone() else tvCourseDate.visible()
            tvCourseDate.text = "• Starts on ${model.startDate} • Ends on ${model.endDate}"
            tvDescription.text = model.courseDescription
            tvOfferPrice.text = if(model.offerPrice == "0") "Free" else "₹${ model.offerPrice }"
            tvOriginalPrice.text = "₹${model.originalPrice}"
            val dc = Utils.calculateDiscount(model.offerPrice.toInt(), model.originalPrice.toInt())
            tvPercentage.text = "${dc.toInt()}% Off"
            if(model.offerPrice == "0") tvPercentage.gone() else tvPercentage.visible()


            if(course.offerPrice == "0") {
              btAddToCart.text = "Open Course"
            }

        }

    }


    private fun addNextLineAfterFullStop(text: String): String {
        val lines = text.split("*").map { it.trim() }.filter { it.isNotEmpty() }
        val newText = StringBuilder()
        lines.forEachIndexed { index, line ->
            newText.append("• $line")
            if (index < lines.size - 1) {
                newText.append("\n")
            }
        }
        return newText.toString()
    }


    private fun setupCounter(initialCount: String, textView: TextView, increaseButton: ImageView, decreaseButton: ImageView) {
        var count = initialCount.toIntOrNull() ?: 1

        textView.text = count.toString()

        increaseButton.setOnClickListener {
            count++
            textView.text = count.toString()
        }

        decreaseButton.setOnClickListener {
            if (count > 1) {
                count--
                textView.text = count.toString()
            }
        }
    }


}