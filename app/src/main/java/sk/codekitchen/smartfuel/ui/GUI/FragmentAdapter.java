package sk.codekitchen.smartfuel.ui.GUI;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by Gabriel Lehock
 *
 * Adapter for fragments in .ui.MainAvtivity
 */
public class FragmentAdapter extends FragmentStatePagerAdapter {

    int mNumOfTabs;
    // intro screen 1
    IntroFragment t1;
    // intro screen 2
    IntroFragment t2;
    // intro screen 3
    IntroFragment t3;
    // login and splashscrren
    IntroFragment tLogin;

    /**
     * Constructs object with 4 tabs
     * @param fm
     * @param NumOfTabs
     */
    public FragmentAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;

        t1 = new IntroFragment();
        t1.setContent(0);
        t2 = new IntroFragment();
        t2.setContent(1);
        t3 = new IntroFragment();
        t3.setContent(2);
        tLogin = new IntroFragment();
        tLogin.setContent(3);

    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0: return t1;
            case 1: return t2;
            case 2: return t3;
            case 3: return tLogin;
            default: return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }

}

