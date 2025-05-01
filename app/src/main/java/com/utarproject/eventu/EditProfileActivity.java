package com.utarproject.eventu;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditProfileActivity extends AppCompatActivity {

    private TextView nameTextView, studentIdTextView, emailTextView;
    private EditText phoneNoEditText;
    private Spinner campusSpinner, roleSpinner;
    private Button saveButton;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();

        // Initialize views
        nameTextView = findViewById(R.id.nameTextView);
        studentIdTextView = findViewById(R.id.studentIdTextView);
        emailTextView = findViewById(R.id.emailTextView);
        phoneNoEditText = findViewById(R.id.phoneNoEditText);
        campusSpinner = findViewById(R.id.campusSpinner);
        roleSpinner = findViewById(R.id.roleSpinner);
        saveButton = findViewById(R.id.saveButton);

        // Set the TextView fields to be read-only
        nameTextView.setFocusable(false);
        studentIdTextView.setFocusable(false);
        emailTextView.setFocusable(false);

        // Populate campus spinner with predefined campuses
        ArrayAdapter<CharSequence> campusAdapter = ArrayAdapter.createFromResource(this,
                R.array.campus_array, android.R.layout.simple_spinner_item);
        campusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        campusSpinner.setAdapter(campusAdapter);

        // Populate campus spinner with predefined campuses
        ArrayAdapter<CharSequence> roleAdapter = ArrayAdapter.createFromResource(this,
                R.array.role_array, android.R.layout.simple_spinner_item);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(roleAdapter);

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
                                // Set phone number
                                phoneNoEditText.setText(currentUser.getPhoneNo());
                                // Set campus (this will be a string)
                                ArrayAdapter<CharSequence> adapter1 = (ArrayAdapter<CharSequence>) campusSpinner.getAdapter();
                                int campusPosition = adapter1.getPosition(currentUser.getCampus());
                                campusSpinner.setSelection(campusPosition);
                                // Set role (this will be a string)
                                ArrayAdapter<CharSequence> adapter2 = (ArrayAdapter<CharSequence>) roleSpinner.getAdapter();
                                int rolePosition = adapter2.getPosition(currentUser.getRole());
                                roleSpinner.setSelection(rolePosition);
                            }
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(EditProfileActivity.this, "Failed to fetch user details", Toast.LENGTH_SHORT).show());
        }

        // Save changes when the save button is clicked
        saveButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Keep Changes?")
                    .setMessage("Are you sure you want to change your profile?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Go back to the previous page
                            saveProfile();
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    private void saveProfile() {
        String campus = campusSpinner.getSelectedItem().toString();
        String role = roleSpinner.getSelectedItem().toString();
        String phoneNo = phoneNoEditText.getText().toString();

        if (!campus.isEmpty() && !role.isEmpty() && !phoneNo.isEmpty()) {
            if (firebaseUser != null) {
                String uid = firebaseUser.getUid();
                // Update the user document with new campus and phone number
                db.collection("users").document(uid)
                        .update("campus", campus, "role", role, "phoneNo", phoneNo)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(EditProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(EditProfileActivity.this, ViewEventActivity.class); // Close the activity and return to the home screen
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        })
                        .addOnFailureListener(e -> Toast.makeText(EditProfileActivity.this, "Error updating profile", Toast.LENGTH_SHORT).show());
            }
        } else {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
        }
    }
}
