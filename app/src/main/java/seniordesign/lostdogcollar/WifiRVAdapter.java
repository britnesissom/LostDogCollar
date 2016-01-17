package seniordesign.lostdogcollar;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by britne on 1/5/16.
 */
public class WifiRVAdapter extends RecyclerView.Adapter<WifiRVAdapter.ViewHolder> {

    private static final String TAG = "WifiRVAdapter";

    private List<ScanResult> wifiList;
    private Context context;
    private WifiConnectListener listener;

    public WifiRVAdapter(List<ScanResult> wifiList, Context context, WifiConnectListener listener) {
        this.wifiList = wifiList;
        this.context = context;
        this.listener = listener;
    }

    public interface WifiConnectListener {
        void connectCollarToWifi(Context context, ScanResult scanResult);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View view;
        public TextView ssid;
        public ImageView signalStrength;
        public TextView connected;

        public ViewHolder(View v) {
            super(v);
            view = v;
            ssid = (TextView) v.findViewById(R.id.ssid);
            signalStrength = (ImageView) v.findViewById(R.id.signal_level);
            connected = (TextView) v.findViewById(R.id.connected_status);
        }
    }

    @Override
    public WifiRVAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.wifi_item, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        int signalLevel = WifiManager.calculateSignalLevel(wifiList.get(position).level, 5);
        String ssidSignal = wifiList.get(position).SSID + " signal: " + signalLevel;

        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.ssid.setText(wifiList.get(position).SSID);

        if (signalLevel == 4) {
            holder.signalStrength.setImageBitmap(BitmapFactory.decodeResource(context
                    .getResources(), R.drawable.ic_signal_wifi_4_bar));
        } else if (signalLevel == 3) {
            holder.signalStrength.setImageBitmap(BitmapFactory.decodeResource(context
                    .getResources(), R.drawable.ic_signal_wifi_3_bar));
        } else if (signalLevel == 2) {
            holder.signalStrength.setImageBitmap(BitmapFactory.decodeResource(context
                    .getResources(), R.drawable.ic_signal_wifi_2_bar));
        } else if (signalLevel == 1) {
            holder.signalStrength.setImageBitmap(BitmapFactory.decodeResource(context
                    .getResources(), R.drawable.ic_signal_wifi_1_bar));
        } else if (signalLevel == 0) {
            holder.signalStrength.setImageBitmap(BitmapFactory.decodeResource(context
                    .getResources(), R.drawable.ic_signal_wifi_0_bar));
        }

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick pos: " + position);
                listener.connectCollarToWifi(context, wifiList.get(position));
            }
        });

    }

    @Override
    public int getItemCount() {
        return wifiList.size();
    }


}
