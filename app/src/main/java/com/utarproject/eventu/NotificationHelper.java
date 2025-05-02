package com.utarproject.eventu;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;
import androidx.core.app.NotificationCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class NotificationHelper {
    private static final String TAG = "NotificationHelper";
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String CHANNEL_ID = "eventhive_channel";
    private static final String CHANNEL_NAME = "EventHive Notifications";
    private static final String CHANNEL_DESC = "Notifications for new events and updates";
    private static int notificationId = 0;

    private static void createNotificationChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(CHANNEL_DESC);

            // Register the channel with the system
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private static void showSystemNotification(Context context, String title, String message, String eventId) {
        createNotificationChannel(context);

        // Create an intent to open the event in displayEventActivity
        Intent intent = new Intent(context, displayEventActivity.class);
        intent.putExtra("EVENT_ID", eventId);
        intent.putExtra("HIGHLIGHT_EVENT", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Create the pending intent
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent);

        // Show the notification
        NotificationManager notificationManager = 
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId++, builder.build());
    }

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
                                    
                                    // Show system notification for the current user only
                                    if (context != null && userId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                        showSystemNotification(
                                            context,
                                            notification.getTitle(),
                                            notification.getMessage(),
                                            eventId
                                        );
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