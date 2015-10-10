package sk.codekitchen.smartfuel.ui.GUI;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import sk.codekitchen.smartfuel.R;

/**
 * Created by Gabriel Lehocky on 15/10/10.
 */
public class IntroFragment extends Fragment {

    public void setContent(int id){
        switch (id){
            case 0:
                break;
            case 1:
                break;
            case 2:
                break;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_intro, container, false
        );
    }

}
