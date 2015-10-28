package sk.codekitchen.smartfuel.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import sk.codekitchen.smartfuel.R;
import sk.codekitchen.smartfuel.ui.views.LightTextView;
import sk.codekitchen.smartfuel.ui.views.Utils;

/**
 * Created by Gabriel Lehocky on 15/10/10.
 *
 * Fragment that contains one intro screen
 */
public class IntroFragment extends Fragment {

    private int id;

    /**
     * Sets id, that specifies which fragment to show.
     * @param id must be from 0 to 3
     */
    public void setContent(int id){
        this.id = id;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_intro, container, false);

        LightTextView introTitle;
        LightTextView introText;
        LinearLayout bck;

        introTitle = (LightTextView) view.findViewById(R.id.intro_title);
        introText = (LightTextView) view.findViewById(R.id.intro_comment);
        bck = (LinearLayout) view.findViewById(R.id.fragment_bck);

        /**
         * Based on id fills the content of the intro screen
         */
        switch (id){
            case 0:
                introTitle.setText(getString(R.string.intro_1));
                introText.setText(getString(R.string.intro_1_comment));
                Utils.setBackgroundOfView(getActivity(), bck, R.drawable.bck1);
                break;
            case 1:
                introTitle.setText(getString(R.string.intro_2));
                introText.setText(getString(R.string.intro_2_comment));
                Utils.setBackgroundOfView(getActivity(), bck, R.drawable.bck2);
                break;
            case 2:
                introTitle.setText(getString(R.string.intro_3));
                introText.setText(getString(R.string.intro_3_comment));
                Utils.setBackgroundOfView(getActivity(), bck, R.drawable.bck3);
                break;
            case 3:
                bck.setVisibility(View.GONE);
                break;
        }
        return view;
    }

}
