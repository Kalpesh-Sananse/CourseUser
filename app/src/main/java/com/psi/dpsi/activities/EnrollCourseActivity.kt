package com.psi.dpsi.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import coil.load
import com.psi.dpsi.R
import com.psi.dpsi.adapter.ContentAdapter
import com.psi.dpsi.databinding.ActivityEnrollCourseBinding
import com.psi.dpsi.model.CourseContentModel
import com.psi.dpsi.model.CourseModel
import com.psi.dpsi.utils.Constants
import com.psi.dpsi.utils.Utils
import com.psi.dpsi.utils.Utils.gone
import com.psi.dpsi.utils.Utils.visible
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EnrollCourseActivity : AppCompatActivity(), ContentAdapter.OnItemClickListener {
    private val binding by lazy { ActivityEnrollCourseBinding.inflate(layoutInflater) }
    private val database = FirebaseDatabase.getInstance()
    private val courseRef = database.getReference(Constants.COURSE_REF)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        val course = intent.getStringExtra(Constants.COURSE_REF)!!

        fetchCourseById(course)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {

                finish()
            }

        })

    }

    private fun fetchCourseById(noteId: String) {
        try {
            val ref = courseRef.child(noteId)
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val note = snapshot.getValue(CourseModel::class.java)
                        note?.let {
                            setDetails(note)
                        }
                    } else {
                        Utils.showMessage(this@EnrollCourseActivity, "Something went wrong")
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        } catch (e: Exception) {
            println("Error fetching note: ${e.message}")
        }
    }

    private fun setDetails(course: CourseModel) {
        binding.apply {
            loadingLayout.gone()
            mainLayout.visible()

            ivCourseImage.load(course.image) {
                placeholder(R.drawable.placeholder)
                error(R.drawable.placeholder)
            }
            tvName.text = course.courseName
            tvDescription.text = course.courseDescription
            tvOriginalPrice.text = "₹ ${course.originalPrice}"
            tvOfferPrice.text = "₹ ${course.offerPrice}"
            btLiveClass.setOnClickListener {
                if(course.liveUrl.isNotEmpty()) {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(course.liveUrl)
                        startActivity(intent)
                    } catch (e: Exception) {
                        print(e.stackTrace)
                    }
                } else {
                    Utils.showMessage(this@EnrollCourseActivity, "No Live Class")
                }
            }

            btCourseNotes.setOnClickListener {
                if(course.notesUrl.isNotEmpty()) {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(course.notesUrl)
                        startActivity(intent)
                    } catch (e: Exception) {
                        print(e.stackTrace)
                    }
                } else {
                    Utils.showMessage(this@EnrollCourseActivity, "No Notes Available")
                }
            }
            val adapter = ContentAdapter(this@EnrollCourseActivity, this@EnrollCourseActivity)
            rv.adapter = adapter
            adapter.submitList(course.courseContent)


        }
    }

    override fun onItemClick(contentModel: CourseContentModel, index: Int) {
//        try {
//            val intent = Intent(Intent.ACTION_VIEW)
//            intent.data = Uri.parse(contentModel.videoUrl)
//            startActivity(intent)
//        } catch (e: Exception) {
//            print(e.stackTrace)
//        }
        val intent = Intent(this@EnrollCourseActivity, MediaPlayerActivity::class.java)
        intent.putExtra(Constants.COURSE, contentModel.videoUrl)
        startActivity(intent)
    }




}