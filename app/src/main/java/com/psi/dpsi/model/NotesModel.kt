package com.psi.dpsi.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class NotesModel(
    val id: String = "",
    val image: String = "",
    val name: String = "",
    val description: String = "",
    val originalPrice: String = "",
    val offerPrice: String = "",
    val rating: String = "",
    val type: String = "",
    val notesUrl: String = "",
): Parcelable
