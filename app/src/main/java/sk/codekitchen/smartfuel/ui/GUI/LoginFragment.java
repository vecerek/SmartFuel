package sk.codekitchen.smartfuel.ui.GUI;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import sk.codekitchen.smartfuel.R;

/**
 * Created by Gabriel Lehocky on 15/10/10.
 */
public class LoginFragment extends Fragment implements View.OnClickListener {

    private Button login;
    private EditLightTextView mail;
    private EditLightTextView pass;
    private LightTextView forgotten;
    private LightTextView register;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        login = (Button) view.findViewById(R.id.login_btn);
        login.setOnClickListener(this);
        mail = (EditLightTextView) view.findViewById(R.id.login_mail);
        pass = (EditLightTextView) view.findViewById(R.id.login_pass);
        register = (LightTextView) view.findViewById(R.id.login_register);
        register.setOnClickListener(this);
        forgotten = (LightTextView) view.findViewById(R.id.login_forgotten);
        forgotten.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.login_btn:
                break;
            case R.id.login_forgotten:
                break;
            case R.id.login_register:
                break;
        }
    }
}
