package seniordesign.lostdogcollar.fragments.dialogs;


import android.app.AlertDialog;
import android.app.Dialog;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;

import seniordesign.lostdogcollar.models.Collar;
import seniordesign.lostdogcollar.listeners.OnRefreshCollarsListener;
import seniordesign.lostdogcollar.listeners.OnSendResponseListener;
import seniordesign.lostdogcollar.RetrieveFromServerAsyncTask;

public class DeleteCollarDialogFragment extends DialogFragment {

    private static final String TAG = "AddCollarDialog";
    private static final String ID = "id";
    private static final String NAME = "name";

    private OnRefreshCollarsListener listener;
    private int id;
    private String name;

    public static DeleteCollarDialogFragment newInstance(int id, String name) {
        DeleteCollarDialogFragment fragment = new DeleteCollarDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ID, id);
        args.putString(NAME, name);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            id = getArguments().getInt(ID);
            name = getArguments().getString(NAME);
        }

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

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Delete Collar");
        builder.setMessage("Are you sure you want to delete " + name + "\'s collar?");
        builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteCollar();
            }
        });

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        return builder.create();

    }

    private void deleteCollar() {
        String message = "REMOVE_COLLAR " + id + " \r\n";

        RetrieveFromServerAsyncTask rsat = new RetrieveFromServerAsyncTask(new OnSendResponseListener() {
            @Override
            public void onSendResponse(String response) {
                //Log.d(TAG, "collar removed");
                Collar collar = new Collar(id, name, "");
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