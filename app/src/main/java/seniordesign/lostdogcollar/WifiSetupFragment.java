package seniordesign.lostdogcollar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by britne on 1/5/16.
 */
public class WifiSetupFragment extends BaseFragment implements WifiPasswordDialogFragment.OnConnectCollarListener, WifiRVAdapter.WifiConnectListener {

    private static final String TAG = "WifiSetupFrag";

    private List<ScanResult> scanList;
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

        scanList = new ArrayList<>();
        adapter = new WifiRVAdapter(scanList, getContext(), this);
        getWifiNetworks();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wifi_setup, container, false);
        setupToolbar((Toolbar) getActivity().findViewById(R.id.toolbar), "Wi-Fi Setup");

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

    //use BroadcastReceiver to let adapter know when wifi list has been retrieved
    private void getWifiNetworks() {
        final WifiManager wifi = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifi.isWifiEnabled()) {
            wifi.setWifiEnabled(true);
        }
        wifi.startScan();

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                sortWifiBySignal(wifi.getScanResults());
                adapter.notifyDataSetChanged();

                recyclerView.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
            }
        };
        getContext().registerReceiver(receiver, new IntentFilter(WifiManager
                .SCAN_RESULTS_AVAILABLE_ACTION));
    }

    // sort wifiList from best signal to worst signal
    private void sortWifiBySignal(final List<ScanResult> scanResultList) {

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                List<ScanResult> results = WifiUtils.removeDuplicatesAndSort(scanResultList);

                scanList.clear();
                scanList.addAll(results);

                adapter.notifyDataSetChanged();
            }
        });

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

    public void showDialog(ScanResult scanResult) {
        String security = WifiUtils.getScanResultSecurity(scanResult);

        if (security.equals("EAP")) {
            DialogFragment passwordDialog = WifiPasswordDialogFragment.newInstance(scanResult,
                    "EAP");
            passwordDialog.show(getChildFragmentManager(), "dialog");
        } else {
            DialogFragment passwordDialog = WifiPasswordDialogFragment.newInstance(scanResult,
                    "else");
            passwordDialog.show(getChildFragmentManager(), "dialog");
        }
    }

    public void showSnackbar(final ScanResult scanResult) {
        Snackbar.make(getActivity().findViewById(R.id.coord_layout), "Can't connect. Password may be " +
                "incorrect.", Snackbar.LENGTH_LONG)
                .setAction("RETRY", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDialog(scanResult);     // open password dialog again
                    }
                }).show();
    }

    public void connectCollarToWifi(String password, ScanResult scanResult) {
        if (!WifiUtils.connectCollarToWifi(getContext(), password, scanResult)) {
            showSnackbar(scanResult);
        } else {

            // update connected text in wifi item
            adapter.notifyDataSetChanged();
        }
    }

    public void connectCollarToEapWifi(String identity, String password, final ScanResult
            scanResult) {
        if (!WifiUtils.connectCollarToEapWifi(getContext(), identity, password, scanResult)) {
            showSnackbar(scanResult);
        } else {

            // need to update connected text in wifi item
            adapter.notifyDataSetChanged();
        }
    }

}
