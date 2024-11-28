package com.xql.safehaven;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.xql.safehaven.databinding.FragmentProfileBinding;

public class ProfileFragment extends Fragment {

    FragmentProfileBinding binding;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    FirebaseUser currentUser;
    String userId, userDocId, userEmail;
    Boolean isActive = false, isNameActive = false;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentProfileBinding.inflate(getLayoutInflater());
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            userId = currentUser.getUid();
        }

        binding.logout.setVisibility(View.VISIBLE);
        binding.editNameLayout.setVisibility(View.GONE);
        binding.submitName.setVisibility(View.GONE);
        binding.cardChangeName.setOnClickListener(v -> {
            if(isNameActive){
                isNameActive = false;
                binding.editNameLayout.setVisibility(View.GONE);
                binding.submitName.setVisibility(View.GONE);
            }
            else{
                isNameActive = true;
                binding.editNameLayout.setVisibility(View.VISIBLE);
                binding.submitName.setVisibility(View.VISIBLE);
            }

            if(isNameActive || isActive){
                binding.logout.setVisibility(View.GONE);
            }
            else{
                binding.logout.setVisibility(View.VISIBLE);
            }
        });

        binding.changeInfo.setVisibility(View.GONE);
        binding.submit.setVisibility(View.GONE);
        binding.cardChangePassword.setOnClickListener(v -> {
            if(isActive){
                isActive = false;
                binding.changeInfo.setVisibility(View.GONE);
                binding.submit.setVisibility(View.GONE);
            }
            else{
                isActive = true;
                binding.changeInfo.setVisibility(View.VISIBLE);
                binding.submit.setVisibility(View.VISIBLE);
            }
            if(isNameActive || isActive){
                binding.logout.setVisibility(View.GONE);
            }
            else{
                binding.logout.setVisibility(View.VISIBLE);
            }
        });

        db.collection("users").get().addOnCompleteListener(task -> {
           if(task.isSuccessful()){
               QuerySnapshot querySnapshot = task.getResult();
               for(QueryDocumentSnapshot document : querySnapshot){
                   String uid = document.getString("userId");
                   String name = document.getString("name");

                   userDocId = document.getId();
                   if(userId.equals(uid)){
                       userEmail = document.getString("email");
                       binding.nameText.setText(name);
                       binding.editName.setText(name);
                       binding.changeInfo.setText("Password reset link will be sent to: "+userEmail);
                   }
               }
           }
        });

        binding.submitName.setOnClickListener(v -> {
            binding.nameProgressBar.setVisibility(View.VISIBLE);
            String newName = binding.editName.getText().toString().trim();
            if(newName.isEmpty()){
                binding.editName.setError("Name should not be empty.");
                binding.editName.requestFocus();
            }
            else{
                db.collection("users").document(userDocId).update("name", newName).addOnCompleteListener(task -> {
                    Toast.makeText(requireContext(), "Name updated successfully.", Toast.LENGTH_SHORT).show();
                    binding.nameText.setText(newName);
                    binding.editName.setText(newName);
                    isNameActive = false;
                    binding.editNameLayout.setVisibility(View.GONE);
                    binding.submitName.setVisibility(View.GONE);
                });
            }
            binding.nameProgressBar.setVisibility(View.GONE);
        });

        binding.submit.setOnClickListener(v -> {
            binding.progressBar.setVisibility(View.VISIBLE);
            mAuth.sendPasswordResetEmail(userEmail)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(requireContext(), "Password reset link is sent to "+userEmail, Toast.LENGTH_SHORT).show();
                                binding.progressBar.setVisibility(View.GONE);
                            }
                        }
                    }).addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        binding.progressBar.setVisibility(View.GONE);
                    });
        });

        binding.logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(requireContext(), MainActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });


        return binding.getRoot();
    }
}