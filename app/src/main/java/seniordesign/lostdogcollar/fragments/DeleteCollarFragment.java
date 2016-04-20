package seniordesign.lostdogcollar.fragments;


import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import seniordesign.lostdogcollar.listeners.OnBackPressedListener;
import seniordesign.lostdogcollar.models.Collar;
import seniordesign.lostdogcollar.R;
import seniordesign.lostdogcollar.ResponseConverterUtil;
import seniordesign.lostdogcollar.RetrieveFromServerAsyncTask;
import seniordesign.lostdogcollar.fragments.dialogs.DeleteCollarDialogFragment;
import seniordesign.lostdogcollar.listeners.OnRefreshCollarsListener;
import seniordesign.lostdogcollar.listeners.OnSendResponseListener;
import seniordesign.lostdogcollar.CollarListRVAdapter;

/**
 * Displays list of collars that user can click to delete. Opens {@link DeleteCollarDialogFragment}
 * dialog asking user to confirm deletion
 */
public class DeleteCollarFragment extends Fragment implements CollarListRVAdapter
        .OnSendCollarIdListener, OnRefreshCollarsListener {

    private static final String TAG = "DeleteCollarFrag";
    private static final String USERNAME = "username";

    private String username;
    private List<Collar> collarList;
    private CollarListRVAdapter adapter;


    public DeleteCollarFragment() {
        // Required empty public constructor
    }

    public static DeleteCollarFragment newInstance(String username) {
        DeleteCollarFragment fragment = new DeleteCollarFragment();
        Bundle args = new Bundle();
        args.putString(USERNAME, username);
        fragment.setArguments(args);
        return new DeleteCollarFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            username = getArguments().getString(USERNAME);
        }

        collarList = new ArrayList<>();
        adapter = new CollarListRVAdapter(collarList, this);

        initCollarList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_delete_collar, container, false);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        toolbar.setSaveEnabled(false); //TODO: look at this later

        setupRecyclerView(view);

        return view;
    }

    private void setupRecyclerView(View view) {
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.collar_rv);
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);

        recyclerView.setAdapter(adapter);
    }

    /**
     * get the list of collars for a certain user
     */
    private void initCollarList() {
        String message = "GET_COLLARS \r\n";
        RetrieveFromServerAsyncTask rsat = new RetrieveFromServerAsyncTask(new OnSendResponseListener() {
            @Override
            public void onSendResponse(String response) {
                Log.d(TAG, "response: " + response);
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
    public void refreshCollarList(Collar collar) {
        Log.d(TAG, "refresh collar list");

        collarList.remove(collar);
        List<Collar> list = new ArrayList<>(collarList);
        collarList.clear();
        collarList.addAll(list);

        Log.d(TAG, "size: " + collarList.size());

        //Log.d(TAG, "size after: " + collarList.size());
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "collar removed");
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onSendCollarId(int id, String name) {
        DialogFragment deleteCollarDialog = DeleteCollarDialogFragment.newInstance(id, name);
        deleteCollarDialog.show(getChildFragmentManager(), "dialog");
    }
}
