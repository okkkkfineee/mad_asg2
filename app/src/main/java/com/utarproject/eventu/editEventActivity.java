package com.utarproject.eventu;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class editEventActivity extends AppCompatActivity {
    private TextInputEditText eventNameInput;
    private TextInputEditText eventDateInput;
    private TextInputEditText registrationLinkInput;
    private TextInputEditText eventStartTimeInput;
    private TextInputEditText eventEndTimeInput;
    private TextInputEditText eventFeesInput;
    private TextInputEditText eventDescriptionInput;
    private Spinner campusSpinner, ussdcSpinner;
    private Button saveButton;
    private FirebaseFirestore db;
    private String eventId;
    private Calendar calendar;
    private boolean hasChanges = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        calendar = Calendar.getInstance();

        // Get event ID from intent
        eventId = getIntent().getStringExtra("EVENT_ID");
        if (eventId == null) {
            Toast.makeText(this, "Error: Event ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        initializeViews();
        setupClickListeners();
        loadEventDetails();
    }

    private void initializeViews() {
        eventNameInput = findViewById(R.id.eventNameInput);
        eventDateInput = findViewById(R.id.eventDateInput);
        registrationLinkInput = findViewById(R.id.registrationLinkInput);
        eventStartTimeInput = findViewById(R.id.eventStartTimeInput);
        eventEndTimeInput = findViewById(R.id.eventEndTimeInput);
        eventFeesInput = findViewById(R.id.eventFeesInput);
        eventDescriptionInput = findViewById(R.id.eventDescriptionInput);
        saveButton = findViewById(R.id.saveButton);

        campusSpinner = findViewById(R.id.campusSpinner);
        ussdcSpinner = findViewById(R.id.ussdcSpinner);

        // Populate campus spinner with predefined campuses
        ArrayAdapter<CharSequence> campusAdapter = ArrayAdapter.createFromResource(this,
                R.array.campus_array, android.R.layout.simple_spinner_item);
        campusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        campusSpinner.setAdapter(campusAdapter);

        // Populate campus spinner with predefined campuses
        ArrayAdapter<CharSequence> ussdcAdapter = ArrayAdapter.createFromResource(this,
                R.array.ussdc_array, android.R.layout.simple_spinner_item);
        ussdcAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ussdcSpinner.setAdapter(ussdcAdapter);
    }

    private void setupClickListeners() {

        eventDateInput.setOnClickListener(v -> showDatePicker());
        eventStartTimeInput.setOnClickListener(v -> showTimePicker(eventStartTimeInput));
        eventEndTimeInput.setOnClickListener(v -> showTimePicker(eventEndTimeInput));

        saveButton.setOnClickListener(v -> saveEventChanges());
        
        // Add text change listeners to track changes
        addTextChangeListeners();
    }
    
    private void addTextChangeListeners() {
        View.OnFocusChangeListener focusListener = (v, hasFocus) -> {
            if (!hasFocus) {
                checkForChanges();
            }
        };

        eventNameInput.setOnFocusChangeListener(focusListener);
        eventDateInput.setOnFocusChangeListener(focusListener);
        registrationLinkInput.setOnFocusChangeListener(focusListener);
        eventStartTimeInput.setOnFocusChangeListener(focusListener);
        eventEndTimeInput.setOnFocusChangeListener(focusListener);
        eventFeesInput.setOnFocusChangeListener(focusListener);
        eventDescriptionInput.setOnFocusChangeListener(focusListener);
        campusSpinner.setOnFocusChangeListener(focusListener);
        ussdcSpinner.setOnFocusChangeListener(focusListener);
    }
    
    private void checkForChanges() {
        // This is a simple implementation - you might want to compare with original values
        // for a more accurate change detection
        hasChanges = true;
    }

    @Override
    public void onBackPressed() {
        if (hasChanges) {
            showDiscardChangesDialog();
        } else {
            super.onBackPressed();
        }
    }

    private void showDiscardChangesDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Discard Changes?")
            .setMessage("You have unsaved changes. Are you sure you want to discard them?")
            .setPositiveButton("Discard", (dialog, which) -> {
                hasChanges = false;
                finish();
            })
            .setNegativeButton("Keep Editing", null)
            .show();
    }

    private void loadEventDetails() {
        db.collection("events").document(eventId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    eventNameInput.setText(documentSnapshot.getString("eventName"));
                    eventDateInput.setText(documentSnapshot.getString("eventDate"));
                    registrationLinkInput.setText(documentSnapshot.getString("registrationTime"));
                    eventStartTimeInput.setText(documentSnapshot.getString("eventTimeStart"));
                    eventEndTimeInput.setText(documentSnapshot.getString("eventTimeEnd"));
                    eventFeesInput.setText(documentSnapshot.getString("eventFees"));
                    // Set event location (this will be a string)
                    ArrayAdapter<CharSequence> adapter1 = (ArrayAdapter<CharSequence>) campusSpinner.getAdapter();
                    int campusPosition = adapter1.getPosition(documentSnapshot.getString("eventLocation"));
                    campusSpinner.setSelection(campusPosition);
                    // Set USSDC Category (this will be a string)
                    ArrayAdapter<CharSequence> adapter2 = (ArrayAdapter<CharSequence>) ussdcSpinner.getAdapter();
                    int ussdcPosition = adapter2.getPosition(documentSnapshot.getString("eventUssdcCat"));
                    ussdcSpinner.setSelection(ussdcPosition);
                    eventFeesInput.setText(documentSnapshot.getString("eventFees"));
                    eventDescriptionInput.setText(documentSnapshot.getString("eventDescription"));
                    hasChanges = false; // Reset changes flag after loading
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error loading event details", Toast.LENGTH_SHORT).show();
                finish();
            });
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDateInView();
                hasChanges = true;
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showTimePicker(EditText targetEditText) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    updateTimeInView(targetEditText);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
        );
        timePickerDialog.show();
    }

    private void updateDateInView() {
        String date = String.format("%d/%d/%d",
            calendar.get(Calendar.DAY_OF_MONTH),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.YEAR));
        eventDateInput.setText(date);
    }

    private void updateTimeInView(EditText editText) {
        String format = "hh:mm a";
        SimpleDateFormat timeFormat = new SimpleDateFormat(format, Locale.getDefault());
        editText.setText(timeFormat.format(calendar.getTime()));
    }

    private void saveEventChanges() {
        String eventName = eventNameInput.getText().toString().trim();
        String eventDate = eventDateInput.getText().toString().trim();
        String registrationLink = registrationLinkInput.getText().toString().trim();
        String eventStartTime = eventStartTimeInput.getText().toString().trim();
        String eventEndTime = eventEndTimeInput.getText().toString().trim();
        String eventFees = eventFeesInput.getText().toString().trim();
        String eventDescription = eventDescriptionInput.getText().toString().trim();
        String eventLocation = campusSpinner.getSelectedItem().toString();
        String eventUssdcCat = ussdcSpinner.getSelectedItem().toString();

        // Validate inputs
        if (eventName.isEmpty() || eventDate.isEmpty() || registrationLink.isEmpty() || eventStartTime.isEmpty() || eventEndTime.isEmpty() || eventFees.isEmpty() || eventDescription.isEmpty() || eventLocation.isEmpty() || eventUssdcCat.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create event data map
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("eventName", eventName);
        eventData.put("eventDate", eventDate);
        eventData.put("registrationTime", registrationLink);
        eventData.put("eventTimeStart", eventStartTime);
        eventData.put("eventTimeEnd", eventEndTime);
        eventData.put("eventFees", eventFees);
        eventData.put("eventLocation", eventLocation);
        eventData.put("eventUssdcCat", eventUssdcCat);
        eventData.put("eventDescription", eventDescription);

        // Update event in Firestore
        db.collection("events").document(eventId)
            .update(eventData)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Event updated successfully", Toast.LENGTH_SHORT).show();
                hasChanges = false;
                Intent intent = new Intent(editEventActivity.this, ProfileDisplayActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent); // Close the activity and return to the Event Display Activity screen
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error updating event", Toast.LENGTH_SHORT).show();
            });
    }
} 