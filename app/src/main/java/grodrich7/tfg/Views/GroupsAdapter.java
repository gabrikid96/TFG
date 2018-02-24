package grodrich7.tfg.Views;

import android.content.Context;
import android.media.Image;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import grodrich7.tfg.Models.Group;
import grodrich7.tfg.R;

/**
 * Created by gabri on 30/01/2018.
 */

public class GroupsAdapter extends ArrayAdapter<Group> {

    private ArrayList<Group> groups;
    private Context mContext;
    private int lastPosition = -1;

    // View lookup cache
    private static class GroupItem {
        TextView name_label;
        ImageButton action_btn;
        TextView user_count_label;
    }

    public GroupsAdapter(ArrayList<Group> groups, Context context) {
        super(context, R.layout.group_item, groups);
        this.groups = groups;
        this.mContext=context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Group group = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        GroupItem groupItem; // view lookup cache stored in tag

        final View result;
        if (convertView == null) {
            groupItem = new GroupItem();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.group_item, parent, false);
            groupItem.name_label = (TextView) convertView.findViewById(R.id.name_label);
            groupItem.action_btn = (ImageButton) convertView.findViewById(R.id.action_btn);
            groupItem.user_count_label = (TextView) convertView.findViewById(R.id.user_count_label);
            result=convertView;
            convertView.setTag(groupItem);
        } else {
            groupItem = (GroupItem) convertView.getTag();
            result=convertView;
        }

        Animation animation = AnimationUtils.loadAnimation(mContext, (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
        result.startAnimation(animation);
        lastPosition = position;

        groupItem.name_label.setText(group.getNameGroup());
        try{
            groupItem.user_count_label.setText(String.valueOf(group.getUsers().size()));
        }catch (NullPointerException e){
            groupItem.user_count_label.setText("0");
        }

        return convertView;
    }

    public void updateData(ArrayList<Group> list){
        groups = list;
    }
}
