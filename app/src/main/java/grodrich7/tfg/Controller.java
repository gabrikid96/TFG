package grodrich7.tfg;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
    private User currentUser;

    protected Controller() {
        Log.d("CONTROLLER", "Get instance");
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        database = FirebaseDatabase.getInstance();
        usersReference = database.getReference("users");
        usersReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).keepSynced(true);
        userGroupsReference = usersReference.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("groups");
        userGroupsReference.keepSynced(true);
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
                System.out.println("The read failed: " + databaseError.getCode());
                Log.e("CONTROLLER",databaseError.getMessage());
            }
        });
    }

    public Task<Void> createGroup(Group group){
        return userGroupsReference.push().setValue(group);
    }

    public Task<Void> updateGroup(String key, Group group){
        return userGroupsReference.child(key).setValue(group);
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

    public String getUserUid(){
        try{
            return FirebaseAuth.getInstance().getCurrentUser().getUid();
        }catch (NullPointerException ex){
            return null;
        }
    }

    //endregion
    public static Controller getInstance() {
        if(instance == null && FirebaseAuth.getInstance().getCurrentUser() != null) {
            instance = new Controller();
        }
        return instance;
    }
}
