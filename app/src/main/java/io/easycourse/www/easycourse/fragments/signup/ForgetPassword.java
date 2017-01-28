package io.easycourse.www.easycourse.fragments.signup;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import io.easycourse.www.easycourse.R;
import io.easycourse.www.easycourse.utils.APIFunctions;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

/**
 * Created by Mark Wen on 10/18/2016.
 */

public class ForgetPassword extends Fragment {

    @BindView(R.id.forgetPasswordEmailLayout)
    TextInputLayout emailInputLayout;
    @BindView(R.id.forgetPasswordEmail)
    EditText email;
    @BindView(R.id.forgetPasswordSubmitButton)
    Button submitButton;

    public ForgetPassword() {
    }

    public static ForgetPassword newInstance() {
        return new ForgetPassword();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.forget_password, container, false);
        ButterKnife.bind(this, v);

        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().equals("")) {
                    submitButton.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.login_button_disable, null));
                } else if (!validateEmail()) {
                    submitButton.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.login_button_disable, null));
                } else {
                    submitButton.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.login_button, null));
                }
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (email.getText().toString().equals("")) {
                    Snackbar.make(view, "Please enter an valid email address", Snackbar.LENGTH_LONG).show();
                } else if (!validateEmail()) {
                    Snackbar.make(view, "Please enter an valid email address", Snackbar.LENGTH_LONG).show();
                } else {
                    try {
                        APIFunctions.forgetPassword(getContext(), email.getText().toString(), new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                Snackbar.make(view, "An email with a link to reset your password has been sent to the email address provided.", Snackbar.LENGTH_LONG).show();
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                                try {
                                    String error = errorResponse.getString("error");
                                    Snackbar.make(view, statusCode + " " + error, Snackbar.LENGTH_LONG).show();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                if (statusCode == 200) {
                                    Snackbar.make(view, "An email with a link to reset your password has been sent to the email address provided.", Snackbar.LENGTH_LONG).show();
                                    return;
                                }
                                Snackbar.make(view, statusCode + " " + responseString, Snackbar.LENGTH_LONG).show();
                            }
                        });
                    } catch (JSONException | UnsupportedEncodingException | FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        v.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_BACK) {
                    getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    return true;
                }
                return false;
            }
        });

        return v;
    }

    private boolean validateEmail() {
        String emailString = email.getText().toString().trim();

        if (emailString.isEmpty()) {
            emailInputLayout.setError("Missing email");
            email.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailString).matches()) {
            emailInputLayout.setError("Email is not correct");
            email.requestFocus();
            return false;
        }

        emailInputLayout.setErrorEnabled(false);
        return true;
    }

}
