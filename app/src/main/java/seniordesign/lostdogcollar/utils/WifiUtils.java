package seniordesign.lostdogcollar.utils;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Created by britne on 1/20/16.
 */
public class WifiUtils {

    public static boolean connectCollarToWifi(Context context, String password, final ScanResult
            scanResult) {
        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + scanResult.SSID + "\"";

        String security = getScanResultSecurity(scanResult);

        switch (security) {
            case "WEP":

                try {
                    long pass = Long.parseLong(password, 16);
                    conf.wepKeys[0] = password;
                }
                catch (NumberFormatException e) {
                    // password is not in hex
                    conf.wepKeys[0] = "\"" + password + "\"";
                }

                conf.wepTxKeyIndex = 0;
                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                break;

            case "PSK":
                conf.preSharedKey = "\""+ password +"\"";
                break;

            case "OPEN":
                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                break;

            default:
                Log.d("WifiConnectUtils", "unknown wifi security type");
        }

        return addAndConnect(context, conf);
    }

    private static boolean addAndConnect(final Context context, WifiConfiguration conf) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }

        int id = wifiManager.addNetwork(conf);

        if (id != -1) {
            //Log.d(TAG, "wifi id: " + id);
            wifiManager.disconnect();
            boolean success = wifiManager.enableNetwork(id, true);
            //Log.d(TAG, "enableNetwork success: " + success);
            wifiManager.reconnect();

            if (success) {
                return true;
            }
        }

        return false;
    }

    public static boolean connectCollarToEapWifi(Context context, String identity, String password,
                                              final ScanResult scanResult) {
        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + scanResult.SSID + "\"";

        //TODO: do some stuff here
        return addAndConnect(context, conf);
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

    public static List<ScanResult> sortScanList(List<ScanResult> scanResultList) {
        Collections.sort(scanResultList, new Comparator<ScanResult>() {
            @Override
            public int compare(ScanResult wifi1, ScanResult wifi2) {

                //return negative of value because we want descending list, not ascending
                if (wifi1.level > wifi2.level) {
                    return -1;
                }

                if (wifi1.level < wifi2.level) {
                    return 1;
                }

                return 0;
            }
        });

        return scanResultList;
    }

    public static List<ScanResult> removeDuplicatesAndSort(List<ScanResult> scanResultList) {
        // if wifi name contains only whitespace, remove it from the list
        // places only highest wifi signal in list if there are duplicates
        List<ScanResult> results = new ArrayList<>();
        HashMap<String, Integer> signalStrengthMap = new HashMap<>();

        for (int i = 0; i < scanResultList.size(); i++) {
            /*Log.d(TAG, "i: " + i + ", results size: " + results.size() + ", hashmap size: " +
                    signalStrengthMap.size());*/
            ScanResult wifi = scanResultList.get(i);

            // if the ssid is whitespace then just skip over it
            if (wifi.SSID.matches("^\\s*$")) {
                continue;
            }

            String key = wifi.SSID + " " + wifi.capabilities;
            //Log.d(TAG, "key: " + key);

            if (!signalStrengthMap.containsKey(key)) {

                //need this check or will try to insert at index -1
                if (signalStrengthMap.size() == 0) {
                    signalStrengthMap.put(key, 0);
                } else {
                    signalStrengthMap.put(key, signalStrengthMap.size()-1);
                }
                results.add(wifi);
            } else {
                int position = signalStrengthMap.get(key);
                //Log.d(TAG, "position: " + position);
                ScanResult updateItem = results.get(position);

                if (WifiManager.compareSignalLevel(updateItem.level, wifi.level) > 0) {

                    // replace old ScanResult with updateItem
                    results.set(position, updateItem);
                }
            }
        }

        return sortScanList(results);
    }
}
