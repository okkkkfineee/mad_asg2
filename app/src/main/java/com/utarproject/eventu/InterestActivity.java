package com.utarproject.eventu;

import android.os.Bundle;

public class InterestActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interest);

        // Setup bottom navigation with interest selected
        setupBottomNavigation(R.id.navigation_interest);
    }
} 