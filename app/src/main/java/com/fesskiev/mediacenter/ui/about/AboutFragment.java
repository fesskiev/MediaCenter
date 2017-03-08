package com.fesskiev.mediacenter.ui.about;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fesskiev.mediacenter.BuildConfig;
import com.fesskiev.mediacenter.R;


public class AboutFragment extends Fragment implements View.OnClickListener {

    public static AboutFragment newInstance() {
        return new AboutFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((TextView)view.findViewById(R.id.versionValue)).setText(BuildConfig.VERSION_NAME);

        view.findViewById(R.id.mailContainer).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.mailContainer:
                sendMail();
                break;
        }
    }

    private void sendMail() {
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("plain/text");
        intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{getString(R.string.mail_address)});
        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.mail_subject));
        startActivity(Intent.createChooser(intent, getString(R.string.mail_chooser)));
    }

}
