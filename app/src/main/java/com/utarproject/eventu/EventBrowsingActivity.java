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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventBrowsingActivity extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_browsing);

        db = FirebaseFirestore.getInstance();
        eventsRef = db.collection("events");

        ImageButton filterButton = findViewById(R.id.filterButton);
        EditText searchInput = findViewById(R.id.searchInput);

        filterButton.setOnClickListener(v -> showFilterDialog(searchInput));

        RecyclerView recyclerView = findViewById(R.id.eventRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        eventAdapter = new EventAdapter(new ArrayList<>(), event -> {
            eventsRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    String documentId = document.getId();
                    Intent intent = new Intent(this, EventDetailActivity.class);
                    intent.putExtra("EVENT_ID", documentId);
                    startActivity(intent);
                }
            });
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
                applyFilter(currentEntryFee, currentCampus, currentUssdc, currentDateFrom, currentDateTo, currentSearchText);
            }
        });

        applyFilter(null, null, null, "", "", "");
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

            applyFilter(entryFee, campus, ussdc, dateFromStr, dateToStr, searchText);
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

            applyFilter(null, null, null, "", "", searchBar.getText().toString().trim());
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

    private void applyFilter(String entryFee, String campus, String ussdc, String dateFrom, String dateTo, String searchText) {
        currentEntryFee = entryFee;
        currentCampus = campus;
        currentUssdc = ussdc;
        currentDateFrom = dateFrom;
        currentDateTo = dateTo;
        currentSearchText = searchText;
        Query query = eventsRef;

        // entry fee filter
        if (currentEntryFee != null && !currentEntryFee.isEmpty()) {
            query = query.whereEqualTo("eventFees", currentEntryFee);
        }

        // campus filter
        if (currentCampus != null && !currentCampus.isEmpty()) {
            query = query.whereEqualTo("eventLocation", currentCampus);
        }

        // ussdc category filter
        if (currentUssdc != null && !currentUssdc.isEmpty()) {
            query = query.whereEqualTo("eventUssdcCat", currentUssdc);
        }

        // date filters (from and to)
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

        // search text filter
        if (currentSearchText != null && !currentSearchText.isEmpty()) {
            query = query.whereGreaterThanOrEqualTo("eventName", currentSearchText)
                    .whereLessThanOrEqualTo("eventName", currentSearchText + "\uf8ff"); // This allows for search text to be case insensitive
        }

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<Event> filteredEvents = new ArrayList<>();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Event event = doc.toObject(Event.class);
                filteredEvents.add(event);
            }
            updateEventList(filteredEvents);
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to fetch events. Try again later.", Toast.LENGTH_SHORT).show();
        });
    }


    private Date parseFirestoreDate(String dateStr) {
        try {
            return new java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(dateStr);
        } catch (Exception e) {
            return null;
        }
    }

    private void updateEventList(List<Event> events) {
        eventAdapter.setEvents(events);
        TextView noEventsText = findViewById(R.id.noEventsText);
        if (events.isEmpty()) {
            noEventsText.setVisibility(View.VISIBLE);
        } else {
            noEventsText.setVisibility(View.GONE);
        }
    }
}
