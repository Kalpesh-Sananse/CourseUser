package com.psi.dpsi.activities

import android.content.Intent
import android.graphics.Paint
import android.net.Uri
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
import com.psi.dpsi.databinding.ActivityNotesDetailsBinding
import com.psi.dpsi.factory.MainViewModelFactory
import com.psi.dpsi.model.CartModel
import com.psi.dpsi.model.NotesModel
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

class NotesDetailsActivity : AppCompatActivity() {
    private val binding by lazy { ActivityNotesDetailsBinding.inflate(layoutInflater) }
    private lateinit var notes: NotesModel
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

        notes = intent.getParcelableExtra<NotesModel>(Constants.NOTES_REF) ?: NotesModel()

        setDetails(notes)
        setupCounter("1", binding.tvQty, binding.btIncrease, binding.btDecrease)
        user = SharedPref.getUserData(this) ?: UserModel()


        val repository = MainRepository(this)
        val factory = MainViewModelFactory(repository)
        mainViewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]


        binding.apply {

            loadingLayout.visible()
            mainLayout.gone()

            mainViewModel.isInCart(user.userId, notes.id)

            mainViewModel.inCartProduct.observe(this@NotesDetailsActivity) { cartItem ->
                setupCounter(cartItem.qty, binding.tvQty, binding.btIncrease, binding.btDecrease)
                tvQty.text = cartItem.qty

                btAddToCart.setOnClickListener {
                    val intent = Intent(this@NotesDetailsActivity, HomeMainActivity::class.java)
                    intent.putExtra(Constants.GO_TO_CART, Constants.CART_REF)
                    if(btAddToCart.text == "Download Notes") {
                        try {
                            val intents = Intent(Intent.ACTION_VIEW)
                            intents.data = Uri.parse(notes.notesUrl)
                            startActivity(intents)
                        } catch (e: Exception) {
                            print(e.stackTrace)
                        }
                    } else if(cartItem.qty != tvQty.text && btAddToCart.text == Constants.GO_TO_CART) {
                        mainViewModel.updateQty(user.userId, cartItem.productId, tvQty.text.toString())
                        startActivity(intent)
                        finish()
                    } else if(btAddToCart.text == Constants.GO_TO_CART) {
                        startActivity(intent)
                        finish()
                    } else {
                        val cartId = Firebase.database.getReference(Constants.CART_REF).push().key
                        val cart = CartModel(cartId!!, notes.id, notes.image,
                            notes.name, tvQty.text.toString(),
                            notes.offerPrice, notes.originalPrice, type = Constants.NOTES)

                        mainViewModel.addToCart(user.userId, cart)
                    }
                }

            }

            mainViewModel.isInCart.observe(this@NotesDetailsActivity) { success ->
                if (success) {
                    mainViewModel.isInCart(user.userId, notes.id)
                } else {
                    btAddToCart.text = Constants.ADD_TO_CART
                    Utils.showMessage(this@NotesDetailsActivity, "Something went wrong")
                }
                if(notes.offerPrice == "0") {
                    btAddToCart.text = "Download Notes"
                }
            }

            mainViewModel.alreadyInCart.observe(this@NotesDetailsActivity) { success ->
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

                if(notes.offerPrice == "0") {
                    btAddToCart.text = "Download Notes"
                }
            }


            btAddToCart.setOnClickListener {
                if(btAddToCart.text == "Download Notes") {
                    try {
                        val intents = Intent(Intent.ACTION_VIEW)
                        intents.data = Uri.parse(notes.notesUrl)
                        startActivity(intents)
                    } catch (e: Exception) {
                        print(e.stackTrace)
                    }
                } else if(btAddToCart.text == Constants.GO_TO_CART) {
                    val intent = Intent(this@NotesDetailsActivity, HomeMainActivity::class.java)
                    intent.putExtra(Constants.GO_TO_CART, Constants.CART_REF)
                    startActivity(intent)
                    finish()

                } else {
                    val cartId = Firebase.database.getReference(Constants.CART_REF).push().key
                    val cart = CartModel(cartId!!, notes.id, notes.image,
                        notes.name, tvQty.text.toString(),
                        notes.offerPrice, notes.originalPrice, type = Constants.NOTES)

                    mainViewModel.addToCart(user.userId, cart)

                }
            }


        }


        onBackPressedDispatcher.addCallback(this@NotesDetailsActivity, object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                val intent = Intent(this@NotesDetailsActivity, HomeMainActivity::class.java)
                intent.putExtra(Constants.GO_TO_CART, Constants.NOTES_REF)
                startActivity(intent)
                finish()
            }

        })

    }

    private fun setDetails(model: NotesModel) {
        binding.apply {
            ivImage.load(model.image) {
                placeholder(R.drawable.placeholder)
                error(R.drawable.placeholder)
            }

            tvOriginalPrice.paintFlags = tvOriginalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            tvTitle.text = model.name
            tvDescription.text = model.description
            tvOfferPrice.text = if(model.offerPrice == "0") "Free" else "₹${ model.offerPrice }"
            tvOriginalPrice.text = "₹${model.originalPrice}"
            val dc = Utils.calculateDiscount(model.offerPrice.toInt(), model.originalPrice.toInt())
            tvPercentage.text = "${dc.toInt()}% Off"
            if(model.offerPrice == "0") tvPercentage.gone() else tvPercentage.visible()


            if(notes.offerPrice == "0") {
                btAddToCart.text = "Download Notes"
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