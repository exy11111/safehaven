package com.xql.safehaven;

import android.app.Activity;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.xql.safehaven.databinding.ActivityHomepageBinding;

public class HomepageActivity extends AppCompatActivity {

    ActivityHomepageBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityHomepageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        if(savedInstanceState == null){
            loadFragment(new HomeFragment());
        }

        binding.bottomNav.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            if(item.getItemId() == R.id.nav_home){
                selectedFragment = new HomeFragment();
            }
            else if(item.getItemId() == R.id.nav_history){
                selectedFragment = new HistoryFragment();
            }
            else if(item.getItemId() == R.id.nav_notes){
                selectedFragment = new HomeFragment();
            }
            else if(item.getItemId() == R.id.nav_profile){
                selectedFragment = new HomeFragment();
            }

            return loadFragment(selectedFragment);
        });
    }

    private boolean loadFragment(Fragment fragment){
        if(fragment != null){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}