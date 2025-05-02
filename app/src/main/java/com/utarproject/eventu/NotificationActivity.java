package com.utarproject.eventu;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class NotificationActivity extends BaseActivity {
    private static final String TAG = "NotificationActivity";
    private RecyclerView notificationsRecyclerView;
    private TextView emptyStateText;
    private NotificationAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private List<NotificationItem> notifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        
        // Initialize views
        notificationsRecyclerView = findViewById(R.id.notificationsRecyclerView);
        emptyStateText = findViewById(R.id.emptyStateText);
        
        // Setup RecyclerView
        notifications = new ArrayList<>();
        adapter = new NotificationAdapter(notifications);
        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        notificationsRecyclerView.setAdapter(adapter);

        // Setup bottom navigation
        setupBottomNavigation(R.id.navigation_notification);

        if (currentUser != null) {
            setupNotificationListener();
        } else {
            showEmptyState("Please sign in to view notifications");
        }
    }

    private void setupNotificationListener() {
        db.collection("notifications")
                .whereEqualTo("userId", currentUser.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Listen failed.", e);
                        return;
                    }

                    notifications.clear();
                    if (snapshots != null && !snapshots.isEmpty()) {
                        for (DocumentSnapshot document : snapshots) {
                            NotificationItem item = document.toObject(NotificationItem.class);
                            if (item != null) {
                                item.setId(document.getId());
                                notifications.add(item);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        showNotifications();
                    } else {
                        showEmptyState("No notifications yet");
                    }
                });
    }

    private void showEmptyState(String message) {
        notificationsRecyclerView.setVisibility(View.GONE);
        emptyStateText.setVisibility(View.VISIBLE);
        emptyStateText.setText(message);
    }

    private void showNotifications() {
        notificationsRecyclerView.setVisibility(View.VISIBLE);
        emptyStateText.setVisibility(View.GONE);
    }

    // Notification Item class
    public static class NotificationItem {
        private String id;
        private String userId;
        private String title;
        private String message;
        private Date timestamp;
        private String eventId;

        public NotificationItem() {} // Required for Firestore

        public NotificationItem(String userId, String title, String message, String eventId) {
            this.userId = userId;
            this.title = title;
            this.message = message;
            this.timestamp = new Date();
            this.eventId = eventId;
        }

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Date getTimestamp() { return timestamp; }
        public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
        public String getEventId() { return eventId; }
        public void setEventId(String eventId) { this.eventId = eventId; }
    }
} 