package sk.codekitchen.smartfuel.ui.GUI;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import sk.codekitchen.smartfuel.R;

/**
 * Created by Gabriel Lehocky on 15/10/10.
 */
public class LoginFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false
        );
    }
}
