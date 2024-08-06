package com.psi.dpsi.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CourseContentModel(
    val id: String = "",
    val videoTitle: String = "",
    val thumbnail: String = "",
    val videoUrl: String = "",
    val notesUrl: String = ""
): Parcelable
