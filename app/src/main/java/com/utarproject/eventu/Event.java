package com.utarproject.eventu;

public class Event {
    private String organizerId;
    private String eventName;
    private String eventDate;
    private String registrationLink;
    private String eventTimeStart;
    private String eventTimeEnd;
    private String eventFees;
    private String eventDescription;
    private String eventUssdcCat;
    private String eventLocation;


    // Empty constructor required for Firestore
    public Event() {}

    public Event(String organizerId, String eventName, String eventDate, String registrationLink, String eventTimeStart, String eventTimeEnd, String eventFees, String eventDescription, String eventLocation, String eventUssdcCat) {
        this.organizerId = organizerId;
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.registrationLink = registrationLink;
        this.eventTimeStart = eventTimeStart;
        this.eventTimeEnd = eventTimeEnd;
        this.eventFees = eventFees;
        this.eventDescription = eventDescription;
        this.eventLocation = eventLocation;
        this.eventUssdcCat = eventUssdcCat;
    }

    // Getters and Setters
    public String getOrganizerId() {

        return organizerId;
    }

    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getRegistrationTime() {
        return registrationLink;
    }

    public void setRegistrationTime(String registrationLink) {
        this.registrationLink = registrationLink;
    }

    public String getEventTimeStart() {
        return eventTimeStart;
    }

    public void setEventTimeStart(String eventTimeStart) {
        this.eventTimeStart = eventTimeStart;
    }

    public String getEventTimeEnd() {
        return eventTimeEnd;
    }

    public void setEventTimeEnd(String eventTimeEnd) {
        this.eventTimeEnd = eventTimeEnd;
    }

    public String getEventFees() {
        return eventFees;
    }

    public void setEventFees(String eventFees) {
        this.eventFees = eventFees;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public String getEventUssdcCat() {
        return eventUssdcCat;
    }

    public void setEventUssdcCat(String eventUssdcCat) {
        this.eventUssdcCat = eventUssdcCat;
    }

    public String getEventLocation() {
        return eventLocation;
    }

    public void setEventLocation(String eventLocation) {
        this.eventLocation = eventLocation;
    }
} 