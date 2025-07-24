package com.example.bakeryappworking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HomeFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.bakery_recycler)
        setupRecyclerView()

        // Setup categories
        val categoryContainer = view.findViewById<LinearLayout>(R.id.category_container)
        val categories = listOf("Cakes", "Donuts", "Cookies", "Bread")
        categories.forEach { cat ->
            val tv = TextView(requireContext())
            tv.text = cat
            tv.setPadding(32, 8, 32, 8)
            tv.setBackgroundResource(android.R.drawable.btn_default)
            tv.setOnClickListener {
                Toast.makeText(requireContext(), "$cat clicked", Toast.LENGTH_SHORT).show()
            }
            categoryContainer.addView(tv)
        }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        val controller = AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.layout_fade_in)
        recyclerView.layoutAnimation = controller
        // Sample adapter for demonstration
        recyclerView.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val tv = TextView(parent.context)
                tv.text = "Product"
                tv.textSize = 18f
                tv.setPadding(32, 32, 32, 32)
                return object : RecyclerView.ViewHolder(tv) {}
            }
            override fun getItemCount() = 6
            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                (holder.itemView as TextView).text = "Product ${position + 1}"
            }
        }
        recyclerView.scheduleLayoutAnimation()
    }
} 