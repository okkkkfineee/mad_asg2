package com.utarproject.eventu;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.FieldPath;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventBrowsingActivity extends BaseActivity {

    private FirebaseFirestore db;
    private CollectionReference eventsRef;
    private String currentEntryFee = null;
    private String currentCampus = null;
    private String currentUssdc = null;
    private String currentDateFrom = "";
    private String currentDateTo = "";
    private String currentSearchText = "";
    private EventAdapter eventAdapter;
    private Button selectedEntryFeeButton = null;
    private Button selectedCampusButton = null;
    private static final int PAGE_SIZE = 5;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private DocumentSnapshot lastVisible = null;
    private String highlightEventId = null;
    private boolean shouldHighlightEvent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_browsing);

        // Get highlight event info from intent
        highlightEventId = getIntent().getStringExtra("EVENT_ID");
        shouldHighlightEvent = getIntent().getBooleanExtra("HIGHLIGHT_EVENT", false);

        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("events");

        setupBottomNavigation(R.id.navigation_home);

        ImageButton filterButton = findViewById(R.id.filterButton);
        EditText searchInput = findViewById(R.id.searchInput);

        filterButton.setOnClickListener(v -> showFilterDialog(searchInput));

        RecyclerView recyclerView = findViewById(R.id.eventRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@androidx.annotation.NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && !isLoading && !isLastPage) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0
                            && totalItemCount >= PAGE_SIZE) {
                        loadMoreEvents();
                    }
                }
            }
        });

        eventAdapter = new EventAdapter(new ArrayList<>(), event -> {
            String documentId = event.getDocumentId();
            Intent intent = new Intent(this, EventDetailActivity.class);
            intent.putExtra("EVENT_ID", documentId);
            startActivity(intent);
        });
        recyclerView.setAdapter(eventAdapter);

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                currentSearchText = s.toString().trim();
                applyFilter(currentEntryFee, currentCampus, currentUssdc, currentDateFrom, currentDateTo, currentSearchText, true);
            }
        });

        loadEvents();
    }

    private void loadEvents() {
        if (shouldHighlightEvent && highlightEventId != null) {
            // First load the highlighted event
            eventsRef.document(highlightEventId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Event event = documentSnapshot.toObject(Event.class);
                            if (event != null) {
                                event.setDocumentId(documentSnapshot.getId());
                                event.setHighlighted(true);
                                eventAdapter.clearEvents();
                                eventAdapter.addEvent(event);
                                
                                // Then load other events
                                loadOtherEvents(event);
                            }
                        } else {
                            // If highlighted event doesn't exist, just load all events
                            applyFilter(null, null, null, "", "", "", true);
                        }
                    })
                    .addOnFailureListener(e -> {
                        // If failed to load highlight event, just load all events
                        applyFilter(null, null, null, "", "", "", true);
                    });
        } else {
            applyFilter(null, null, null, "", "", "", true);
        }
    }

    private void loadOtherEvents(Event highlightedEvent) {
        Query query = eventsRef
                .whereNotEqualTo(FieldPath.documentId(), highlightedEvent.getDocumentId())
                .limit(PAGE_SIZE);

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Event> newEvents = new ArrayList<>();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Event event = doc.toObject(Event.class);
                event.setDocumentId(doc.getId());
                event.setHighlighted(false);
                newEvents.add(event);
            }

            if (!newEvents.isEmpty()) {
                lastVisible = queryDocumentSnapshots.getDocuments()
                        .get(queryDocumentSnapshots.size() - 1);
            }

            if (newEvents.size() < PAGE_SIZE) {
                isLastPage = true;
            }

            eventAdapter.addEvents(newEvents);
            toggleNoEventsText();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to fetch events. Try again later.", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadMoreEvents() {
        if (isLoading || isLastPage) return;
        isLoading = true;

        Query query = eventsRef.limit(PAGE_SIZE);
        if (lastVisible != null) {
            query = query.startAfter(lastVisible);
        }

        if (highlightEventId != null) {
            query = query.whereNotEqualTo(FieldPath.documentId(), highlightEventId);
        }

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            isLoading = false;
            List<Event> newEvents = new ArrayList<>();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Event event = doc.toObject(Event.class);
                event.setDocumentId(doc.getId());
                event.setHighlighted(false);
                newEvents.add(event);
            }

            if (!newEvents.isEmpty()) {
                lastVisible = queryDocumentSnapshots.getDocuments()
                        .get(queryDocumentSnapshots.size() - 1);
            }

            if (newEvents.size() < PAGE_SIZE) {
                isLastPage = true;
            }

            eventAdapter.addEvents(newEvents);
            toggleNoEventsText();
        }).addOnFailureListener(e -> {
            isLoading = false;
            Toast.makeText(this, "Failed to fetch more events.", Toast.LENGTH_SHORT).show();
        });
    }

    private void showFilterDialog(EditText searchBar) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.filter_modal, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Entry Fee buttons
        Button btnEntryFree = dialogView.findViewById(R.id.btnEntryFree);
        if ("Free".equals(currentEntryFee)) {
            btnEntryFree.setSelected(true);
            btnEntryFree.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.teal_700));
            btnEntryFree.setTextColor(Color.WHITE);
        }
        btnEntryFree.setOnClickListener(v -> {
            selectEntryFeeButton(btnEntryFree);
            currentEntryFee = (selectedEntryFeeButton == btnEntryFree) ? "Free" : null;
        });

        // Campus buttons
        Button btnCampusKampar = dialogView.findViewById(R.id.btnCampusKampar);
        Button btnCampusSgLong = dialogView.findViewById(R.id.btnCampusSgLong);
        if ("Kampar Campus".equals(currentCampus)) {
            selectCampusButton(btnCampusKampar, btnCampusSgLong);
        } else if ("Sg Long Campus".equals(currentCampus)) {
            selectCampusButton(btnCampusSgLong, btnCampusKampar);
        }
        btnCampusKampar.setOnClickListener(v -> {
            selectCampusButton(btnCampusKampar, btnCampusSgLong);
            currentCampus = (selectedCampusButton == btnCampusKampar) ? "Kampar Campus" : null;
        });

        btnCampusSgLong.setOnClickListener(v -> {
            selectCampusButton(btnCampusSgLong, btnCampusKampar);
            currentCampus = (selectedCampusButton == btnCampusSgLong) ? "Sg Long Campus" : null;
        });

        // USSDC Category spinner
        Spinner ussdcSpinner = dialogView.findViewById(R.id.ussdcSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.ussdc_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ussdcSpinner.setAdapter(adapter);
        if (currentUssdc != null) {
            int position = adapter.getPosition(currentUssdc);
            ussdcSpinner.setSelection(position);
        }

        // Date fields
        EditText dateFrom = dialogView.findViewById(R.id.dateFrom);
        EditText dateTo = dialogView.findViewById(R.id.dateTo);
        if (!currentDateFrom.isEmpty()) {
            dateFrom.setText(currentDateFrom);
        }
        if (!currentDateTo.isEmpty()) {
            dateTo.setText(currentDateTo);
        }
        dateFrom.setOnClickListener(view -> showDatePickerDialog(dateFrom));
        dateTo.setOnClickListener(view -> showDatePickerDialog(dateTo));

        // Apply button
        Button applyButton = dialogView.findViewById(R.id.applyButton);
        applyButton.setOnClickListener(view -> {
            String entryFee = (selectedEntryFeeButton == btnEntryFree) ? "Free" : null;
            String campus = (selectedCampusButton == btnCampusKampar) ? "Kampar Campus" :
                    (selectedCampusButton == btnCampusSgLong) ? "Sg Long Campus" : null;
            Object selectedItem = ussdcSpinner.getSelectedItem();
            String ussdc = null;
            if (selectedItem != null && !selectedItem.toString().equals("Choose a Category")) {
                ussdc = selectedItem.toString();
            }
            String dateFromStr = dateFrom.getText().toString().trim();
            String dateToStr = dateTo.getText().toString().trim();
            String searchText = searchBar.getText().toString().trim();

            applyFilter(entryFee, campus, ussdc, dateFromStr, dateToStr, searchText, true);
            dialog.dismiss();
        });

        // Clear button
        Button clearButton = dialogView.findViewById(R.id.clearButton);
        clearButton.setOnClickListener(view -> {
            btnEntryFree.setSelected(false);
            btnCampusKampar.setSelected(false);
            btnCampusSgLong.setSelected(false);
            ussdcSpinner.setSelection(0);
            dateFrom.setText("");
            dateTo.setText("");

            selectedEntryFeeButton = null;
            selectedCampusButton = null;
            currentEntryFee = null;
            currentCampus = null;
            currentUssdc = null;
            currentDateFrom = "";
            currentDateTo = "";
            currentSearchText = "";

            applyFilter(null, null, null, "", "", searchBar.getText().toString().trim(), true);
            dialog.dismiss();
        });

        // Close Button
        ImageButton closeBtn = dialogView.findViewById(R.id.closeButton);
        closeBtn.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
    }


    private void selectEntryFeeButton(Button clicked) {
        Context context = this;
        if (clicked.equals(selectedEntryFeeButton)) {
            clicked.setSelected(false);
            clicked.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.grey));
            clicked.setTextColor(Color.BLACK);
            selectedEntryFeeButton = null;
        } else {
            clicked.setSelected(true);
            clicked.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.teal_700));
            clicked.setTextColor(Color.WHITE);
            if (selectedEntryFeeButton != null) {
                selectedEntryFeeButton.setSelected(false);
                selectedEntryFeeButton.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.grey));
                selectedEntryFeeButton.setTextColor(Color.BLACK);
            }
            selectedEntryFeeButton = clicked;
        }
    }

    private void selectCampusButton(Button clicked, Button... others) {
        Context context = this;
        if (clicked.equals(selectedCampusButton)) {
            clicked.setSelected(false);
            clicked.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.grey));
            clicked.setTextColor(Color.BLACK);
            selectedCampusButton = null;
        } else {
            clicked.setSelected(true);
            clicked.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.teal_700));
            clicked.setTextColor(Color.WHITE);
            for (Button b : others) {
                b.setSelected(false);
                b.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.grey));
                b.setTextColor(Color.BLACK);
            }
            selectedCampusButton = clicked;
        }
    }

    private void showDatePickerDialog(EditText targetEditText) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year1, monthOfYear + 1, dayOfMonth);
                    targetEditText.setText(selectedDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void applyFilter(String entryFee, String campus, String ussdc, String dateFrom, String dateTo, String searchText, boolean clearResults) {
        currentEntryFee = entryFee;
        currentCampus = campus;
        currentUssdc = ussdc;
        currentDateFrom = dateFrom;
        currentDateTo = dateTo;
        currentSearchText = searchText;

        Query query = eventsRef;

        // Apply filters
        if (currentEntryFee != null && !currentEntryFee.isEmpty()) {
            query = query.whereEqualTo("eventFees", currentEntryFee);
        }

        if (currentCampus != null && !currentCampus.isEmpty()) {
            query = query.whereEqualTo("eventLocation", currentCampus);
        }

        if (currentUssdc != null && !currentUssdc.isEmpty()) {
            query = query.whereEqualTo("eventUssdcCat", currentUssdc);
        }

        if (currentDateFrom != null && !currentDateFrom.isEmpty()) {
            Date fromDateObj = parseFirestoreDate(currentDateFrom);
            if (fromDateObj != null) {
                query = query.whereGreaterThanOrEqualTo("eventDate", fromDateObj);
            }
        }

        if (currentDateTo != null && !currentDateTo.isEmpty()) {
            Date toDateObj = parseFirestoreDate(currentDateTo);
            if (toDateObj != null) {
                query = query.whereLessThanOrEqualTo("eventDate", toDateObj);
            }
        }

        if (currentSearchText != null && !currentSearchText.isEmpty()) {
            query = query.whereGreaterThanOrEqualTo("eventName", currentSearchText)
                    .whereLessThanOrEqualTo("eventName", currentSearchText + "\uf8ff");
        }

        // Pagination
        query = query.limit(PAGE_SIZE);
        if (!clearResults && lastVisible != null) {
            query = query.startAfter(lastVisible);
        }

        isLoading = true;
        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            isLoading = false;

            if (clearResults) {
                eventAdapter.clearEvents();
                lastVisible = null;
                isLastPage = false;
            }

            List<Event> newEvents = new ArrayList<>();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Event event = doc.toObject(Event.class);
                event.setDocumentId(doc.getId());
                newEvents.add(event);
            }

            if (!newEvents.isEmpty()) {
                lastVisible = queryDocumentSnapshots.getDocuments()
                        .get(queryDocumentSnapshots.size() - 1);
            }

            if (newEvents.size() < PAGE_SIZE) {
                isLastPage = true;
            }

            eventAdapter.addEvents(newEvents);
            toggleNoEventsText();
        }).addOnFailureListener(e -> {
            isLoading = false;
            Toast.makeText(this, "Failed to fetch events. Try again later.", Toast.LENGTH_SHORT).show();
        });
    }

    private Date parseFirestoreDate(String dateStr) {
        try {
            return new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr);
        } catch (Exception e) {
            return null;
        }
    }

    private void toggleNoEventsText() {
        TextView noEventsText = findViewById(R.id.noEventsText);
        if (eventAdapter.getItemCount() == 0) {
            noEventsText.setVisibility(View.VISIBLE);
        } else {
            noEventsText.setVisibility(View.GONE);
        }
    }
}
