package grodrich7.tfg.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import grodrich7.tfg.Models.DrivingData;
import grodrich7.tfg.Models.User;
import grodrich7.tfg.R;

public class ViewUsersActivity extends HelperActivity {

    private RecyclerView friends_list;
    private FirebaseRecyclerAdapter<User, FriendHolder> friendsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        putFriends();
    }

    @Override
    protected void onStart() {
        super.onStart();
        friendsAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        friendsAdapter.stopListening();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
            default:
                break;
        }

        return true;
    }

    @Override
    protected void getViewsByXML() {
        setContentView(R.layout.activity_view_users);
        enableToolbar(R.string.view_users);
        /*List View*/
        friends_list = (RecyclerView) findViewById(R.id.friends_list);
    }

    private void putFriends(){
        final int lastPosition = -1;
        Query query = controller.getUserFriendsReference();
        FirebaseRecyclerOptions<User> options =
                new FirebaseRecyclerOptions.Builder<User>()
                        .setQuery(query, User.class)
                        .build();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        friends_list.setLayoutManager(linearLayoutManager);
        friendsAdapter = new FirebaseRecyclerAdapter<User, FriendHolder>(options) {
            @Override
            public FriendHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.view_users_item, parent, false);

                return new ViewUsersActivity.FriendHolder(view);
            }

            @Override
            protected void onBindViewHolder(final FriendHolder holder, final int position, final User model) {
                //final DatabaseReference postRef = getRef(holder.getAdapterPosition());
                //final String postKey = postRef.getKey();
                holder.name_label.setText(model.getName().toUpperCase());
                holder.email_label.setText(String.valueOf(model.getEmail()));
                toggleDrivingIcon(holder.action_btn, true);
                isDriving(holder.action_btn, getRef(position).getKey());
                if (position > lastPosition)
                {
                    holder.setAnimation();
                }

                holder.container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(ViewUsersActivity.this,ViewUserActivity.class);
                        intent.putExtra("key",getRef(position).getKey());
                        intent.putExtra("name",model.getName().toUpperCase());
                        startActivity(intent);
                        overridePendingTransition(R.anim.transition_left_in, R.anim.transition_left_out);
                    }
                });
            }
        };

        friends_list.setAdapter(friendsAdapter);
    }

    private void toggleDrivingIcon(ImageView drivingState, boolean isDriving){
        drivingState.setBackgroundResource(isDriving ? R.mipmap.driving_on : R.mipmap.driving_off);
    }

    private void isDriving(final ImageView drivingState, String friendUid){
        final boolean[] isDriving = {false};
        controller.getDataReference().child(friendUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try{
                    for (DataSnapshot groupSnapshot : dataSnapshot.getChildren()) {
                        for (DataSnapshot drivingDataSnapshot : groupSnapshot.getChildren()){
                            if (drivingDataSnapshot.getKey().equals(FirebaseAuth.getInstance().getUid())){
                                DrivingData drivingData = drivingDataSnapshot.getValue(DrivingData.class);
                                isDriving[0] = drivingData.isDriving() != null && drivingData.isDriving();
                                if (isDriving[0]){
                                    break;
                                }
                            }
                        }
                        if (isDriving[0]){
                            break;
                        }
                    }
                }catch (Exception ex){
                    Log.e("VIEW", ex.getMessage());
                }finally {
                    toggleDrivingIcon(drivingState, isDriving[0]);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private class FriendHolder extends RecyclerView.ViewHolder {

        TextView name_label;
        ImageView action_btn;
        TextView email_label;
        LinearLayout container;

        public FriendHolder(View itemView) {
            super(itemView);
            name_label = (TextView) itemView.findViewById(R.id.name_label);
            action_btn = (ImageView) itemView.findViewById(R.id.action_btn);
            email_label = (TextView) itemView.findViewById(R.id.email_label);
            container = (LinearLayout) itemView.findViewById(R.id.shape_layout);
        }

        private void setAnimation()
        {
            Animation animation = AnimationUtils.loadAnimation(this.itemView.getContext(), android.R.anim.slide_in_left);
            this.itemView.startAnimation(animation);
        }
    }
}
