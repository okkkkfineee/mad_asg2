package com.utarproject.eventu;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileDisplayActivity extends AppCompatActivity {

    private TextView nameTextView, studentIdTextView, emailTextView, phoneTextView, campusTextView, roleTextView;
    private Button editProfileButton, createEventButton, displayEventButton, homeButton;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_display);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();

        // Initialize views
        nameTextView = findViewById(R.id.nameTextView);
        studentIdTextView = findViewById(R.id.studentIdTextView);
        emailTextView = findViewById(R.id.emailTextView);
        phoneTextView = findViewById(R.id.phoneTextView);
        campusTextView = findViewById(R.id.campusTextView);
        roleTextView = findViewById(R.id.roleTextView);
        editProfileButton = findViewById(R.id.editProfileButton);
        createEventButton = findViewById(R.id.createEventButton);
        displayEventButton = findViewById(R.id.displayEventButton);
        homeButton = findViewById(R.id.homeButton);

        // Set the TextView fields to be read-only
        nameTextView.setFocusable(false);
        studentIdTextView.setFocusable(false);
        emailTextView.setFocusable(false);
        phoneTextView.setFocusable(false);
        campusTextView.setFocusable(false);
        roleTextView.setFocusable(false);

        // Fetch user details from Firestore
        if (firebaseUser != null) {
            db.collection("users").document(firebaseUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            currentUser = documentSnapshot.toObject(User.class);
                            if (currentUser != null) {
                                // Prepopulate the fields with the current user data
                                nameTextView.setText("Name:\n\n" + "\t\t" + currentUser.getName());
                                studentIdTextView.setText("Student ID:\n\n" + "\t\t" + currentUser.getStuId());
                                emailTextView.setText("Email:\n\n" + "\t\t" + currentUser.getEmail());
                                phoneTextView.setText("Phone Number:\n\n" + "\t\t" + currentUser.getPhoneNo());
                                campusTextView.setText("Campus:\n\n" + "\t\t" + currentUser.getCampus());
                                roleTextView.setText("Role:\n\n" + "\t\t" + currentUser.getRole());

                                // Show create button only for organizers
                                if ("Organizer".equals(currentUser.getRole())) {
                                    createEventButton.setVisibility(View.VISIBLE);
                                    displayEventButton.setVisibility(View.VISIBLE);
                                    createEventButton.setOnClickListener(v -> {
                                        // TODO: Navigate to create event activity
                                        // Toast.makeText(ProfileDisplayActivity.this, "Create Event functionality coming soon!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(ProfileDisplayActivity.this, addEventActivity.class)); // Close the activity and return to the home screen
                                    });
                                    displayEventButton.setOnClickListener(v -> {
                                        // TODO: Navigate to display event activity
                                        // Toast.makeText(ProfileDisplayActivity.this, "Display Event functionality coming soon!", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(ProfileDisplayActivity.this, displayEventActivity.class)); // Close the activity and return to the home screen
                                    });
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(ProfileDisplayActivity.this, "Failed to fetch user details", Toast.LENGTH_SHORT).show());
        }

        // Save changes when the save button is clicked
        editProfileButton.setOnClickListener(v -> editProfile());
        // Back to button to get  back to home screen
        homeButton.setOnClickListener(v -> getBackHome());
    }

    private void editProfile() {
        // Set an OnClickListener to navigate to the EditProfileActivity when clicked
        Intent intent = new Intent(ProfileDisplayActivity.this, EditProfileActivity.class);
        startActivity(intent);
    }

    private void getBackHome() {
        // Set an OnClickListener to navigate to the HomeActivity when clicked
        Intent intent = new Intent(ProfileDisplayActivity.this, HomeActivity.class);
        startActivity(intent);
    }
}
