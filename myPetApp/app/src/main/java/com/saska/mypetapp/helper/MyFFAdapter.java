package com.saska.mypetapp.helper;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.saska.mypetapp.R;
import com.saska.mypetapp.db.DBHelper;
import com.saska.mypetapp.db.FFact;
import com.saska.mypetapp.singletons.AppContext;

import java.util.ArrayList;
import java.util.List;

public class MyFFAdapter extends RecyclerView.Adapter<MyFFAdapter.ViewHolder> {

    private List<FFact> mData = new ArrayList<>();;
    private LayoutInflater mInflater;
    private Context context;


    // data is passed into the constructor
    public MyFFAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
        this.context = context;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_ff_row, parent, false);
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
    public void setItems(List<FFact> items) {
        mData = items;
    }

    // stores and recycles views as they are scrolled off screen
    class ViewHolder extends RecyclerView.ViewHolder {
        TextView ff_text;

        ViewHolder(View itemView) {
            super(itemView);
            ff_text = itemView.findViewById(R.id.ff_text);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setCancelable(true);
                    if (AppContext.getContext().getActiveUser().isUser()){
                        builder.setTitle("Fun Fact");
                        builder.setMessage(ff_text.getText().toString());
                        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                    }
                    else{
                        builder.setTitle("Delete Fun Fact");
                        builder.setMessage("Are you sure you want to delete selected fun fact?");
                        builder.setPositiveButton("Confirm",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        FFact toRemove = null;
                                        for(FFact fact :mData){
                                            if (ff_text.getText().toString().equals(fact.getText())){
                                                toRemove = fact;
                                                break;
                                            }
                                        }
                                        if (toRemove != null){
                                            removeAt(getPosition());
                                            DBHelper.deleteFunFact(toRemove.getId());
                                        }
                                    }
                                });
                        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                    }


                    AlertDialog dialog = builder.create();
                    dialog.show();
                    return true;
                };
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.out.println("Selected " + ff_text.getText().toString() );
                }
            });
        }

        public void removeAt(int position) {
            mData.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, mData.size());
        }

        void bindData(FFact item) {
            Log.i("AAAAAA", "setting text to : " + item.getText());
            ff_text.setText(item.getText());
            //loadImage(petImageLayout, item.getImageBitmap());
        }
    }


}