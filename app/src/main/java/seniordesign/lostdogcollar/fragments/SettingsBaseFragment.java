package seniordesign.lostdogcollar.fragments;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

/**
 * Created by britne on 1/26/16.
 */
public class SettingsBaseFragment extends Fragment {

    public void setupToolbar(Toolbar toolbar, String title) {
        toolbar.setTitle(title);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        toolbar.setSaveEnabled(false); //TODO: look at this later
    }

}
