package com.xql.safehaven;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.xql.safehaven.databinding.ActivityMainBinding;
import com.xql.safehaven.databinding.DialogNewupdateBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.SSLContext;

import firebase.com.protolitewrapper.BuildConfig;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    FirebaseAuth auth;
    FirebaseFirestore db;
    FirebaseUser currentUser;
    String email;
    public static final int CURRENT_VERSION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        checkForUpdates();

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), HomepageActivity.class);
            startActivity(intent);
            finish();
        }


        binding.loginLedy.setOnClickListener(v -> {
            binding.loginLedy.setVisibility(View.GONE);
            binding.progressBar.setVisibility(View.VISIBLE);
            login("ledy0612");
        });

        binding.loginExy.setOnClickListener(v -> {
            binding.loginExy.setVisibility(View.GONE);
            binding.progressBar.setVisibility(View.VISIBLE);
            login("exy0612");
        });
    }

    private void login(String user){
        Intent intent = new Intent(getApplicationContext(), PasswordActivity.class);
        db.collection("users").document(user).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                DocumentSnapshot document = task.getResult();
                email = document.getString("email");
                intent.putExtra("email", email);
                binding.loginLedy.setVisibility(View.VISIBLE);
                binding.loginExy.setVisibility(View.VISIBLE);
                binding.progressBar.setVisibility(View.GONE);
                startActivity(intent);
                finish();
            }
            else{
                Toast.makeText(this, "Unexpected error. Please try again.", Toast.LENGTH_SHORT).show();
                binding.loginLedy.setVisibility(View.VISIBLE);
                binding.loginExy.setVisibility(View.VISIBLE);
                binding.progressBar.setVisibility(View.GONE);
            }

        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            binding.loginLedy.setVisibility(View.VISIBLE);
            binding.loginExy.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
        });

    }

    private void checkForUpdates() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("version")
                .document("latestversion")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            int latestVersion = document.getLong("version").intValue();
                            String apkUrl = document.getString("apk_url");
                            String description = document.getString("description");
                            if (latestVersion > CURRENT_VERSION) {
                                Intent intent = new Intent(this, NewUpdateActivity.class);
                                intent.putExtra("apkUrl", apkUrl);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }
                        }
                    } else {
                        Toast.makeText(this, "Failed to check for updates!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}