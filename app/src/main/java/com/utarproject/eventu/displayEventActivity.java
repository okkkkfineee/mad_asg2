package com.utarproject.eventu;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class displayEventActivity extends BaseActivity {
    private LinearLayout eventsContainer;
    private ScrollView scrollView;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private String currentUserId;
    private String currentUserRole;
    private List<String> userInterests;
    private String highlightEventId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_events);

        eventsContainer = findViewById(R.id.eventsContainer);
        scrollView = findViewById(R.id.scrollView);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        userInterests = new ArrayList<>();

        // Get highlight event ID if passed
        highlightEventId = getIntent().getStringExtra("EVENT_ID");
        
        // Setup bottom navigation with home selected
        setupBottomNavigation(R.id.navigation_home);

        if (firebaseUser != null) {
            currentUserId = firebaseUser.getUid();
            loadCurrentUserInfo();
        }
    }

    private void loadCurrentUserInfo() {
        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        currentUserRole = documentSnapshot.getString("role");
                        loadUserInterests();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load user info", Toast.LENGTH_SHORT).show());
    }

    private void loadUserInterests() {
        db.collection("user_interests")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> interests = (List<String>) documentSnapshot.get("eventIds");
                        if (interests != null) {
                            userInterests = interests;
                        }
                    }
                    loadEvents();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load interests", Toast.LENGTH_SHORT).show();
                    loadEvents();
                });
    }

    private void loadEvents() {
        db.collection("events").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    eventsContainer.removeAllViews();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        List<String> attendees = (List<String>) doc.get("attendees");
                        String organizerId = doc.getString("organizerId");

                        boolean isAttendee = attendees != null && attendees.contains(currentUserId);
                        boolean isOrganizer = organizerId != null && organizerId.equals(currentUserId);

                        if ((currentUserRole.equals("Attendee") && isAttendee) ||
                                (currentUserRole.equals("Organizer") && (isAttendee || isOrganizer))) {
                            displayEvent(doc, isOrganizer);
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load events", Toast.LENGTH_SHORT).show());
    }

    private void displayEvent(DocumentSnapshot eventDoc, boolean isOrganizerOfThisEvent) {
        View eventView = getLayoutInflater().inflate(R.layout.item_event_card, null);
        String eventId = eventDoc.getId();

        ((TextView) eventView.findViewById(R.id.eventName)).setText(eventDoc.getString("eventName"));
        ((TextView) eventView.findViewById(R.id.eventDate)).setText("Date: " + eventDoc.getString("eventDate"));
        ((TextView) eventView.findViewById(R.id.eventTime)).setText("Time: " + eventDoc.getString("eventTimeStart") + " - " + eventDoc.getString("eventTimeEnd"));
        ((TextView) eventView.findViewById(R.id.eventFees)).setText("Fees: " + eventDoc.getString("eventFees"));
        ((TextView) eventView.findViewById(R.id.eventUssdcCat)).setText("USSDC Category: " + eventDoc.getString("eventUssdcCat"));
        ((TextView) eventView.findViewById(R.id.eventLocation)).setText("Event Location: " + eventDoc.getString("eventLocation"));

        ImageButton btnInterest = eventView.findViewById(R.id.btnInterest);
        boolean isInterested = userInterests.contains(eventId);
        
        // Set the initial icon state
        btnInterest.setImageResource(isInterested ? R.drawable.filled_love_icon : R.drawable.love_icon);

        // Handle interest button click
        btnInterest.setOnClickListener(v -> toggleInterest(eventId, btnInterest));

        eventView.setOnClickListener(v -> {
            Intent intent = new Intent(this, ViewEventActivity.class);
            intent.putExtra("EVENT_ID", eventId);
            startActivity(intent);
        });

        // Highlight the event if it matches the highlightEventId
        if (eventId.equals(highlightEventId) && getIntent().getBooleanExtra("HIGHLIGHT_EVENT", false)) {
            CardView cardView = eventView.findViewById(R.id.eventCard);
            cardView.setCardBackgroundColor(getResources().getColor(R.color.highlight_color, getTheme()));
            // Scroll to the highlighted event
            eventsContainer.addView(eventView);
            eventView.post(() -> {
                int[] location = new int[2];
                eventView.getLocationInWindow(location);
                scrollView.smoothScrollTo(0, location[1] - 100); // Subtract 100 to show some content above
            });
            return;
        }

        eventsContainer.addView(eventView);
    }

    private void toggleInterest(String eventId, ImageButton btnInterest) {
        boolean isCurrentlyInterested = userInterests.contains(eventId);
        
        if (isCurrentlyInterested) {
            userInterests.remove(eventId);
        } else {
            userInterests.add(eventId);
        }

        // Update UI immediately
        btnInterest.setImageResource(userInterests.contains(eventId) ? 
            R.drawable.filled_love_icon : R.drawable.love_icon);

        // Update in Firebase
        Map<String, Object> data = new HashMap<>();
        data.put("eventIds", userInterests);

        db.collection("user_interests")
                .document(currentUserId)
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    String message = userInterests.contains(eventId) ? 
                        "Added to interests" : "Removed from interests";
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Revert the local change if Firebase update fails
                    if (isCurrentlyInterested) {
                        userInterests.add(eventId);
                    } else {
                        userInterests.remove(eventId);
                    }
                    btnInterest.setImageResource(isCurrentlyInterested ? 
                        R.drawable.filled_love_icon : R.drawable.love_icon);
                    Toast.makeText(this, "Failed to update interests", Toast.LENGTH_SHORT).show();
                });
    }
}
