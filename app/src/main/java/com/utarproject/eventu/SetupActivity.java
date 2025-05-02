package com.utarproject.eventu;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.graphics.Color;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class SetupActivity extends AppCompatActivity {

    private EditText studentIdEditText;
    private Spinner campusSpinner, roleSpinner;
    private Button saveButton;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        studentIdEditText = findViewById(R.id.studentIdEditText);
        campusSpinner = findViewById(R.id.campusSpinner);
        roleSpinner = findViewById(R.id.roleSpinner);
        saveButton = findViewById(R.id.saveButton);

        // Set up campus options in the spinner with custom style
        ArrayAdapter<CharSequence> campusAdapter = ArrayAdapter.createFromResource(this,
                R.array.campus_array, R.layout.spinner_item);
        campusAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        campusSpinner.setAdapter(campusAdapter);

        // Set up role options in the spinner with custom style
        ArrayAdapter<CharSequence> roleAdapter = ArrayAdapter.createFromResource(this,
                R.array.role_array, R.layout.spinner_item);
        roleAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        roleSpinner.setAdapter(roleAdapter);

        saveButton.setOnClickListener(v -> {
            String stuId = studentIdEditText.getText().toString().trim();
            String campus = campusSpinner.getSelectedItem().toString();
            String role = roleSpinner.getSelectedItem().toString();

            if (!stuId.isEmpty() && !campus.equals(getString(R.string.select_campus)) &&
                    !role.equals(getString(R.string.select_role))) {
                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                if (firebaseUser != null) {
                    String uid = firebaseUser.getUid();
                    String name = firebaseUser.getDisplayName();

                    // Check if user document exists
                    db.collection("users").document(uid).get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    // Document exists, update user info
                                    db.collection("users").document(uid)
                                            .update("stuId", stuId, "campus", campus, "role", role)
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(SetupActivity.this, "Welcome, " + name, Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(SetupActivity.this, EventBrowsingActivity.class));
                                                finish();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(SetupActivity.this, "Error updating user info", Toast.LENGTH_SHORT).show();
                                            });
                                } else {
                                    // Document doesn't exist, set user data
                                    Map<String, Object> userData = new HashMap<>();
                                    userData.put("stuId", stuId);
                                    userData.put("campus", campus);
                                    userData.put("role", role);

                                    db.collection("users").document(uid)
                                            .set(userData, SetOptions.merge())
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(SetupActivity.this, "Welcome, " + name, Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(SetupActivity.this, EventBrowsingActivity.class));
                                                finish();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(SetupActivity.this, "Error setting user info", Toast.LENGTH_SHORT).show();
                                            });
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(SetupActivity.this, "Error checking user document", Toast.LENGTH_SHORT).show();
                            });
                }
            } else {
                Toast.makeText(SetupActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
