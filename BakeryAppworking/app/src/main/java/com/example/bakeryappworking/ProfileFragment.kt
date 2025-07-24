package com.example.bakeryappworking

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
// import android.widget.Toast
import androidx.fragment.app.Fragment
// import com.google.android.material.button.MaterialButton

class ProfileFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Commented out for test layout
        // view.findViewById<MaterialButton>(R.id.edit_profile_button).setOnClickListener {
        //     Toast.makeText(requireContext(), "Edit profile clicked", Toast.LENGTH_SHORT).show()
        //     // TODO: Implement edit profile functionality
        // }
    }
} 