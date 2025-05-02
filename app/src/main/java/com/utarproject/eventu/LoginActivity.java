package com.utarproject.eventu;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.FirebaseFirestore;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class LoginActivity extends AppCompatActivity {
    private View titleContainer;
    private TextView tvEventHive, tvSubtitle, tvTerms;
    private Button btnUtarSignIn;
    private static final long ANIMATION_DURATION = 1000;
    private static final long ANIMATION_DELAY = 500;

    // Login related fields
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private static final int NOTIFICATION_PERMISSION_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    NOTIFICATION_PERMISSION_CODE);
            }
        }

        // Initialize views
        titleContainer = findViewById(R.id.titleContainer);
        tvEventHive = findViewById(R.id.tvEventHive);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        btnUtarSignIn = findViewById(R.id.btnUtarSignIn);
        tvTerms = findViewById(R.id.tvTerms);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .setHostedDomain("1utar.my") // Restrict to UTAR email domain
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Start animations immediately after view initialization
        titleContainer.post(this::startAnimationSequence);
    }

    private void startAnimationSequence() {
        // EventHive title container animation
        ObjectAnimator titleFadeIn = ObjectAnimator.ofFloat(titleContainer, "alpha", 0f, 1f);
        titleFadeIn.setDuration(ANIMATION_DURATION);

        // Subtitle animation
        ObjectAnimator subtitleFadeIn = ObjectAnimator.ofFloat(tvSubtitle, "alpha", 0f, 1f);
        subtitleFadeIn.setDuration(ANIMATION_DURATION);

        // Sign In button animation
        ObjectAnimator buttonFadeIn = ObjectAnimator.ofFloat(btnUtarSignIn, "alpha", 0f, 1f);
        buttonFadeIn.setDuration(ANIMATION_DURATION);

        // Terms text animation
        ObjectAnimator termsFadeIn = ObjectAnimator.ofFloat(tvTerms, "alpha", 0f, 1f);
        termsFadeIn.setDuration(ANIMATION_DURATION);

        // Create animation sequence
        AnimatorSet animatorSet = new AnimatorSet();
        
        // Play animations in sequence with shorter delays
        animatorSet.playSequentially(
            titleFadeIn,
            subtitleFadeIn,
            buttonFadeIn,
            termsFadeIn
        );

        // Start the animation sequence immediately
        animatorSet.start();
    }

    public void signIn(View view) {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                String email = account.getEmail();
                if (email != null && email.endsWith("1utar.my")) {
                    firebaseAuthWithGoogle(account.getIdToken());
                } else {
                    Toast.makeText(this, "Please use your UTAR email", Toast.LENGTH_SHORT).show();
                    mGoogleSignInClient.signOut();
                }
            } catch (ApiException e) {
                Toast.makeText(this, "Sign In Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, check if user exists in Firestore
                        checkUserInFirestore();
                    } else {
                        Toast.makeText(LoginActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUserInFirestore() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(user.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // User exists, go to main screen
                            startActivity(new Intent(LoginActivity.this, displayEventActivity.class));
                        } else {
                            // New user, go to setup
                            startActivity(new Intent(LoginActivity.this, SetupActivity.class));
                        }
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(LoginActivity.this, "Error checking user data", Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, notifications will work
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {
                // Permission denied, notifications won't work
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
} 