package com.psi.dpsi.activities

import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import coil.load
import com.psi.dpsi.R
import com.psi.dpsi.databinding.ActivityNotesDownloadBinding
import com.psi.dpsi.model.NotesModel
import com.psi.dpsi.utils.Constants
import com.psi.dpsi.utils.Utils
import com.psi.dpsi.utils.Utils.gone
import com.psi.dpsi.utils.Utils.visible
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NotesDownloadActivity : AppCompatActivity() {
    private val binding by lazy { ActivityNotesDownloadBinding.inflate(layoutInflater) }
    private val database = FirebaseDatabase.getInstance()
    private val notesRef = database.getReference(Constants.NOTES_REF)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        val notes = intent.getStringExtra(Constants.NOTES_REF)!!

        fetchNoteById(notes)


        binding.apply {






        }


        onBackPressedDispatcher.addCallback(this@NotesDownloadActivity, object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                finish()
            }

        })

    }

    private fun fetchNoteById(noteId: String) {
        try {
            val noteRef = notesRef.child(noteId)
            noteRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val note = snapshot.getValue(NotesModel::class.java)
                        note?.let {
                            setDetails(note)
                        }
                    } else {
                        Utils.showMessage(this@NotesDownloadActivity, "Something went wrong")
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        } catch (e: Exception) {
            println("Error fetching note: ${e.message}")
        }
    }

    private fun setDetails(model: NotesModel) {
        binding.apply {
            loadingLayout.gone()
            mainLayout.visible()

            ivImage.load(model.image) {
                placeholder(R.drawable.placeholder)
                error(R.drawable.placeholder)
            }

            tvOriginalPrice.paintFlags = tvOriginalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            tvTitle.text = model.name
            tvDescription.text = addNextLineAfterFullStop(model.description)
            tvOfferPrice.text = "₹${model.offerPrice}"
            tvOriginalPrice.text = "₹${model.originalPrice}"
            val dc = Utils.calculateDiscount(model.offerPrice.toInt(), model.originalPrice.toInt())
            tvPercentage.text = "${dc.toInt()}% Off"


            btAddToCart.setOnClickListener {
                try {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(model.notesUrl)
                    startActivity(intent)
                } catch (e: Exception) {
                    print(e.stackTrace)
                }
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




}