package grodrich7.tfg;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import grodrich7.tfg.Models.Group;
import grodrich7.tfg.Models.User;

/**
 * Created by gabri on 14/01/2018.
 */

public class Controller {
    private static Controller instance = null;
    private FirebaseDatabase database;
    public DatabaseReference usersReference;
    public DatabaseReference userGroupsReference;
    public DatabaseReference userFriendsReference;

    private User currentUser;

    protected Controller() {
        Log.d("CONTROLLER", "Get instance");
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        database = FirebaseDatabase.getInstance();
        usersReference = database.getReference("users");
        usersReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).keepSynced(true);
        userGroupsReference = usersReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("groups");
        userFriendsReference = usersReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("friends");

        userGroupsReference.keepSynced(true);
        userFriendsReference.keepSynced(true);
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
        return userGroupsReference.child(key).removeValue();
    }

    private void checkOccurrences(final String groupKey, Group group){
        for (final String email : group.getUsers()){
            usersReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try{
                        String uid = (String)((HashMap) dataSnapshot.getValue()).keySet().toArray()[0];
                        DatabaseReference ref = usersReference.child(uid).child("friends"). //uid del que vamos a añadirle como amigo
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

    //endregion
    public static Controller getInstance() {
        if(instance == null && FirebaseAuth.getInstance().getCurrentUser() != null) {
            instance = new Controller();
        }
        return instance;
    }
}
