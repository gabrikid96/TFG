package grodrich7.tfg.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import java.util.HashMap;

import grodrich7.tfg.Models.Group;
import grodrich7.tfg.Models.User;
import grodrich7.tfg.R;

public class GroupsActivity extends HelperActivity {

    private RecyclerView groups_list;
    private ProgressBar progressBar;
    private FirebaseRecyclerAdapter groupsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadGroups();
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


    private void loadGroups() {
        new AsyncTask<Void,Void,Void>(){
            @Override
            protected Void doInBackground(Void... voids) {
                while(controller.getCurrentUser() == null);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                progressBar.setVisibility(View.GONE);
                putGroups();
                groupsAdapter.startListening();
            }
        }.execute();
    }

    protected void getViewsByXML() {
        setContentView(R.layout.activity_groups);
        enableToolbar(R.string.groups);
        /*List View*/
        groups_list = (RecyclerView) findViewById(R.id.groups_list);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
    }

    private void putGroups() {
        final int lastPosition = -1;
        Query query = controller.getUserGroupsReference();
        FirebaseRecyclerOptions<Group> options =
                new FirebaseRecyclerOptions.Builder<Group>()
                        .setQuery(query, Group.class)
                        .build();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        groups_list.setLayoutManager(linearLayoutManager);
        groupsAdapter = new FirebaseRecyclerAdapter<Group, GroupHolder>(options) {
            @Override
            public GroupHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.group_item, parent, false);

                return new GroupHolder(view);
            }

            @Override
            protected void onBindViewHolder(final GroupHolder holder, int position, final Group model) {
                final DatabaseReference postRef = getRef(holder.getAdapterPosition());
                final String postKey = postRef.getKey();
                holder.name_label.setText(model.getNameGroup().toUpperCase());
                try{
                    holder.user_count_label.setText(String.valueOf(model.getUsers().size()) + " " +  getApplicationContext().getResources().getString(R.string.users));
                }catch (NullPointerException e){
                    holder.user_count_label.setText("0 " + getApplicationContext().getResources().getString(R.string.users));
                }
                if (position > lastPosition)
                {
                    holder.setAnimation();
                }

                holder.container.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        editGroup(postKey,model);
                    }
                });

                holder.action_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final Animation animation = AnimationUtils.loadAnimation(GroupsActivity.this, android.R.anim.slide_out_right);
                        animation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                deleteGroup(postKey, model);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                        holder.itemView.startAnimation(animation);


                    }
                });
            }
        };

        groups_list.setAdapter(groupsAdapter);
    }

    public void createGroup(View v){
        Intent intent = new Intent(this,GroupActivity.class);
        startActivityForResult(intent,GROUP_EDIT);
        overridePendingTransition(R.anim.transition_left_in, R.anim.transition_left_out);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GROUP_EDIT) {
            if(resultCode == Activity.RESULT_OK)
                //groupsAdapter.updateData(getGroupsArray());

            if (resultCode == Activity.RESULT_CANCELED) {

            }
        }
    }

    public void editGroup(String postKey, Group group){
        Intent intent = new Intent(this,GroupActivity.class);
        intent.putExtra("group",group);
        intent.putExtra("key",postKey);
        startActivityForResult(intent,GROUP_EDIT);
        overridePendingTransition(R.anim.transition_left_in, R.anim.transition_left_out);
    }

    public void deleteGroup(String postKey, final Group group){
        controller.removeGroup(postKey, group);
        Snackbar mySnackbar = Snackbar.make(groups_list,
                R.string.deleted_group, Snackbar.LENGTH_LONG);
        mySnackbar.setAction(R.string.undo, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controller.createGroup(group);
            }
        });
        mySnackbar.show();
    }



    private class GroupHolder extends RecyclerView.ViewHolder {

        TextView name_label;
        ImageButton action_btn;
        TextView user_count_label;
        LinearLayout container;

        public GroupHolder(View itemView) {
            super(itemView);
            name_label = (TextView) itemView.findViewById(R.id.name_label);
            action_btn = (ImageButton) itemView.findViewById(R.id.action_btn);
            user_count_label = (TextView) itemView.findViewById(R.id.user_count_label);
            container = (LinearLayout) itemView.findViewById(R.id.shape_layout);
        }

        private void setAnimation()
        {
            Animation animation = AnimationUtils.loadAnimation(this.itemView.getContext(), android.R.anim.slide_in_left);
            this.itemView.startAnimation(animation);
        }
    }
}
