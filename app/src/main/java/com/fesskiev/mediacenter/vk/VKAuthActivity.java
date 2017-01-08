package com.fesskiev.mediacenter.vk;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.fesskiev.mediacenter.MediaApplication;
import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.data.source.DataRepository;
import com.fesskiev.mediacenter.data.source.remote.ErrorHelper;
import com.fesskiev.mediacenter.utils.AnimationUtils;
import com.fesskiev.mediacenter.utils.AppLog;
import com.fesskiev.mediacenter.utils.AppSettingsManager;
import com.fesskiev.mediacenter.utils.RxUtils;
import com.fesskiev.mediacenter.data.model.vk.User;
import com.fesskiev.mediacenter.widgets.MaterialProgressBar;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class VKAuthActivity extends AppCompatActivity {

    public static final int VK_AUTH_RESULT = 10;

    private Subscription subscription;
    private AppSettingsManager settingsManager;

    private MaterialProgressBar progressBar;
    private EditText loginEditText;
    private EditText passwordEditText;
    private Button signInButton;
    private CheckBox rememberUser;
    private String login;
    private String password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vk_auth);

        TypedValue typedValue = new TypedValue();
        getResources().getValue(R.dimen.activity_window_height, typedValue, true);
        float scaleValue = typedValue.getFloat();

        int height = (int) (getResources().getDisplayMetrics().heightPixels * scaleValue);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, height);

        settingsManager = AppSettingsManager.getInstance();

        login = "";
        password = "";

        rememberUser = (CheckBox) findViewById(R.id.rememberCheckBox);
        rememberUser.setOnCheckedChangeListener((buttonView, isChecked) -> rememberUser(isChecked));

        signInButton = (Button) findViewById(R.id.signInButton);
        signInButton.setOnClickListener(view -> singIn(login, password));

        loginEditText = (EditText) findViewById(R.id.editLogin);
        passwordEditText = (EditText) findViewById(R.id.editPassword);

        progressBar = (MaterialProgressBar) findViewById(R.id.progressBar);

        LoginTextWatcher loginTextWatcher = new LoginTextWatcher();

        loginEditText.addTextChangedListener(loginTextWatcher);
        passwordEditText.addTextChangedListener(loginTextWatcher);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxUtils.unsubscribe(subscription);
    }


    private class LoginTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (editable == passwordEditText.getEditableText()) {
                password = editable.toString();
            } else if (editable == loginEditText.getEditableText()) {
                login = editable.toString();
            }
        }
    }

    private void singIn(String login, String password) {
        if (!TextUtils.isEmpty(login) && !TextUtils.isEmpty(password)) {
            showProgressBar();
            DataRepository repository = MediaApplication.getInstance().getRepository();
            subscription = repository.auth(login, password)
                    .flatMap(oAuth -> {

                        settingsManager.setAuthToken(oAuth.getToken());
                        settingsManager.setUserId(oAuth.getUserId());

                        return repository.getUser();
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(userResponse -> {

                        User user = userResponse.getUser();
                        if (user != null) {
                            settingsManager.setUserFirstName(user.getFirstName());
                            settingsManager.setUserLastName(user.getLastName());
                            settingsManager.setPhotoURL(user.getPhotoUrl());

                            finishAuth();
                        }
                    }, this::checkRequestError);
        } else {
            AnimationUtils.getInstance().errorAnimation(signInButton);
        }
    }

    private void checkRequestError(Throwable throwable) {
        hideProgressBar();
        ErrorHelper.getInstance().createErrorSnackBar(this, throwable, null);
    }

    private void finishAuth() {
        setResult(Activity.RESULT_OK);
        finish();
    }

    private void rememberUser(boolean isChecked) {

    }

    public void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }


    public void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

}
