package com.psi.dpsi.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.psi.dpsi.model.CourseModel
import com.psi.dpsi.model.CartModel
import com.psi.dpsi.model.NotesModel
import com.psi.dpsi.model.OrderModel
import com.psi.dpsi.model.UserModel
import com.psi.dpsi.utils.Constants
import com.psi.dpsi.utils.SharedPref
import com.psi.dpsi.utils.Utils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainRepository(context: Context) {
    private val database = FirebaseDatabase.getInstance()
    private val fireStore = FirebaseFirestore.getInstance()

    private val user = SharedPref.getUserData(context) ?: UserModel()
    private val serviceRef = database.getReference(Constants.SERVICE_REF)
    private val cartRef = database.getReference(Constants.CART_REF)
    private val orderRef = database.getReference(Constants.ORDERS_REF)
    private val courseRef = database.getReference(Constants.COURSE_REF)
    private val notesRef = database.getReference(Constants.NOTES_REF)
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    val status: MutableLiveData<Boolean> = MutableLiveData()
    val isInCart: MutableLiveData<Boolean> = MutableLiveData()
    val alreadyInCart: MutableLiveData<Boolean> = MutableLiveData()
    val orderPlaced: MutableLiveData<Boolean> = MutableLiveData()
    val inCartProduct: MutableLiveData<CartModel> = MutableLiveData()

    private val _cartItems: MutableLiveData<List<CartModel>> = MutableLiveData()
    val cartItemsList: LiveData<List<CartModel>> = _cartItems

    private val _notesList = MutableLiveData<List<NotesModel>>()
    val notesList: LiveData<List<NotesModel>> = _notesList

    private val _courseList = MutableLiveData<List<CourseModel>>()
    val courseList: LiveData<List<CourseModel>> = _courseList

    private val _orderHistory = MutableLiveData<List<CartModel>>()
    val orderHistory: LiveData<List<CartModel>> = _orderHistory

    private val _ordersList: MutableLiveData<List<CartModel>> = MutableLiveData()
    val pendingOrdersList: LiveData<List<CartModel>> = _ordersList



    init {
        fetchCartItems(user.userId)
    }

    init {
        coroutineScope.launch {
            getHistory(user.userId)
        }
    }

    init {
        coroutineScope.launch {
            fetchUserOrders(user.userId)
        }
    }




    fun fetchCourse() {
        try {
            val list = mutableListOf<CourseModel>()
            courseRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()) {
                        list.clear()
                        for (childSnapshot in snapshot.children) {
                            val course = childSnapshot.getValue(CourseModel::class.java)
                            course?.let { list.add(it) }
                        }
                        _courseList.postValue(list)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    status.postValue(false)
                }

            })


        } catch (e: Exception) {
            status.postValue(false)
            println("Error fetching services: ${e.message}")
        }
    }

    fun fetchNotes() {
        try {
            val list = mutableListOf<NotesModel>()
            notesRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()) {
                        list.clear()
                        for (childSnapshot in snapshot.children) {
                            val course = childSnapshot.getValue(NotesModel::class.java)
                            course?.let { list.add(it) }
                        }
                        _notesList.postValue(list)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    status.postValue(false)
                }

            })


        } catch (e: Exception) {
            status.postValue(false)
            println("Error fetching services: ${e.message}")
        }
    }



    private fun fetchUserOrders(userId: String) {
        try {
            val orderList = mutableListOf<CartModel>()
            orderRef.child(userId).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()) {
                        orderList.clear()
                        for (childSnapshot in snapshot.children) {
                            val order = childSnapshot.getValue(OrderModel::class.java)
                            order?.let { orderList.addAll(it.orderedItems) }
                        }
                        _ordersList.postValue(orderList)
                    } else {
                        _ordersList.postValue(emptyList())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    _ordersList.postValue(emptyList())
                }

            })
        } catch (e: Exception) {
            _ordersList.postValue(emptyList())
        }

    }


    suspend fun addToCart(userId: String, cart: CartModel) {
        try {
            cartRef.child(userId).child(cart.productId).setValue(cart)
                .addOnSuccessListener {
                isInCart(userId, cart.productId)
            }.addOnFailureListener {
                isInCart.postValue(false)
            }.await()

        } catch (e: Exception) {
            isInCart.postValue(false)
        }
    }

    private fun fetchCartItems(userId: String) {
        try {
            val cartList = mutableListOf<CartModel>()
            cartRef.child(userId).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    cartList.clear()
                    if (snapshot.exists()) {
                        for (childSnapshot in snapshot.children) {
                            val cart = childSnapshot.getValue(CartModel::class.java)
                            cart?.let { cartList.add(it) }
                        }
                    }
                    _cartItems.postValue(cartList)
                }

                override fun onCancelled(error: DatabaseError) {
                    _cartItems.postValue(emptyList())
                }
            })
        } catch (e: Exception) {
            _cartItems.postValue(emptyList())
        }
    }

    fun isInCart(userId: String, productId: String) {
        cartRef.child(userId).child(productId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()) {
                    val product = snapshot.getValue(CartModel::class.java) ?: CartModel()
                    inCartProduct.postValue(product)
                    alreadyInCart.postValue(true)
                } else {
                    alreadyInCart.postValue(false)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }


    suspend fun removeFromCart(userId: String, productId: String) {
        cartRef.child(userId).child(productId).removeValue().await()
    }

    suspend fun placeOrder(userId: String, date: String, amount: String, orders: List<CartModel>) {
        try {
            val orderId = orderRef.push().key
            orders.forEach {
                it.orderDate = Utils.getCurrentDateTime()
            }
            val order = orderId?.let { OrderModel(it, userId, date, amount, orders) }
            if (orderId != null) {
                orderRef.child(userId).child(orderId).setValue(order)
                    .addOnSuccessListener {
                        coroutineScope.launch {
                            clearCart(userId)
                        }
                    }.addOnFailureListener {
                        orderPlaced.postValue(false)
                    }.await()
            }

        } catch (e: Exception) {
            orderPlaced.postValue(false)
            println("Error placing order: ${e.message}")
        }
    }

    private suspend fun clearCart(userId: String) {
        cartRef.child(userId).removeValue().addOnSuccessListener {
            orderPlaced.postValue(true)
        }.await()

    }

    suspend fun updateQty(userId: String, productId: String, qty: String) {
        val newQty = mapOf<String, Any>("qty" to qty)
        cartRef.child(userId).child(productId).updateChildren(newQty).await()

    }

    private suspend fun getHistory(userId: String) {
       try {
           val history = ArrayList<CartModel>()
           val documents = fireStore.collection(Constants.ORDERS_HISTORY_REF).document(userId).collection(Constants.ORDERS_HISTORY_REF)
               .get().await()

           history.addAll(documents.mapNotNull { it.toObject(CartModel::class.java) })
           if(history.isNotEmpty()) {
               _orderHistory.postValue(history)
           } else {
               _orderHistory.postValue(emptyList())
           }
       } catch (e: Exception) {
           _orderHistory.postValue(emptyList())
       }
    }



}



