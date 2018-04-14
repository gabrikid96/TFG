package grodrich7.tfg.Activities;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import grodrich7.tfg.Models.Group;
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
            protected void onBindViewHolder(final FriendHolder holder, int position, final User model) {
                //final DatabaseReference postRef = getRef(holder.getAdapterPosition());
                //final String postKey = postRef.getKey();
                holder.name_label.setText(model.getName().toUpperCase());
                holder.email_label.setText(String.valueOf(model.getEmail()));
                toggleDrivingIcon(holder.action_btn, ViewUserActivity.getRandomBoolean());
                if (position > lastPosition)
                {
                    holder.setAnimation();
                }

                holder.container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        launchIntent(ViewUserActivity.class, TRANSITION_RIGHT);
                    }
                });
            }
        };

        friends_list.setAdapter(friendsAdapter);
    }

    private void toggleDrivingIcon(ImageView drivingState, boolean isDriving){
        drivingState.setBackgroundResource(isDriving ? R.mipmap.driving_on : R.mipmap.driving_off);
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
