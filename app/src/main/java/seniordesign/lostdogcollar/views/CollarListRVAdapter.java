package seniordesign.lostdogcollar.views;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import seniordesign.lostdogcollar.Collar;
import seniordesign.lostdogcollar.R;

/**
 * Created by britne on 3/13/16.
 */
public class CollarListRVAdapter extends RecyclerView.Adapter<CollarListRVAdapter.ViewHolder> {

    private static final String TAG = "CollarRVAdapter";

    private List<Collar> collarList;
    private OnSendCollarIdListener listener;

    public interface OnSendCollarIdListener {
        void onSendCollarId(int id, String name);
    }

    public CollarListRVAdapter(List<Collar> collarList, OnSendCollarIdListener listener) {
        this.collarList = collarList;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View view;
        public TextView name;

        public ViewHolder(View v) {
            super(v);
            view = v;
            name = (TextView) v.findViewById(R.id.collar_name);
        }
    }

    @Override
    public CollarListRVAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.collar_rv_item, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        //Log.i("collar adapter", collarList.get(position).getName());
        final int clickPos = holder.getAdapterPosition();
        holder.name.setText(collarList.get(position).getName());
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // send collar id to display map
                //Log.d(TAG, "pos: " + clickPos);
                listener.onSendCollarId(collarList.get(clickPos).getId(),
                        collarList.get(clickPos).getName());
            }
        });
    }

    @Override
    public int getItemCount() {
        return collarList.size();
    }
}
