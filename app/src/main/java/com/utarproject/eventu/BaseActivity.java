package com.utarproject.eventu;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.app.ActivityOptions;

public abstract class BaseActivity extends AppCompatActivity {
    protected BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    protected void setupBottomNavigation(int selectedItemId) {
        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(selectedItemId);
        
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == selectedItemId) {
                return true;
            }

            Intent intent = null;
            if (item.getItemId() == R.id.navigation_home) {
                intent = new Intent(this, ViewEventActivity.class);
            } else if (item.getItemId() == R.id.navigation_interest) {
                intent = new Intent(this, InterestActivity.class);
            } else if (item.getItemId() == R.id.navigation_notification) {
                intent = new Intent(this, NotificationActivity.class);
            } else if (item.getItemId() == R.id.navigation_profile) {
                intent = new Intent(this, ProfileDisplayActivity.class);
            }

            if (intent != null) {
                ActivityOptions options = ActivityOptions.makeCustomAnimation(
                    this, android.R.anim.fade_in, android.R.anim.fade_out);
                startActivity(intent, options.toBundle());
                finish();
                return true;
            }
            return false;
        });
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
} 