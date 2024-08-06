package com.psi.dpsi.model

data class CartModel(
    val id: String = "",
    val productId: String = "",
    val productImage: String = "",
    val title: String = "",
    var qty: String = "",
    var price: String = "",
    var originalPrice: String = "",
    var orderDate: String = "",
    var type: String = ""

)
