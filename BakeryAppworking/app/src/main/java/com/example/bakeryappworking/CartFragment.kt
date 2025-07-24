package com.example.bakeryappworking

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class CartFragment : Fragment() {
    private var recyclerView: RecyclerView? = null
    private var emptyView: TextView? = null
    private var checkoutButton: MaterialButton? = null
    private val firebaseManager = FirebaseManager.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        try {
            return inflater.inflate(R.layout.fragment_cart, container, false)
        } catch (e: Exception) {
            Log.e("CartFragment", "Error inflating layout", e)
            return null
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            Log.d("CartFragment", "onViewCreated started")
            
            recyclerView = view.findViewById(R.id.cart_recycler)
            emptyView = view.findViewById(R.id.empty_cart_text)
            checkoutButton = view.findViewById(R.id.checkout_button)

            if (recyclerView == null || emptyView == null || checkoutButton == null) {
                Log.e("CartFragment", "Failed to find required views")
                return
            }

            setupRecyclerView()
            setupCheckoutButton()
            updateUI()
            
            Cart.addListener { updateUI() }
            
            try {
                Cart.loadItems()
            } catch (e: Exception) {
                Log.e("CartFragment", "Error loading cart items", e)
                Toast.makeText(requireContext(), "Error loading cart items", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("CartFragment", "Error in onViewCreated", e)
            Toast.makeText(requireContext(), "Error initializing cart", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        try {
            recyclerView?.let { rv ->
                rv.layoutManager = LinearLayoutManager(requireContext())
                try {
                    val controller = AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_fade_in)
                    rv.layoutAnimation = controller
                } catch (e: Exception) {
                    Log.e("CartFragment", "Error loading layout animation", e)
                }
                
                rv.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                        val tv = TextView(parent.context)
                        tv.text = "Cart Item"
                        tv.textSize = 18f
                        tv.setPadding(32, 32, 32, 32)
                        return object : RecyclerView.ViewHolder(tv) {}
                    }
                    override fun getItemCount() = 0
                    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                        (holder.itemView as TextView).text = "Cart Item ${position + 1}"
                    }
                }
                try {
                    rv.scheduleLayoutAnimation()
                } catch (e: Exception) {
                    Log.e("CartFragment", "Error scheduling layout animation", e)
                }
            }
        } catch (e: Exception) {
            Log.e("CartFragment", "Error in setupRecyclerView", e)
        }
    }

    private fun setupCheckoutButton() {
        try {
            checkoutButton?.let { button ->
                val scaleDown = AnimationUtils.loadAnimation(requireContext(), R.anim.button_scale)
                val scaleUp = AnimationUtils.loadAnimation(requireContext(), R.anim.button_scale_reverse)
                
                button.setOnTouchListener { view, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            view.startAnimation(scaleDown)
                            true
                        }
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            view.startAnimation(scaleUp)
                            true
                        }
                        else -> false
                    }
                }

                button.setOnClickListener {
                    try {
                        Log.d("CartFragment", "Checkout button clicked")
                        val items = Cart.getItems()
                        if (items.isEmpty()) {
                            Toast.makeText(requireContext(), "Your cart is empty", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        val totalAmount = Cart.getTotalPrice()
                        Log.d("CartFragment", "Creating order with total amount: $totalAmount")
                        val order = firebaseManager.createOrder(items, totalAmount)
                        
                        button.isEnabled = false
                        button.text = "Processing..."
                        
                        firebaseManager.saveOrder(order) { success ->
                            requireActivity().runOnUiThread {
                                button.isEnabled = true
                                button.text = "Proceed to Checkout"
                                
                                if (success) {
                                    Log.d("CartFragment", "Order saved successfully")
                                    Cart.clear()
                                    showOrderConfirmation()
                                } else {
                                    Log.e("CartFragment", "Failed to save order")
                                    Toast.makeText(requireContext(), "Failed to place order", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("CartFragment", "Error during checkout", e)
                        Toast.makeText(requireContext(), "Error during checkout: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("CartFragment", "Error in setupCheckoutButton", e)
        }
    }

    private fun showOrderConfirmation() {
        try {
            Log.d("CartFragment", "Showing order confirmation dialog")
            
            if (!isAdded) {
                Log.e("CartFragment", "Fragment not attached to activity")
                return
            }

            val dialog = Dialog(requireContext(), android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)
            dialog.setContentView(R.layout.dialog_order_confirmation)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.setCancelable(false)
            
            dialog.findViewById<Button>(R.id.btn_back_to_home).setOnClickListener {
                dialog.dismiss()
                startActivity(Intent(requireContext(), HomeActivity::class.java))
                requireActivity().finish()
            }
            
            // Show dialog on the main thread
            requireActivity().runOnUiThread {
                try {
                    dialog.show()
                } catch (e: Exception) {
                    Log.e("CartFragment", "Error showing dialog", e)
                    Toast.makeText(requireContext(), "Error showing confirmation dialog", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e("CartFragment", "Error in showOrderConfirmation", e)
            if (isAdded) {
                Toast.makeText(requireContext(), "Error showing confirmation dialog", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUI() {
        try {
            val items = Cart.getItems()
            if (items.isEmpty()) {
                recyclerView?.visibility = View.GONE
                emptyView?.visibility = View.VISIBLE
                checkoutButton?.visibility = View.GONE
            } else {
                recyclerView?.visibility = View.VISIBLE
                emptyView?.visibility = View.GONE
                checkoutButton?.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
            Log.e("CartFragment", "Error updating UI", e)
            Toast.makeText(requireContext(), "Error updating UI", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Cart.removeListener { updateUI() }
        recyclerView = null
        emptyView = null
        checkoutButton = null
    }
} 