package grodrich7.tfg;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import grodrich7.tfg.Activities.DrivingActivity;
import grodrich7.tfg.Models.Constants;
import grodrich7.tfg.Models.DrivingData;
import grodrich7.tfg.Models.Group;
import grodrich7.tfg.Models.LocationInfo;
import grodrich7.tfg.Models.User;

import static grodrich7.tfg.Models.Constants.DATA_REFERENCE;
import static grodrich7.tfg.Models.Constants.FRIENDS_REFERENCE;
import static grodrich7.tfg.Models.Constants.GROUPS_REFERENCE;
import static grodrich7.tfg.Models.Constants.USERS_REFERENCE;

/**
 * Created by gabri on 14/01/2018.
 */
public class Controller {
    private static Controller instance = null;
    private FirebaseDatabase database;
    private DatabaseReference usersReference;
    private DatabaseReference userGroupsReference;
    private DatabaseReference userFriendsReference;
    private DatabaseReference dataReference;
    private DrivingData drivingData;

    private User currentUser;

    protected Controller() {
        Log.d("CONTROLLER", "Get instance");
        try{
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }catch (DatabaseException ex){

        }
        database = FirebaseDatabase.getInstance();
        usersReference = database.getReference(USERS_REFERENCE);
        usersReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).keepSynced(true);
        userGroupsReference = usersReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(GROUPS_REFERENCE);
        userFriendsReference = usersReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(FRIENDS_REFERENCE);
        dataReference = database.getReference(DATA_REFERENCE);
        userGroupsReference.keepSynced(true);
        userFriendsReference.keepSynced(true);
        dataReference.keepSynced(true);
        drivingData = new DrivingData();
        loadUser();
    }

    private void loadUser() {
        usersReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e("CONTROLLER", "User changes");
                currentUser = dataSnapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("CONTROLLER",databaseError.getMessage());
            }
        });
    }

    //region Groups
    public Task<Void> createGroup(Group group){
        setFriends(group.getUsers());
        return userGroupsReference.push().setValue(group);
    }

    public Task<Void> updateGroup(String key, Group group){
        setFriends(group.getUsers());
        return userGroupsReference.child(key).setValue(group);
    }

    public void setFriends(ArrayList<String> emails){

        for (final String email : emails){
            usersReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try{
                        String uid = (String)((HashMap) dataSnapshot.getValue()).keySet().toArray()[0];
                        DatabaseReference ref = usersReference.child(uid).child("friends"). //uid del que vamos a añadirle como amigo
                                child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        ref.setValue(new User(currentUser.getName(),currentUser.getEmail()));
                    }catch (NullPointerException ex){
                        Log.e("CONTROLLER", "unknown email: " + email);
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    public Task<Void> removeGroup(String key, Group group){
        checkOccurrences(key, group);
        dataReference.child(FirebaseAuth.getInstance().getUid()).child(key).removeValue();
        return userGroupsReference.child(key).removeValue();
    }

    private void checkOccurrences(final String groupKey, Group group){
        for (final String email : group.getUsers()){
            usersReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try{
                        String uid = (String)((HashMap) dataSnapshot.getValue()).keySet().toArray()[0];
                        DatabaseReference ref = usersReference.child(uid).child(FRIENDS_REFERENCE). //uid del que vamos a añadirle como amigo
                                child(FirebaseAuth.getInstance().getCurrentUser().getUid());

                        if (!hasOccurrences(groupKey, email)){
                            ref.removeValue();
                        }
                    }catch (NullPointerException ex){
                        Log.e("CONTROLLER", "unknown email: " + email);
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }
    private boolean hasOccurrences(String groupKey, String email){
        String key; //Group id
        Group value; //Group
        for(HashMap.Entry<String, Group> entry : currentUser.getGroups().entrySet()) {
            key = entry.getKey();
            value = entry.getValue();
            if (!key.equals(groupKey) && value.getUsers().contains(email)){
                return true;
            }
        }
        return false;
    }
    //endregion

    //region ShareData
    public void saveDrivingData(){
        final DatabaseReference ref = dataReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        for(final HashMap.Entry<String, Group> entry : currentUser.getGroups().entrySet()) {
            for (final String email : entry.getValue().getUsers()){
                usersReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        try{
                            String uid = (String)((HashMap) dataSnapshot.getValue()).keySet().toArray()[0];
                            DatabaseReference friend_ref = ref.child(entry.getKey()).child(uid);//TODO : uidUser
                            friend_ref.setValue(getPermittedData(drivingData, entry.getValue()));
                        }catch (NullPointerException ex){
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

        }
    }

    private DrivingData getPermittedData(DrivingData model, Group group){
        DrivingData permitted = new DrivingData();
        String permission;
        boolean isPermitted;
        for(HashMap.Entry<String, Boolean> entry : group.getPermissions().entrySet()) {
            permission = entry.getKey();
            isPermitted = entry.getValue();

            if (isPermitted){
                switch (Constants.Data.valueOf(permission)){
                    case ACCEPT_CALLS:
                        permitted.setAcceptCalls(model.isAcceptCalls());
                        break;
                    case DRIVING:
                        permitted.setDriving(model.isDriving());
                        break;
                    case DESTINATION:
                        permitted.setDestination(model.getDestination());
                        break;
                    case LOCATION:
                        permitted.setLocationInfo(model.getLocationInfo());
                        break;
                    case TRIP_TIME_START:
                        permitted.setStartTimeHour(model.getStartTimeHour());
                        permitted.setStartTimeMin(model.getStartTimeMin());
                        break;
                    case SEARCHING_PARKING:
                        permitted.setSearchingParking(model.isSearchingParking());
                        break;
                }
            }
        }
        return permitted;
    }

    public void updateLocation(Location location){
        LocationInfo locationInfo = new LocationInfo(String.valueOf(location.getLatitude()),String.valueOf(location.getLongitude()), Calendar.getInstance().getTime());
        drivingData.setLocationInfo(locationInfo);
        if (drivingData.isDriving() != null && drivingData.isDriving()) {
            final DatabaseReference ref = dataReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            for (final HashMap.Entry<String, Group> entry : currentUser.getGroups().entrySet()) {
                for (final String email : entry.getValue().getUsers()) {
                    usersReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            try {
                                String uid = (String) ((HashMap) dataSnapshot.getValue()).keySet().toArray()[0];
                                DatabaseReference friend_ref = ref.child(entry.getKey()).child(uid);
                                if (entry.getValue().getPermissions().get(Constants.Data.LOCATION.toString())) {
                                    friend_ref.child("locationInfo").setValue(drivingData.getLocationInfo());
                                }
                            } catch (NullPointerException ex) {
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        }
    }
    public void updateDestination(String destination){
        drivingData.setDestination(destination);
        if (drivingData.isDriving() != null && drivingData.isDriving()){
            final DatabaseReference ref = dataReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            for(final HashMap.Entry<String, Group> entry : currentUser.getGroups().entrySet()) {
                for (final String email : entry.getValue().getUsers()){
                    usersReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            try{
                                String uid = (String)((HashMap) dataSnapshot.getValue()).keySet().toArray()[0];
                                DatabaseReference friend_ref = ref.child(entry.getKey()).child(uid);//TODO : uidUser
                                if (entry.getValue().getPermissions().get(Constants.Data.DESTINATION.toString())){
                                    friend_ref.child("destination").setValue(drivingData.getDestination());
                                }
                            }catch (NullPointerException ex){
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

            }
        }
    }

    public void updateAcceptCalls(boolean acceptCalls){
        drivingData.setAcceptCalls(acceptCalls);
        if (drivingData.isDriving() != null && drivingData.isDriving()){
            final DatabaseReference ref = dataReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            for(final HashMap.Entry<String, Group> entry : currentUser.getGroups().entrySet()) {
                for (final String email : entry.getValue().getUsers()){
                    usersReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            try{
                                String uid = (String)((HashMap) dataSnapshot.getValue()).keySet().toArray()[0];
                                DatabaseReference friend_ref = ref.child(entry.getKey()).child(uid);//TODO : uidUser
                                if (entry.getValue().getPermissions().get(Constants.Data.ACCEPT_CALLS.toString())){
                                    friend_ref.child("acceptCalls").setValue(drivingData.isAcceptCalls());
                                }
                            }catch (NullPointerException ex){
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

            }
        }
    }

    public void updateParking(boolean parking){
        drivingData.setSearchingParking(parking);
        if (drivingData.isDriving() != null && drivingData.isDriving()){
            final DatabaseReference ref = dataReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            for(final HashMap.Entry<String, Group> entry : currentUser.getGroups().entrySet()) {
                for (final String email : entry.getValue().getUsers()){
                    usersReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            try{
                                String uid = (String)((HashMap) dataSnapshot.getValue()).keySet().toArray()[0];
                                DatabaseReference friend_ref = ref.child(entry.getKey()).child(uid);//TODO : uidUser
                                if (entry.getValue().getPermissions().get(Constants.Data.SEARCHING_PARKING.toString())){
                                    friend_ref.child("searchingParking").setValue(drivingData.isSearchingParking());
                                }
                            }catch (NullPointerException ex){
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

            }
        }
    }

    public void endDriving(){
        drivingData.setDriving(false);
        final DatabaseReference ref = dataReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        for(final HashMap.Entry<String, Group> entry : currentUser.getGroups().entrySet()) {
            for (final String email : entry.getValue().getUsers()){
                usersReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        try{
                            String uid = (String)((HashMap) dataSnapshot.getValue()).keySet().toArray()[0];
                            DatabaseReference friend_ref = ref.child(entry.getKey()).child(uid);//TODO : uidUser
                            if (entry.getValue().getPermissions().get(Constants.Data.DRIVING.toString())){
                                friend_ref.child("driving").setValue(drivingData.isDriving());
                            }
                        }catch (NullPointerException ex){
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

        }
    }
    //endregion
    //region GETTERS

    public User getCurrentUser(){
        return currentUser;
    }

    public DatabaseReference getUsersReference() {
        return usersReference;
    }

    public DatabaseReference getUserGroupsReference(){
        return userGroupsReference;
    }

    public DatabaseReference getUserFriendsReference() {
        return userFriendsReference;
    }

    public DatabaseReference getDataReference() {
        return dataReference;
    }

    public DrivingData getDrivingData() {
        return drivingData;
    }

    //endregion

    public void updateImages(final String url){
        if (drivingData.getImages() == null){
            drivingData.setImages(new ArrayList<String>(10));
        }
        if (drivingData.getImages().size() >= 10){
            drivingData.getImages().remove(0);
        }
        drivingData.getImages().add(url);
        if (drivingData.isDriving() != null && drivingData.isDriving()) {
            final DatabaseReference ref = dataReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            for (final HashMap.Entry<String, Group> entry : currentUser.getGroups().entrySet()) {
                for (final String email : entry.getValue().getUsers()) {
                    usersReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            try {
                                String uid = (String) ((HashMap) dataSnapshot.getValue()).keySet().toArray()[0];
                                DatabaseReference friend_ref = ref.child(entry.getKey()).child(uid);//TODO : uidUser
                                friend_ref.child("images").setValue(drivingData.getImages());
                                if (entry.getValue().getPermissions().get(Constants.Data.IMAGES.toString())) {
                                    friend_ref.child("images").setValue(drivingData.getImages()).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e("IMAGES", e.getMessage());
                                        }
                                    });
                                }
                            } catch (NullPointerException ex) {
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        }
    }
    public static Controller getInstance() {
        if(instance == null && FirebaseAuth.getInstance().getCurrentUser() != null) {
            instance = new Controller();
        }
        return instance;
    }
    public static void destroy(){
        instance = null;
    }
}
