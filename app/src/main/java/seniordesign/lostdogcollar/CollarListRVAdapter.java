package seniordesign.lostdogcollar;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by britne on 3/13/16.
 */
public class CollarListRVAdapter extends RecyclerView.Adapter<CollarListRVAdapter.ViewHolder> {

    private List<Collar> collarList;
    private Context context;
    private OnSendCollarIdListener listener;

    public interface OnSendCollarIdListener {
        void onSendCollarId(int id);
    }

    public CollarListRVAdapter(List<Collar> collarList, Context context,
                                       OnSendCollarIdListener listener) {
        this.collarList = collarList;
        this.context = context;
        this.listener = listener;

        for (int i = 0; i < collarList.size(); i++) {
            Log.i("CollarAdapter", collarList.get(i).getName());
        }
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
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Log.i("collar adapter", collarList.get(position).getName());
        holder.name.setText(collarList.get(position).getName());
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // send collar id to display map
                listener.onSendCollarId(collarList.get(holder.getAdapterPosition()).getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return collarList.size();
    }
}
