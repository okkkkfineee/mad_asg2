 package com.utarproject.eventu;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.*;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1001;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // very important!
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        findViewById(R.id.btnGoogleSignIn).setOnClickListener(v -> signIn());
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Google sign in failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            validateUTAREmail(firebaseUser);
                        }
                    } else {
                        Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void validateUTAREmail(FirebaseUser firebaseUser) {
        String email = firebaseUser.getEmail();
        if (email != null && (email.endsWith("@utar.edu.my") || email.endsWith("@1utar.my"))) {
            // Valid UTAR email
            saveUserToFirestore(firebaseUser);
        } else {
            // Not a UTAR email
            FirebaseAuth.getInstance().signOut();
            mGoogleSignInClient.signOut();
            Toast.makeText(this, "Only UTAR email accounts are allowed!", Toast.LENGTH_LONG).show();
        }
    }

    private void saveUserToFirestore(FirebaseUser firebaseUser) {
        String uid = firebaseUser.getUid();
        String email = firebaseUser.getEmail();
        String name = firebaseUser.getDisplayName();

        // Fetch user document from Firestore
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // User exists, check if stuId and campus are set
                        String stuId = documentSnapshot.getString("stuId");
                        String campus = documentSnapshot.getString("campus");

                        if (stuId != null && !stuId.isEmpty() && campus != null && !campus.isEmpty()) {
                            // User has completed setup
                            Toast.makeText(LoginActivity.this, "Welcome back, " + name, Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                            finish();
                        } else {
                            // User hasn't completed setup, redirect to SetupActivity
                            startActivity(new Intent(LoginActivity.this, SetupActivity.class));
                            finish();
                        }
                    } else {
                        // User doesn't exist, create a new record with empty stuId and campus
                        User user = new User("", name, email, "", "", "");
                        db.collection("users").document(uid).set(user)
                                .addOnSuccessListener(aVoid -> {
                                    // After saving user, move to setup screen
                                    startActivity(new Intent(LoginActivity.this, SetupActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(LoginActivity.this, "Error saving user: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(LoginActivity.this, "Error checking user: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }


}

