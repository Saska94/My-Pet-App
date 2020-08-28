package com.saska.mypetapp.helper;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.saska.mypetapp.R;
import com.saska.mypetapp.db.DBHelper;
import com.saska.mypetapp.db.User;

import java.util.ArrayList;
import java.util.List;

public class MyUsersAdapter extends RecyclerView.Adapter<MyUsersAdapter.ViewHolder> {

    private List<User> mData = new ArrayList<>();;
    private LayoutInflater mInflater;
    private Context context;
    private ProgressBar progresBarUsers;


    // data is passed into the constructor
    public MyUsersAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
        this.context = context;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_user_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bindData(mData.get(position));
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    // resets the list with a new set of data
    public void setItems(List<User> items) {
        mData = items;
    }

    public void setProgresBarUsers(ProgressBar progresBarUsers){
        this.progresBarUsers = progresBarUsers;
    }

    // stores and recycles views as they are scrolled off screen
    class ViewHolder extends RecyclerView.ViewHolder {
        TextView user_text;

        ViewHolder(View itemView) {
            super(itemView);
            user_text = itemView.findViewById(R.id.user);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.out.println("Selected " + user_text.getText().toString() );
                    progresBarUsers.setVisibility(View.VISIBLE);
                    Helper.blockTouch(((Activity)context).getWindow());
                    DBHelper.loadUserDetails(progresBarUsers, context, user_text.getText().toString());
                }
            });
        }

        public void removeAt(int position) {
            mData.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, mData.size());
        }

        void bindData(User item) {
            user_text.setText(item.getUsername());
        }
    }


}