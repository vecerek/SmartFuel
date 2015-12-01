package sk.codekitchen.smartfuel.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import sk.codekitchen.smartfuel.R;

/**
 * @author Gabriel Lehocky
 */
public class FragmentShop extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragmnet_shop, container, false);

        return view;
    }
}