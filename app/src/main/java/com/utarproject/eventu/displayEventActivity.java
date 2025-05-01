package com.utarproject.eventu;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import java.util.List;

public class displayEventActivity extends BaseActivity {
    private LinearLayout eventsContainer;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private String currentUserId;
    private String currentUserRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_events);

        eventsContainer = findViewById(R.id.eventsContainer);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();

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
                        loadEvents();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load user info", Toast.LENGTH_SHORT).show());
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

        ((TextView) eventView.findViewById(R.id.eventName)).setText(eventDoc.getString("eventName"));
        ((TextView) eventView.findViewById(R.id.eventDate)).setText("Date: " + eventDoc.getString("eventDate"));
        ((TextView) eventView.findViewById(R.id.eventTime)).setText("Time: " + eventDoc.getString("eventTimeStart") + " - " + eventDoc.getString("eventTimeEnd"));
        ((TextView) eventView.findViewById(R.id.eventFees)).setText("Fees: " + eventDoc.getString("eventFees"));
        ((TextView) eventView.findViewById(R.id.eventUssdcCat)).setText("USSDC Category: " + eventDoc.getString("eventUssdcCat"));
        ((TextView) eventView.findViewById(R.id.eventLocation)).setText("Event Location: " + eventDoc.getString("eventLocation"));

        eventView.setOnClickListener(v -> {
            Intent intent = new Intent(this, ViewEventActivity.class);
            intent.putExtra("EVENT_ID", eventDoc.getId());
            startActivity(intent);
        });

        eventsContainer.addView(eventView);
    }
}
