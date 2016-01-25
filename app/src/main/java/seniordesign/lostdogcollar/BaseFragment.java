package seniordesign.lostdogcollar;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

/**
 * Created by britne on 1/6/16.
 */
public class BaseFragment extends Fragment {

    private boolean mResolvingError;

    public void setupToolbar(Toolbar toolbar, String title) {
        toolbar.setTitle(title);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        toolbar.setSaveEnabled(false); //TODO: look at this later
    }

    /* Creates a dialog for an error message */
    public void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        GoogleClientErrorDialogFragment dialogFragment = new GoogleClientErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt("dialog_error", errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getChildFragmentManager(), "errordialog");
    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {
        mResolvingError = false;
    }

    public boolean getmResolvingError() {
        return mResolvingError;
    }

    public void setmResolvingError(boolean mResolvingError) {
        this.mResolvingError = mResolvingError;
    }
}
