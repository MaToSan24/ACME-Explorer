package com.example.acmeexplorer;

import com.example.acmeexplorer.entity.Trip;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Map;

public class FirebaseDatabaseService {
    private static String userId;
    private static FirebaseDatabaseService service;
    private static FirebaseDatabase mDatabase;

    public static FirebaseDatabaseService getServiceInstance() {
        if (service == null || mDatabase == null) {
            service = new FirebaseDatabaseService();
            mDatabase = FirebaseDatabase.getInstance();
            mDatabase.setPersistenceEnabled(true);
        }

        if (userId == null || userId.isEmpty()) {
            userId = FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : "";
        }

        return service;
    }

    public DatabaseReference getTrip(String tripId) {
        return mDatabase.getReference("users/" + userId + "/trips/" + tripId).getRef();
    }

    public DatabaseReference getTrips() {
        System.out.println("Getting trips for user: " + userId);
        return mDatabase.getReference("users/" + userId + "/trips").getRef();
    }

    public void upsertTrip(Trip trip, DatabaseReference.CompletionListener completionListener) {
        System.out.println("Upserting trips for user: " + userId);
        Map<String, Object> tripValues = trip.toMap();
        mDatabase.getReference("users/" + userId + "/trips/" + trip.getId()).setValue(tripValues, completionListener);
    }

    public void deleteTrip(String tripId, DatabaseReference.CompletionListener completionListener) {
        mDatabase.getReference("users/" + userId + "/trips/" + tripId).removeValue(completionListener);
    }

    public void deleteAllTrips(DatabaseReference.CompletionListener completionListener) {
        System.out.println("Deleting all trips for user: " + userId);
        mDatabase.getReference("users/" + userId + "/trips").removeValue(completionListener);
    }

    public void setUserId(String userId) {
        FirebaseDatabaseService.userId = userId;
    }
}
