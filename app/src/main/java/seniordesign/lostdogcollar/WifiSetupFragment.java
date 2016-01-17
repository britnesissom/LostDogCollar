package seniordesign.lostdogcollar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by britne on 1/5/16.
 */
public class WifiSetupFragment extends BaseFragment implements WifiRVAdapter.WifiConnectListener {

    private static final String TAG = "WifiSetupFrag";

    private List<ScanResult> scanResultList;
    private BroadcastReceiver receiver;
    private RecyclerView.Adapter adapter;
    private RecyclerView recyclerView;
    private ProgressBar mProgressBar;

    public WifiSetupFragment() {

    }

    public static WifiSetupFragment newInstance() {
        return new WifiSetupFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scanResultList = new ArrayList<>();
        adapter = new WifiRVAdapter(scanResultList, getContext(), this);
        getWifiNetworks();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wifi_setup, container, false);
        setupToolbar((Toolbar) view.findViewById(R.id.toolbar), "Wi-Fi Setup");

        mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar);

        recyclerView = (RecyclerView) view.findViewById(R.id.wifi_rv);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        recyclerView.setAdapter(adapter);

        return view;
    }

    private void getWifiNetworks() {
        final WifiManager wifi = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifi.isWifiEnabled()) {
            //probably need to get permission to do this
            wifi.setWifiEnabled(true);
        }
        wifi.startScan();

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                scanResultList.clear();
                scanResultList.addAll(wifi.getScanResults());
                sortWifiBySignal();

                adapter.notifyDataSetChanged();
                recyclerView.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
            }
        };
        getContext().registerReceiver(receiver, new IntentFilter(WifiManager
                .SCAN_RESULTS_AVAILABLE_ACTION));
    }

    // sort wifiList from best signal to worst signal
    private void sortWifiBySignal() {

        // if wifi name contains only whitespace, remove it from the list
        List<ScanResult> results = new ArrayList<>();
        for (ScanResult wifi : scanResultList) {
            if (!wifi.SSID.matches("^\\s*$")) {
                results.add(wifi);
            }
        }

        scanResultList.clear();
        scanResultList.addAll(results);

        Collections.sort(scanResultList, new Comparator<ScanResult>() {
            @Override
            public int compare(ScanResult lhs, ScanResult rhs) {
                if (lhs.level > rhs.level) {
                    return 1;
                }
                else if (lhs.level == rhs.level) {
                    return 0;
                }
                else if (lhs.level < rhs.level) {
                    return -1;
                }

                Log.d(TAG, "unreachable code for sorting");
                return 2;   //should not reach here
            }
        });

        Collections.reverse(scanResultList);
    }

    @Override
    public void onPause() {
        super.onPause();
        getContext().unregisterReceiver(receiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        getContext().registerReceiver(receiver, new IntentFilter(WifiManager
                .SCAN_RESULTS_AVAILABLE_ACTION));
    }

    public void connectCollarToWifi(Context context, ScanResult scanResult) {
        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + scanResult.SSID + "\"";

        String security = getScanResultSecurity(scanResult);
        if (security.equals("WEP")) {

        } else if (security.equals("EAP")) {

        } else if (security.equals("PSK")) {

        } else if (security.equals("OPEN")) {

        } else {
            Log.d(TAG, "unknown wifi security type");
        }
    }

    public static String getScanResultSecurity(ScanResult scanResult) {
        final String cap = scanResult.capabilities;
        final String[] securityModes = { "WEP", "EAP", "PSK" };
        for (int i = securityModes.length - 1; i >= 0; i--) {
            if (cap.contains(securityModes[i])) {
                return securityModes[i];
            }
        }

        return "OPEN";
    }
}
