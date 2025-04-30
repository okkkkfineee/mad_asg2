package com.utarproject.eventu;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class addEventActivity extends AppCompatActivity {

    private EditText eventNameInput, eventDateInput, registrationLinkInput, eventTimeStartInput, eventTimeEndInput, eventFeesInput, eventDescriptionInput;
    private Spinner campusSpinner, ussdcSpinner;
    private ImageButton calendarButton, calendarClock1Button, calendarClock2Button;
    private Button createEventButton;
    private Calendar calendar;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        // Initialize Firebase instances
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        calendar = Calendar.getInstance();

        // Initialize views
        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        eventNameInput = findViewById(R.id.input_eventname);
        eventDateInput = findViewById(R.id.input_eventdate);
        registrationLinkInput = findViewById(R.id.editTextRegistrationLink);
        eventTimeStartInput = findViewById(R.id.input_eventtimestart);
        eventTimeEndInput = findViewById(R.id.input_eventtimeend);
        eventFeesInput = findViewById(R.id.input_eventfees);
        eventDescriptionInput = findViewById(R.id.input_eventdescription);
        campusSpinner = findViewById(R.id.campusSpinner);
        ussdcSpinner = findViewById(R.id.ussdcSpinner);
        
        calendarButton = findViewById(R.id.imgbtn_calendar);
        calendarClock1Button = findViewById(R.id.imgbtn_calendarclock1);
        calendarClock2Button = findViewById(R.id.imgbtn_calendarclock2);
        
        createEventButton = findViewById(R.id.btn_createevent);

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
        // Date picker for event date
        calendarButton.setOnClickListener(v -> showDatePicker(eventDateInput));

        // Date and time picker for event start time
        calendarClock1Button.setOnClickListener(v -> {
            showTimePicker(eventTimeStartInput);
        });

        // Date and time picker for event end time
        calendarClock2Button.setOnClickListener(v -> {
            showTimePicker(eventTimeEndInput);
        });

        // Create event button
        createEventButton.setOnClickListener(v -> createEvent());
    }

    private void showDatePicker(EditText targetEditText) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDateInView(targetEditText);
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

    private void updateDateInView(EditText editText) {
        String format = "dd/MM/yyyy";
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
        editText.setText(dateFormat.format(calendar.getTime()));
    }

    private void updateTimeInView(EditText editText) {
        String format = "hh:mm a";
        SimpleDateFormat timeFormat = new SimpleDateFormat(format, Locale.getDefault());
        editText.setText(timeFormat.format(calendar.getTime()));
    }

    private void createEvent() {
        String eventName = eventNameInput.getText().toString().trim();
        String eventDate = eventDateInput.getText().toString().trim();
        String registrationLink = registrationLinkInput.getText().toString().trim();
        String eventTimeStart = eventTimeStartInput.getText().toString().trim();
        String eventTimeEnd = eventTimeEndInput.getText().toString().trim();
        String eventFees = eventFeesInput.getText().toString().trim();
        String eventDescription = eventDescriptionInput.getText().toString().trim();
        String eventLocation = campusSpinner.getSelectedItem().toString();
        String eventUssdcCat = ussdcSpinner.getSelectedItem().toString();

        // Validate inputs
        if (eventName.isEmpty() || eventDate.isEmpty() || registrationLink.isEmpty() ||
            eventTimeStart.isEmpty() || eventTimeEnd.isEmpty() || eventFees.isEmpty() || 
            eventDescription.isEmpty() || eventLocation.isEmpty() || eventUssdcCat.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get current user
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create event object
        Event event = new Event(
            currentUser.getUid(),
            eventName,
            eventDate,
            registrationLink,
            eventTimeStart,
            eventTimeEnd,
            eventFees,
            eventDescription, eventLocation, eventUssdcCat

        );

        // Save event to Firestore
        db.collection("events")
            .add(event)
            .addOnSuccessListener(documentReference -> {
                Toast.makeText(addEventActivity.this, 
                    "Event created successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(addEventActivity.this, ProfileDisplayActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent); // Close the activity and return to the Event Display Activity screen
            })
            .addOnFailureListener(e -> {
                Toast.makeText(addEventActivity.this, 
                    "Error creating event: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
            });
    }
}
