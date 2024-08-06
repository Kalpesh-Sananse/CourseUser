package com.psi.dpsi.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class CourseModel(
    val id: String = "",
    val image: String = "",
    val courseName: String = "",
    val courseDescription: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val originalPrice: String = "",
    val offerPrice: String = "",
    val rating: String = "",
    val type: String = "",
    val liveUrl: String = "",
    val notesUrl: String = "",
    val courseContent: @RawValue MutableList<CourseContentModel> = mutableListOf()
): Parcelable
