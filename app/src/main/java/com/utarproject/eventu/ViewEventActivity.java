package com.utarproject.eventu;

import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import java.util.*;

public class ViewEventActivity extends AppCompatActivity {

    private TextView name, date, time, regTime, fees, desc, loc, ussdc;
    private Button editBtn, deleteBtn;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private String currentUserId;
    private String currentUserRole;
    private String eventId;
    private DocumentSnapshot eventDoc;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_event);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();

        // Initialize views
        name = findViewById(R.id.view_event_name);
        date = findViewById(R.id.view_event_date);
        time = findViewById(R.id.view_event_time);
        regTime = findViewById(R.id.view_event_reg);
        fees = findViewById(R.id.view_event_fees);
        desc = findViewById(R.id.view_event_desc);
        loc = findViewById(R.id.view_event_loc);
        ussdc = findViewById(R.id.view_event_ussdc);
        editBtn = findViewById(R.id.view_edit_btn);
        deleteBtn = findViewById(R.id.view_delete_btn);

        // Setup bottom navigation
        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.navigation_home);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                return true;
            } else if (itemId == R.id.navigation_interest) {
                startActivity(new Intent(this, InterestActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.navigation_notification) {
                startActivity(new Intent(this, NotificationActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.navigation_profile) {
                startActivity(new Intent(this, ProfileDisplayActivity.class));
                finish();
                return true;
            }
            return false;
        });

        // Get event ID from intent if it exists
        eventId = getIntent().getStringExtra("EVENT_ID");

        if (firebaseUser != null) {
            currentUserId = firebaseUser.getUid();
            if (eventId != null) {
                // If we have an event ID, load that specific event
                loadCurrentUserInfo();
            } else {
                // If no event ID, show the main event list
                showMainEventList();
            }
        }
    }

    private void showMainEventList() {
        // TODO: Implement the main event list view
        // For now, just show a welcome message
        name.setText("Welcome to EventHive");
        date.setText("Browse upcoming events");
        time.setVisibility(View.GONE);
        regTime.setVisibility(View.GONE);
        fees.setVisibility(View.GONE);
        desc.setVisibility(View.GONE);
        loc.setVisibility(View.GONE);
        ussdc.setVisibility(View.GONE);
        editBtn.setVisibility(View.GONE);
        deleteBtn.setVisibility(View.GONE);
    }

    private void loadCurrentUserInfo() {
        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        currentUserRole = snapshot.getString("role");
                        loadEventDetails();
                    }
                });
    }

    private void loadEventDetails() {
        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        eventDoc = snapshot;
                        populateUI(snapshot);
                    }
                });
    }

    private void populateUI(DocumentSnapshot doc) {
        name.setText(doc.getString("eventName"));
        date.setText("Event Date: " + doc.getString("eventDate"));
        time.setText("Event Time: " + doc.getString("eventTimeStart") + " - " + doc.getString("eventTimeEnd"));
        regTime.setText("Registration Link: " + doc.getString("registrationTime"));
        fees.setText("Fees: " + doc.getString("eventFees"));
        desc.setText(doc.getString("eventDescription"));
        loc.setText("Event Location: " + doc.getString("eventLocation"));
        ussdc.setText("USSDC Category: " + doc.getString("eventUssdcCat"));

        String organizerId = doc.getString("organizerId");
        List<String> attendees = (List<String>) doc.get("attendees");

        boolean isOrganizer = currentUserId.equals(organizerId);
        boolean isAlreadyJoined = attendees != null && attendees.contains(currentUserId);

        if (isOrganizer) {
            editBtn.setVisibility(View.VISIBLE);
            deleteBtn.setVisibility(View.VISIBLE);
        }

        editBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, editEventActivity.class);
            intent.putExtra("EVENT_ID", eventId);
            startActivity(intent);
        });

        deleteBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete event?")
                    .setMessage("Are you sure you want to delete this event?")
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            db.collection("events").document(eventId)
                                    .delete()
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(ViewEventActivity.this, "Event deleted", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(ViewEventActivity.this, ProfileDisplayActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(ViewEventActivity.this, "Failed to delete event", Toast.LENGTH_SHORT).show());
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }
}
