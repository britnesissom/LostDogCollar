package seniordesign.lostdogcollar;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import seniordesign.lostdogcollar.models.Collar;

/**
 * Created by britne on 3/13/16.
 */
public class CollarListRVAdapter extends RecyclerView.Adapter<CollarListRVAdapter.ViewHolder> {

    private static final String TAG = "CollarRVAdapter";

    private List<Collar> collarList;
    private OnSendCollarIdListener listener;
    private int selected;

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
        public TextView currentCollar;
        public TextView batteryLife;

        public ViewHolder(View v) {
            super(v);
            view = v;
            name = (TextView) v.findViewById(R.id.collar_name);
            currentCollar = (TextView) v.findViewById(R.id.current_collar);
            batteryLife = (TextView) v.findViewById(R.id.battery_life);
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
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        //Log.i("collar adapter", collarList.get(position).getName());
        final int clickPos = holder.getAdapterPosition();

        /*if (holder.currentCollar.getVisibility() == View.VISIBLE) {
            Log.d(TAG, "set visibility to gone: " + collarList.get(position).getName());
            holder.currentCollar.setVisibility(View.GONE);
            //holder.currentCollar.
        }*/

        String battery = collarList.get(position).getBattery() + "%";
        holder.batteryLife.setText(battery);
        holder.name.setText(collarList.get(position).getName());
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // send collar id to display map
                //Log.d(TAG, "pos: " + clickPos);
                Log.d(TAG, "on click: " + collarList.get(clickPos).getName() + " " + collarList
                        .get(clickPos).getId());
                //holder.currentCollar.setVisibility(View.VISIBLE);
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
