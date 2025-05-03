package com.utarproject.eventu;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    private final List<NotificationActivity.NotificationItem> notifications;
    private final SimpleDateFormat dateFormat;

    public NotificationAdapter(List<NotificationActivity.NotificationItem> notifications) {
        this.notifications = notifications;
        this.dateFormat = new SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault());
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationActivity.NotificationItem notification = notifications.get(position);
        
        holder.titleText.setText(notification.getTitle());
        holder.messageText.setText(notification.getMessage());
        holder.timeText.setText(dateFormat.format(notification.getTimestamp()));

        // Set click listener to open the event in EventBrowsingActivity
        holder.itemView.setOnClickListener(v -> {
            if (notification.getEventId() != null) {
                Intent intent = new Intent(v.getContext(), EventBrowsingActivity.class);
                intent.putExtra("EVENT_ID", notification.getEventId());
                intent.putExtra("HIGHLIGHT_EVENT", true);
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        TextView messageText;
        TextView timeText;

        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.notificationTitle);
            messageText = itemView.findViewById(R.id.notificationMessage);
            timeText = itemView.findViewById(R.id.notificationTime);
        }
    }
} 