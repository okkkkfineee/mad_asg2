package com.utarproject.eventu;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private List<Event> events;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Event event);
    }

    public EventAdapter(List<Event> events, OnItemClickListener listener) {
        this.events = events;
        this.listener = listener;
    }

    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_browsing_card, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(EventViewHolder holder, int position) {
        if (holder != null && position < events.size()) {
            Event event = events.get(position);
            if (event != null) {
                holder.bind(event);
            }
        }
    }

    @Override
    public int getItemCount() {
        return events != null ? events.size() : 0;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
        notifyDataSetChanged();
    }

    public class EventViewHolder extends RecyclerView.ViewHolder {
        private TextView eventTitle;
        private TextView eventDate;
        private CardView cardView;

        public EventViewHolder(View itemView) {
            super(itemView);
            eventTitle = itemView.findViewById(R.id.eventTitle);
            eventDate = itemView.findViewById(R.id.eventDate);
            cardView = itemView.findViewById(R.id.eventCard);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION && position < events.size()) {
                    listener.onItemClick(events.get(position));
                }
            });
        }

        public void bind(Event event) {
            if (event != null) {
                if (eventTitle != null) {
                    eventTitle.setText(event.getEventName());
                }
                if (eventDate != null) {
                    eventDate.setText(event.getEventDate());
                }
                if (cardView != null) {
                    cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), 
                        event.isHighlighted() ? R.color.highlight_color : android.R.color.white));
                    
                    // Scroll to this item if it's highlighted
                    if (event.isHighlighted() && getAdapterPosition() != RecyclerView.NO_POSITION) {
                        RecyclerView recyclerView = (RecyclerView) itemView.getParent();
                        if (recyclerView != null) {
                            recyclerView.post(() -> recyclerView.smoothScrollToPosition(getAdapterPosition()));
                        }
                    }
                }
            }
        }
    }

    public void addEvent(Event event) {
        if (event != null) {
            events.add(event);
            notifyItemInserted(events.size() - 1);
        }
    }

    public void addEvents(List<Event> newEvents) {
        if (newEvents != null) {
            int startPosition = events.size();
            events.addAll(newEvents);
            notifyItemRangeInserted(startPosition, newEvents.size());
        }
    }

    public void clearEvents() {
        int size = events.size();
        events.clear();
        notifyItemRangeRemoved(0, size);
    }
}


