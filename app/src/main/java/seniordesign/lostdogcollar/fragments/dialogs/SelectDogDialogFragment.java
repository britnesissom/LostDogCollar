package seniordesign.lostdogcollar.fragments.dialogs;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import seniordesign.lostdogcollar.DogNotifRVAdapter;
import seniordesign.lostdogcollar.R;
import seniordesign.lostdogcollar.ResponseConverterUtil;
import seniordesign.lostdogcollar.RetrieveFromServerAsyncTask;
import seniordesign.lostdogcollar.listeners.OnSendResponseListener;
import seniordesign.lostdogcollar.models.Collar;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SelectDogDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SelectDogDialogFragment extends DialogFragment implements DogNotifRVAdapter
        .OnSendCollarIdListener {

    private List<Collar> collarList;
    private DogNotifRVAdapter adapter;
    private OnSelectDogListener listener;
    private AlertDialog d;

    public interface OnSelectDogListener {
        void onSelectDog(int id);
    }


    public SelectDogDialogFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static SelectDogDialogFragment newInstance() {
        return new SelectDogDialogFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initCollarList();
        collarList = new ArrayList<>();
        adapter = new DogNotifRVAdapter(collarList, this);

        try {
            listener = (OnSelectDogListener) getParentFragment();
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
        View view = inflater.inflate(R.layout.collar_list_dialog, null);
        setupRecyclerView(view);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setTitle("Dog for Notification Changes");

        d = builder.create();

        return d;

    }

    private void setupRecyclerView(View view) {
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.collar_rv);
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);

        recyclerView.setAdapter(adapter);
    }

    private void initCollarList() {
        String message = "GET_COLLARS \r\n";
        RetrieveFromServerAsyncTask rsat = new RetrieveFromServerAsyncTask(new OnSendResponseListener() {
            @Override
            public void onSendResponse(String response) {
                // TODO: find out why this is called twice
                if (response.equals("")) {
                    return;
                }
                List<Collar> collars = ResponseConverterUtil.convertResponseToCollarList(response);

                if (collars.size() == 0) {
                    // TODO: display dialog saying 'you have no saved collars!'
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), "No saved collars", Toast.LENGTH_SHORT).show();
                        }
                    });

                    return;
                }

                collarList.clear();
                collarList.addAll(collars);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("SelectDogFrag", "adapter changed");
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            rsat.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, message);
        } else {
            rsat.execute(message);
        }
    }

    @Override
    public void onSendCollarId(int id, String name) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                d.dismiss();
            }
        });
        listener.onSelectDog(id);
    }
}
