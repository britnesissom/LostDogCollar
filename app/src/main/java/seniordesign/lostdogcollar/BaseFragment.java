package seniordesign.lostdogcollar;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

/**
 * Created by britne on 1/6/16.
 */
public class BaseFragment extends Fragment {

    public void setupToolbar(Toolbar toolbar, String title) {
        toolbar.setTitle(title);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        toolbar.setSaveEnabled(false); //TODO: look at this later
    }
}
