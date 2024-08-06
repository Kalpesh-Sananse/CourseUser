package com.psi.dpsi.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.psi.dpsi.R
import com.psi.dpsi.activities.HomeMainActivity
import com.psi.dpsi.adapter.CartAdapter
import com.psi.dpsi.databinding.DialogAnimationBinding
import com.psi.dpsi.databinding.FragmentCartBinding
import com.psi.dpsi.factory.MainViewModelFactory
import com.psi.dpsi.model.CartModel
import com.psi.dpsi.model.UserModel
import com.psi.dpsi.repository.MainRepository
import com.psi.dpsi.utils.Constants
import com.psi.dpsi.utils.SharedPref
import com.psi.dpsi.utils.Utils
import com.psi.dpsi.utils.Utils.gone
import com.psi.dpsi.utils.Utils.visible
import com.psi.dpsi.viewmodel.MainViewModel
import com.razorpay.Checkout
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import org.json.JSONObject


class CartFragment : Fragment(), CartAdapter.OnItemClickListener, PaymentResultWithDataListener {
    private val binding by lazy { FragmentCartBinding.inflate(layoutInflater) }
    private lateinit var mainViewModel: MainViewModel
    private lateinit var adapter: CartAdapter
    private lateinit var user: UserModel
    private lateinit var animationProgress: AlertDialog
    private lateinit var cartList: ArrayList<CartModel>
    private lateinit var checkoutProducts: ArrayList<CartModel>
//    private lateinit var productList: ArrayList<ProductModel>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = MainRepository(requireContext())
        val factory = MainViewModelFactory(repository)
        mainViewModel = ViewModelProvider(this@CartFragment, factory)[MainViewModel::class.java]
        adapter = CartAdapter(this@CartFragment)
        binding.rv.adapter = adapter
        animationProgress = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog).create()
        user = SharedPref.getUserData(requireContext()) ?: UserModel()
        cartList = ArrayList()
//        productList = ArrayList()
        checkoutProducts = ArrayList()

        binding.apply {

//            mainViewModel.servicesList.observe(viewLifecycleOwner) {
//                productList.addAll(it)
//            }

            mainViewModel.cartItemsList.observe(viewLifecycleOwner) { list->
                if (list.isNotEmpty()) {
                    cartList.clear()
                    cartList.addAll(list)
                    loadingLayout.gone()
                    mainLayout.visible()
                    adapter.submitList(list)
                    tvGrandTotal.text = "₹ ${calculateGrandTotal(adapter)}"
                    checkoutProducts.addAll(list)
                    btSwipeOrder.setOnActiveListener {
                        initPayment()
                    }

                } else {
                    loadingLayout.visible()
                    mainLayout.gone()
                    adapter.submitList(emptyList())
                    animationView3.setAnimation("empty_cart.json")
                    animationView3.playAnimation()
                    tvStatus.text = "Your Cart is Empty"
                }
            }

            mainViewModel.orderPlaced.observe(viewLifecycleOwner) { success ->
                if(success) {
                    animationProgress.dismiss()
                    showAnimationDialog("Purchase Success", "order_placed.json", true)
                } else {
                    Utils.showMessage(requireContext(), "Order Failed")
                }
            }

        }

    }


    private fun showAnimationDialog(title: String, animation: String, visible: Boolean) {
        animationProgress = AlertDialog.Builder(context, R.style.CustomAlertDialog).create()
        val processLayout = DialogAnimationBinding.inflate(LayoutInflater.from(context))
        processLayout.animationView3.setAnimation(animation)
        processLayout.animationView3.playAnimation()
        animationProgress.setView(processLayout.root)
        animationProgress.setCancelable(false)

        if(!visible) processLayout.btBack.gone() else processLayout.btBack.visible()

        processLayout.tvHeading.text = title

        processLayout.btBack.setOnClickListener {
            animationProgress.dismiss()
            val intent = Intent(requireActivity(), HomeMainActivity::class.java)
            intent.putExtra(Constants.GO_TO_CART, Constants.CATEGORY_REF)
            startActivity(intent)
        }

        animationProgress.show()
    }

    private fun calculateGrandTotal(adapter: CartAdapter): Int {
        var grandTotal = 0
        val itemList = adapter.currentList
        for (item in itemList) {
            val totalPrice = item.qty.toInt() * item.price.toInt()
            grandTotal += totalPrice
        }
        return grandTotal
    }

    override fun onPaymentSuccess(p0: String?, p1: PaymentData?) {
        showAnimationDialog("Processing..", "order_placed.json", false)
        mainViewModel.placeOrder(user.userId, Utils.getCurrentDateTime(), calculateGrandTotal(adapter).toString(), checkoutProducts)
        Utils.showMessage(requireContext(), "Payment success")

    }

    override fun onPaymentError(p0: Int, p1: String?, p2: PaymentData?) {
        Utils.showMessage(requireContext(), "Payment Failed")
    }

    private fun initPayment() {
        val activity: Activity = requireActivity()
        val co = Checkout()

        try {
            val options = JSONObject()
            options.put("name", getString(R.string.app_name))
            options.put("description", "DPSI Forensic")
            options.put("image", "https://drive.google.com/file/d/156UVj5-FHTfwTGn9guqO7r7vmpgVWfQh/view?usp=drivesdk")
            options.put("theme.color", "#4E74F9");
            options.put("currency", "INR");
            options.put("order_id", "order_DBJOWzybf0sJbb");
            val amountInPaise = (calculateGrandTotal(adapter) * 100)
            options.put("amount", amountInPaise)

            val retryObj = JSONObject();
            retryObj.put("enabled", true);
            retryObj.put("max_count", 4);
            options.put("retry", retryObj);

            val prefill = JSONObject()
            prefill.put("email", "gaurav.kumar@example.com")
            prefill.put("contact", "9876543210")

            options.put("prefill", prefill)
            co.open(activity, options)
        } catch (e: Exception) {
            Toast.makeText(activity, "Error in payment", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }

    }

    override fun removeFromCart(product: CartModel) {
        mainViewModel.removeFromCart(user.userId, product.productId)
        binding.rv.adapter = adapter
        adapter.submitList(cartList)
        binding.tvGrandTotal.text = "₹ ${calculateGrandTotal(adapter)}"
    }

    override fun onQuantityChanged(product: CartModel) {
        val updatedList = adapter.currentList.map {
            if (it.productId == product.productId) {
                it.copy(qty = product.qty)
            } else {
                it
            }
        }
        adapter.submitList(updatedList)
        binding.tvGrandTotal.text = "₹ ${calculateGrandTotal(adapter)}"

    }

}