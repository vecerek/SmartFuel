package sk.codekitchen.smartfuel.ui.GUI;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import sk.codekitchen.smartfuel.R;

/**
 * Created by Gabriel Lehocky on 15/10/10.
 */
public class IntroFragment extends Fragment {

    private int id;

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

        switch (id){
            case 0:
                introTitle.setText(getString(R.string.intro_1));
                introText.setText(getString(R.string.intro_1_comment));
                Utils.setBackgroundOfView(getActivity(),bck, R.mipmap.bck1);
                break;
            case 1:
                introTitle.setText(getString(R.string.intro_2));
                introText.setText(getString(R.string.intro_2_comment));
                Utils.setBackgroundOfView(getActivity(), bck, R.mipmap.bck2);
                break;
            case 2:
                introTitle.setText(getString(R.string.intro_3));
                introText.setText(getString(R.string.intro_3_comment));
                Utils.setBackgroundOfView(getActivity(), bck, R.mipmap.bck3);
                break;
            case 3:
                bck.setVisibility(View.GONE);
                break;
            case 4:
                introText.setVisibility(View.INVISIBLE);
                introTitle.setVisibility(View.INVISIBLE);
                ImageView img = (ImageView) view.findViewById(R.id.intro_logo);
                img.setVisibility(View.VISIBLE);
        }
        return view;
    }

}
