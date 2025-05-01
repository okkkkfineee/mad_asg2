package com.utarproject.eventu;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InterestActivity extends BaseActivity {
    private static final String TAG = "InterestActivity";
    private LinearLayout interestEventsContainer;
    private TextView emptyStateText;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private String currentUserId;
    private List<String> userInterests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interest);

        interestEventsContainer = findViewById(R.id.interestEventsContainer);
        emptyStateText = findViewById(R.id.emptyStateText);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        userInterests = new ArrayList<>();

        // Add debug logging for authentication status
        if (firebaseUser != null) {
            Log.d(TAG, "User is signed in with email: " + firebaseUser.getEmail());
            Log.d(TAG, "User ID: " + firebaseUser.getUid());
        } else {
            Log.e(TAG, "No user is signed in!");
        }

        // Setup bottom navigation with interest selected
        setupBottomNavigation(R.id.navigation_interest);

        if (firebaseUser != null) {
            currentUserId = firebaseUser.getUid();
            Log.d(TAG, "Current user ID: " + currentUserId);
            loadUserInterests();
        } else {
            Log.e(TAG, "No user is signed in!");
            Toast.makeText(this, "Please sign in to view interests", Toast.LENGTH_LONG).show();
        }
    }

    private void loadUserInterests() {
        Log.d(TAG, "Loading user interests for user: " + currentUserId);
        
        DocumentReference userInterestsRef = db.collection("user_interests").document(currentUserId);
        userInterestsRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    Log.d(TAG, "Successfully accessed user_interests document");
                    if (documentSnapshot.exists()) {
                        Log.d(TAG, "Document exists");
                        List<String> interests = (List<String>) documentSnapshot.get("eventIds");
                        if (interests != null && !interests.isEmpty()) {
                            Log.d(TAG, "Found " + interests.size() + " interests");
                            userInterests = interests;
                            loadInterestedEvents();
                        } else {
                            Log.d(TAG, "No interests found in document");
                            showEmptyState();
                        }
                    } else {
                        Log.d(TAG, "No interests document exists for user");
                        // Create empty interests document for the user
                        Map<String, Object> data = new HashMap<>();
                        data.put("eventIds", new ArrayList<String>());
                        userInterestsRef.set(data)
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "Created empty interests document"))
                                .addOnFailureListener(e -> Log.e(TAG, "Error creating interests document", e));
                        showEmptyState();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading user interests", e);
                    Toast.makeText(this, "Failed to load interests: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    showEmptyState();
                });
    }

    private void loadInterestedEvents() {
        if (userInterests.isEmpty()) {
            Log.d(TAG, "No interests to load");
            showEmptyState();
            return;
        }

        Log.d(TAG, "Loading " + userInterests.size() + " interested events");
        hideEmptyState();
        interestEventsContainer.removeAllViews();

        // Create a query to get all interested events
        db.collection("events")
                .whereIn(FieldPath.documentId(), userInterests)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Successfully queried events");
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d(TAG, "No matching events found");
                        showEmptyState();
                        return;
                    }

                    Log.d(TAG, "Found " + queryDocumentSnapshots.size() + " events");
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        displayEvent(doc);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading events", e);
                    Toast.makeText(this, "Failed to load events: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    showEmptyState();
                });
    }

    private void showEmptyState() {
        interestEventsContainer.setVisibility(View.GONE);
        emptyStateText.setVisibility(View.VISIBLE);
    }

    private void hideEmptyState() {
        interestEventsContainer.setVisibility(View.VISIBLE);
        emptyStateText.setVisibility(View.GONE);
    }

    private void displayEvent(DocumentSnapshot eventDoc) {
        Log.d(TAG, "Displaying event: " + eventDoc.getId());
        View eventView = getLayoutInflater().inflate(R.layout.item_event_card, null);

        try {
            ((TextView) eventView.findViewById(R.id.eventName)).setText(eventDoc.getString("eventName"));
            ((TextView) eventView.findViewById(R.id.eventDate)).setText("Date: " + eventDoc.getString("eventDate"));
            ((TextView) eventView.findViewById(R.id.eventTime)).setText("Time: " + eventDoc.getString("eventTimeStart") + " - " + eventDoc.getString("eventTimeEnd"));
            ((TextView) eventView.findViewById(R.id.eventFees)).setText("Fees: " + eventDoc.getString("eventFees"));
            ((TextView) eventView.findViewById(R.id.eventUssdcCat)).setText("USSDC Category: " + eventDoc.getString("eventUssdcCat"));
            ((TextView) eventView.findViewById(R.id.eventLocation)).setText("Event Location: " + eventDoc.getString("eventLocation"));

            ImageButton btnInterest = eventView.findViewById(R.id.btnInterest);
            String eventId = eventDoc.getId();
            
            btnInterest.setImageResource(R.drawable.filled_love_icon);
            btnInterest.setOnClickListener(v -> removeFromInterests(eventId, eventView));

            eventView.setOnClickListener(v -> {
                Intent intent = new Intent(this, ViewEventActivity.class);
                intent.putExtra("EVENT_ID", eventId);
                startActivity(intent);
            });

            interestEventsContainer.addView(eventView);
            Log.d(TAG, "Successfully displayed event: " + eventId);
        } catch (Exception e) {
            Log.e(TAG, "Error displaying event: " + eventDoc.getId(), e);
        }
    }

    private void removeFromInterests(String eventId, View eventView) {
        Log.d(TAG, "Removing event from interests: " + eventId);
        userInterests.remove(eventId);

        Map<String, Object> data = new HashMap<>();
        data.put("eventIds", userInterests);

        db.collection("user_interests")
                .document(currentUserId)
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Successfully removed event from interests: " + eventId);
                    eventView.animate()
                            .alpha(0f)
                            .setDuration(300)
                            .withEndAction(() -> {
                                interestEventsContainer.removeView(eventView);
                                if (interestEventsContainer.getChildCount() == 0) {
                                    showEmptyState();
                                }
                            })
                            .start();
                    Toast.makeText(this, "Removed from interests", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error removing event from interests: " + eventId, e);
                    userInterests.add(eventId);
                    Toast.makeText(this, "Failed to update interests: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (firebaseUser != null) {
            Log.d(TAG, "Refreshing interests on resume");
            loadUserInterests();
        }
    }
} 