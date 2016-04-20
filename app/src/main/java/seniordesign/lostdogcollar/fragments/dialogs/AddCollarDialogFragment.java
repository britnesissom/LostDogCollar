package seniordesign.lostdogcollar.fragments.dialogs;

import android.app.Dialog;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import seniordesign.lostdogcollar.models.Collar;
import seniordesign.lostdogcollar.listeners.OnRefreshCollarsListener;
import seniordesign.lostdogcollar.listeners.OnSendResponseListener;
import seniordesign.lostdogcollar.R;
import seniordesign.lostdogcollar.RetrieveFromServerAsyncTask;

// TODO: error check if user doesn't enter collar id or name
public class AddCollarDialogFragment extends DialogFragment {

    private static final String TAG = "AddCollarDialog";

    private OnRefreshCollarsListener listener;

    public static AddCollarDialogFragment newInstance() {
        return new AddCollarDialogFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            listener = (OnRefreshCollarsListener) getParentFragment();
            //Log.d(TAG, "listener: " + listener);
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling Fragment must implement " +
                    "OnRefreshCollarsListener");
        }
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_add_collar_dialog, null);

        final EditText editTextId = (EditText) view.findViewById(R.id.collar_id);
        final EditText editTextName = (EditText) view.findViewById(R.id.collar_name);
        final TextView enterId = (TextView) view.findViewById(R.id.reenter_id);
        final TextView enterName = (TextView) view.findViewById(R.id.reenter_name);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setTitle("Add New Collar");
        builder.setPositiveButton("ADD", null);

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        final AlertDialog d = builder.create();
        d.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {

                Button b = d.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        enterId.setVisibility(View.GONE);
                        enterName.setVisibility(View.GONE);

                        if (TextUtils.isEmpty(editTextId.getText())) {
                            enterId.setText(getString(R.string.enter_id));
                            enterId.setVisibility(View.VISIBLE);
                        }
                        else if (TextUtils.isEmpty(editTextName.getText())) {
                            enterName.setText(getString(R.string.enter_name));
                            enterName.setVisibility(View.VISIBLE);
                        }
                        else {
                            sendCollarToServer(editTextId.getText().toString(), editTextName.getText().toString());
                            d.dismiss();
                        }
                    }
                });
            }
        });

        return d;

    }

    private void sendCollarToServer(final String id, final String name) {
        String message = "ADD_COLLAR " + id + " " + name + "\r\n";

        RetrieveFromServerAsyncTask rsat = new RetrieveFromServerAsyncTask(new OnSendResponseListener() {
            @Override
            public void onSendResponse(String response) {
                Log.d(TAG, "collar added");
                Collar collar = new Collar(Integer.parseInt(id), name, "100");
                listener.refreshCollarList(collar);
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            rsat.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, message);
        } else {
            rsat.execute(message);
        }
    }
}
