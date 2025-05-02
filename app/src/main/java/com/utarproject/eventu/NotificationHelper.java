package com.utarproject.eventu;

import android.util.Log;
import android.widget.Toast;
import android.content.Context;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class NotificationHelper {
    private static final String TAG = "NotificationHelper";
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static void createEventNotification(String eventId, String eventName, String organizerName, Context context) {
        Log.d(TAG, "Creating notifications for event: " + eventName + " by " + organizerName);
        
        // Get all users
        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int userCount = queryDocumentSnapshots.size();
                    Log.d(TAG, "Found " + userCount + " users to notify");
                    
                    for (com.google.firebase.firestore.DocumentSnapshot document : queryDocumentSnapshots) {
                        String userId = document.getId();
                        String userName = document.getString("name");
                        Log.d(TAG, "Creating notification for user: " + userName + " (ID: " + userId + ")");
                        
                        // Create notification for each user
                        NotificationActivity.NotificationItem notification = new NotificationActivity.NotificationItem(
                                userId,
                                "New Event: " + eventName,
                                "A new event has been created by " + organizerName,
                                eventId
                        );
                        
                        // Add to Firestore
                        db.collection("notifications")
                                .add(notification)
                                .addOnSuccessListener(documentReference -> {
                                    Log.d(TAG, "Notification created successfully for user: " + userName);
                                    if (context != null) {
                                        Toast.makeText(context, 
                                            "Notification created for event: " + eventName, 
                                            Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error creating notification for user: " + userName, e);
                                    if (context != null) {
                                        Toast.makeText(context, 
                                            "Failed to create notification: " + e.getMessage(), 
                                            Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting users", e);
                    if (context != null) {
                        Toast.makeText(context, 
                            "Failed to create notifications: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public static void deleteEventNotifications(String eventId) {
        Log.d(TAG, "Deleting notifications for event: " + eventId);
        
        db.collection("notifications")
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size();
                    Log.d(TAG, "Found " + count + " notifications to delete");
                    
                    for (com.google.firebase.firestore.DocumentSnapshot document : queryDocumentSnapshots) {
                        document.getReference().delete()
                                .addOnSuccessListener(aVoid -> 
                                    Log.d(TAG, "Successfully deleted notification: " + document.getId()))
                                .addOnFailureListener(e -> 
                                    Log.e(TAG, "Error deleting notification: " + document.getId(), e));
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error querying notifications", e));
    }

    // Helper method to check notifications for testing
    public static void checkNotifications(String userId, Context context) {
        Log.d(TAG, "Checking notifications for user: " + userId);
        
        db.collection("notifications")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size();
                    String message = "Found " + count + " notifications";
                    Log.d(TAG, message);
                    if (context != null) {
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking notifications", e);
                    if (context != null) {
                        Toast.makeText(context, 
                            "Error checking notifications: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    }
                });
    }
} 