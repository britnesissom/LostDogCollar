package seniordesign.lostdogcollar;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import seniordesign.lostdogcollar.listeners.OnSendCollarIdListener;
import seniordesign.lostdogcollar.models.Collar;

/**
 * Created by britne on 4/25/16.
 */
public class DogNotifRVAdapter extends RecyclerView.Adapter<DogNotifRVAdapter.ViewHolder> {

    private OnSendCollarIdListener listener;
    private List<Collar> collarList;

    public interface OnSendCollarIdListener {
        void onSendCollarId(int id, String name);
    }

    public DogNotifRVAdapter(List<Collar> collarList, OnSendCollarIdListener listener) {
        this.collarList = collarList;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View view;
        public TextView name;

        public ViewHolder(View v) {
            super(v);
            view = v;
            name = (TextView) v.findViewById(android.R.id.text1);
        }
    }

    @Override
    public DogNotifRVAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_selectable_list_item, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        //Log.i("collar adapter", collarList.get(position).getName());
        final int clickPos = holder.getAdapterPosition();

        holder.name.setText(collarList.get(position).getName());
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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