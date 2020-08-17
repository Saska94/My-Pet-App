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
import com.saska.mypetapp.db.Post;

import java.util.ArrayList;
import java.util.List;

public class MyPostsAdapter extends RecyclerView.Adapter<MyPostsAdapter.ViewHolder> {

    private List<Post> mData = new ArrayList<>();;
    private LayoutInflater mInflater;
    private Context context;
    private ProgressBar progressBarPosts;


    // data is passed into the constructor
    public MyPostsAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
        this.context = context;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_post_row, parent, false);
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

    public void setProgressBarPosts(ProgressBar progressBar){
        this.progressBarPosts = progressBar;
    }

    // resets the list with a new set of data
    public void setItems(List<Post> items) {
        mData = items;
    }

    // stores and recycles views as they are scrolled off screen
    class ViewHolder extends RecyclerView.ViewHolder {
        TextView postUser, postHeading, postDate;

        ViewHolder(View itemView) {
            super(itemView);
            postUser = itemView.findViewById(R.id.postUser);
            postHeading = itemView.findViewById(R.id.postHeading);
            postDate = itemView.findViewById(R.id.postDate);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.out.println("Selected " + postUser.getText().toString() );
                    progressBarPosts.setVisibility(View.VISIBLE);
                    Helper.blockTouch(((Activity)context).getWindow());
                    DBHelper.loadPostDetails(progressBarPosts, context, postHeading.getText().toString());
                }
            });
        }

        void bindData(Post item) {
            postUser.setText(item.getUser().getUsername());
            postHeading.setText(item.getHeading());
            postDate.setText(Helper.formatTime(item.getCreatedAt()));
        }
    }


}