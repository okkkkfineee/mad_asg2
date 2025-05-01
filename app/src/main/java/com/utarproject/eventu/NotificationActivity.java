package com.utarproject.eventu;

import android.os.Bundle;

public class NotificationActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        // Setup bottom navigation with notification selected
        setupBottomNavigation(R.id.navigation_notification);
    }
} 