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

import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileDisplayActivity extends BaseActivity {

    private TextView nameTextView, studentIdTextView, emailTextView, phoneTextView, campusTextView, roleTextView;
    private Button editProfileButton, createEventButton, displayEventButton, logoutButton;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private User currentUser;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_display);

        // Initialize Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

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
        logoutButton = findViewById(R.id.logoutButton);

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
                                nameTextView.setText(currentUser.getName());
                                studentIdTextView.setText(currentUser.getStuId());
                                emailTextView.setText(currentUser.getEmail());
                                phoneTextView.setText(currentUser.getPhoneNo());
                                campusTextView.setText(currentUser.getCampus());
                                roleTextView.setText(currentUser.getRole());

                                // Show create button only for organizers
                                if ("Organizer".equals(currentUser.getRole())) {
                                    createEventButton.setVisibility(View.VISIBLE);
                                    displayEventButton.setVisibility(View.VISIBLE);
                                    createEventButton.setOnClickListener(v -> {
                                        startActivity(new Intent(ProfileDisplayActivity.this, addEventActivity.class));
                                    });
                                    displayEventButton.setOnClickListener(v -> {
                                        startActivity(new Intent(ProfileDisplayActivity.this, displayEventActivity.class));
                                    });
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(ProfileDisplayActivity.this, "Failed to fetch user details", Toast.LENGTH_SHORT).show());
        }

        // Save changes when the save button is clicked
        editProfileButton.setOnClickListener(v -> editProfile());

        // Setup logout button
        logoutButton.setOnClickListener(v -> showLogoutConfirmation());

        // Setup bottom navigation with profile selected
        setupBottomNavigation(R.id.navigation_profile);
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> performLogout())
                .setNegativeButton("No", null)
                .show();
    }

    private void performLogout() {
        // Sign out from Google and revoke access to force account chooser next time
        googleSignInClient.signOut().addOnCompleteListener(this, task -> {
            googleSignInClient.revokeAccess().addOnCompleteListener(revokeTask -> {
                // Sign out from Firebase
                mAuth.signOut();
                
                // Show signout message
                Toast.makeText(this, "Sign out successful", Toast.LENGTH_SHORT).show();

                // Redirect to login activity
                Intent intent = new Intent(ProfileDisplayActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        });
    }

    private void editProfile() {
        Intent intent = new Intent(ProfileDisplayActivity.this, EditProfileActivity.class);
        startActivity(intent);
    }
}
