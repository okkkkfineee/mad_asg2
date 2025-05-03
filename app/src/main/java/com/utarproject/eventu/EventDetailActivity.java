package com.utarproject.eventu;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;

import com.google.firebase.firestore.FirebaseFirestore;

public class EventDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_detail);

        // Initialize UI components
        TextView titleText = findViewById(R.id.titleText);
        TextView organizerText = findViewById(R.id.organizerText);
        TextView dateText = findViewById(R.id.dateText);
        TextView registrationTimeText = findViewById(R.id.registrationTimeText);
        TextView eventTimeText = findViewById(R.id.eventTimeText);
        TextView locationText = findViewById(R.id.locationText);
        TextView feesText = findViewById(R.id.feesText);
        TextView ussdcText = findViewById(R.id.ussdcText);
        TextView descriptionText = findViewById(R.id.descriptionText);
        Button registerButton = findViewById(R.id.registerButton);
        ImageButton backBtn = findViewById(R.id.backBtn);
        ImageButton shareBtn = findViewById(R.id.shareBtn);
        ImageButton interestBtn = findViewById(R.id.interestBtn);

        boolean[] isInterested = {false};
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String eventId = getIntent().getStringExtra("EVENT_ID");
        if (eventId == null) {
            return;
        }

        // Back button action
        backBtn.setOnClickListener(v -> {
            Intent intent = new Intent(EventDetailActivity.this, EventBrowsingActivity.class);
            startActivity(intent);
            finish();
        });

        // Interest button action
        db.collection("user_interests").document(userId).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                java.util.List<String> interests = (java.util.List<String>) snapshot.get("eventIds");
                if (interests != null && interests.contains(eventId)) {
                    isInterested[0] = true;
                    interestBtn.setImageResource(R.drawable.filled_love_icon);
                }
            }
        });

        // Interest button toggle logic
        interestBtn.setOnClickListener(v -> {
            if (isInterested[0]) {
                db.collection("user_interests").document(userId)
                        .update("eventIds", FieldValue.arrayRemove(eventId))
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Removed from Interests", Toast.LENGTH_SHORT).show();
                            interestBtn.setImageResource(R.drawable.love_icon);
                            isInterested[0] = false;
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Error removing interest", Toast.LENGTH_SHORT).show();
                        });
            } else {
                // Add interest
                db.collection("user_interests").document(userId)
                        .update("eventIds", FieldValue.arrayUnion(eventId))
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Added to Interests", Toast.LENGTH_SHORT).show();
                            interestBtn.setImageResource(R.drawable.filled_love_icon);
                            isInterested[0] = true;
                        })
                        .addOnFailureListener(e -> {
                            java.util.Map<String, Object> data = new java.util.HashMap<>();
                            data.put("eventIds", java.util.Arrays.asList(eventId));
                            db.collection("user_interests").document(userId).set(data)
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(this, "Added to Interests", Toast.LENGTH_SHORT).show();
                                        interestBtn.setImageResource(R.drawable.filled_love_icon);
                                        isInterested[0] = true;
                                    });
                        });
            }
        });

        db.collection("events").document(eventId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String eventName = getField(documentSnapshot.getString("eventName"));
                String organizerId = getField(documentSnapshot.getString("organizerId"));
                String registrationTime = getField(documentSnapshot.getString("registrationTime"));
                String eventDate = getField(documentSnapshot.getString("eventDate"));
                String eventTimeStart = getField(documentSnapshot.getString("eventTimeStart"));
                String eventTimeEnd = getField(documentSnapshot.getString("eventTimeEnd"));
                String eventLocation = getField(documentSnapshot.getString("eventLocation"));
                String eventFees = getField(documentSnapshot.getString("eventFees"));
                String eventUssdcCat = getField(documentSnapshot.getString("eventUssdcCat"));
                String eventDescription = getField(documentSnapshot.getString("eventDescription"));
                String registrationLink = getField(documentSnapshot.getString("registration_link"));

                getOrganizerNameAndSet(organizerId, organizerText);

                titleText.setText(eventName);
                dateText.setText("Date:\n" + eventDate);
                registrationTimeText.setText("Registration Time:\n" + registrationTime);
                eventTimeText.setText("Event Time:\n" + eventTimeStart + " - " + eventTimeEnd);
                locationText.setText("Campus:\n" + eventLocation);
                feesText.setText("Fees:\n" + eventFees);
                ussdcText.setText("USSDC Category:\n" + eventUssdcCat);
                descriptionText.setText(eventDescription);

                // Share button action
                shareBtn.setOnClickListener(v -> {
                    String shareText = "Check out this event!\n\n" +
                            "Event: " + eventName + "\n" +
                            "Date: " + eventDate + "\n" +
                            "Location: " + eventLocation + "\n\n" +
                            "More info: " + registrationLink;

                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

                    startActivity(Intent.createChooser(shareIntent, "Share via"));
                });

                registerButton.setOnClickListener(v -> {
                    if (!registrationLink.equals("No detail")) {
                        Intent register = new Intent(Intent.ACTION_VIEW, Uri.parse(registrationLink));
                        startActivity(register);
                    }
                });
            }
        });
    }

    private String getField(String value) {
        return (value != null && !value.trim().isEmpty()) ? value : "No detail";
    }

    private void getOrganizerNameAndSet(String organizerId, TextView organizerTextView) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(organizerId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String organizerName = documentSnapshot.getString("name");
                        if (organizerName != null && !organizerName.isEmpty()) {
                            organizerTextView.setText("Organized by " + organizerName);
                        } else {
                            organizerTextView.setText("Organized by Unknown");
                        }
                    } else {
                        organizerTextView.setText("Organized by Not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "error loading", e);
                    organizerTextView.setText("Organized by Error loading");
                });
    }
}
