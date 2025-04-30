package com.utarproject.eventu;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    private Button editProfileButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        editProfileButton = findViewById(R.id.editProfileButton);

        // Set an OnClickListener to navigate to the EditProfileActivity when clicked
        editProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProfileDisplayActivity.class);
            startActivity(intent);
        });
    }
}
