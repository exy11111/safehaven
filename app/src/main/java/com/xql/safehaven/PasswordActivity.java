package com.xql.safehaven;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.xql.safehaven.databinding.ActivityPasswordBinding;

public class PasswordActivity extends AppCompatActivity {

    ActivityPasswordBinding binding;
    FirebaseAuth mAuth;
    FirebaseFirestore db;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), HomepageActivity.class);
            startActivity(intent);
            finish();
        }

        Intent getIntent = getIntent();
        String email = getIntent.getStringExtra("email");

        binding.login.setOnClickListener(v -> {
            binding.progressBar.setVisibility(View.VISIBLE);

            String password = binding.pin.getText().toString().trim();

            if (password.isEmpty()) {
                binding.progressBar.setVisibility(View.GONE);
                binding.pin.setError("Password cannot be empty.");
                binding.pin.requestFocus();
            }
            else if(password.length() < 6){
                binding.progressBar.setVisibility(View.GONE);
                binding.pin.setError("Password cannot be less than 6 digits.");
                binding.pin.requestFocus();
            } else{
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(PasswordActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                                    binding.progressBar.setVisibility(View.GONE);
                                    Intent intent = new Intent(getApplicationContext(), HomepageActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    binding.progressBar.setVisibility(View.GONE);
                                    binding.pin.setError("Invalid email or password.");
                                    binding.pin.requestFocus();
                                }
                            }
                        }).addOnFailureListener(e -> {
                            String errorMessage = e.getMessage();
                            Toast.makeText(PasswordActivity.this, "Login failed: " + errorMessage, Toast.LENGTH_LONG).show();
                            binding.progressBar.setVisibility(View.GONE);
                        });
            }
        });

        binding.back.setOnClickListener(v -> {
            finish();
        });
    }
}