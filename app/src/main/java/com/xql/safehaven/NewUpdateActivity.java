package com.xql.safehaven;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.xql.safehaven.databinding.ActivityNewUpdateBinding;

public class NewUpdateActivity extends AppCompatActivity {

    ActivityNewUpdateBinding binding;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityNewUpdateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Intent getIntent = getIntent();
        String apkUrl = getIntent.getStringExtra("apkUrl");

        binding.confirm.setOnClickListener(v -> {
            downloadApk(apkUrl);
        });
    }

    private void downloadApk(String apkUrl) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(apkUrl));
        startActivity(intent);
    }
}