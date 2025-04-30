package com.utarproject.eventu;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

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

        // Set up campus options in the spinner (assuming a simple list of campuses)
        ArrayAdapter<CharSequence> campusAdapter = ArrayAdapter.createFromResource(this,
                R.array.campus_array, android.R.layout.simple_spinner_item);
        campusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        campusSpinner.setAdapter(campusAdapter);

        // Set up role options in the spinner (assuming a simple list of role)
        ArrayAdapter<CharSequence> roleAdapter = ArrayAdapter.createFromResource(this,
                R.array.role_array, android.R.layout.simple_spinner_item);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(roleAdapter);

        saveButton.setOnClickListener(v -> {
            String stuId = studentIdEditText.getText().toString().trim();
            String campus = campusSpinner.getSelectedItem().toString();
            String role = roleSpinner.getSelectedItem().toString();

            if (!stuId.isEmpty() && !campus.isEmpty() && !role.isEmpty()) {
                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                if (firebaseUser != null) {
                    String uid = firebaseUser.getUid();
                    String name = firebaseUser.getDisplayName();
                    db.collection("users").document(uid)
                            .update("stuId", stuId, "campus", campus, "role", role)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(SetupActivity.this, "Welcome back, " + name, Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(SetupActivity.this, HomeActivity.class));
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(SetupActivity.this, "Error updating user info", Toast.LENGTH_SHORT).show();
                            });
                }
            } else {
                Toast.makeText(SetupActivity.this, "Please enter valid student ID and campus", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
