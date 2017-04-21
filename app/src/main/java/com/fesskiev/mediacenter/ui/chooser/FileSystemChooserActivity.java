package com.fesskiev.mediacenter.ui.chooser;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.fesskiev.mediacenter.R;

public class FileSystemChooserActivity extends AppCompatActivity implements FileSystemChooserFragment.OnFragmentChooserListener {

    public static final String RESULT_SELECTED_DIR = "RESULT_SELECTED_DIR";
    public static final int RESULT_CODE_DIR_SELECTED = 11;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.directory_chooser_activity);

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.main, FileSystemChooserFragment.newInstance(),
                    FileSystemChooserFragment.class.getName());
            transaction.commit();
        }
    }


    @Override
    public void onSelectDirectory(String path) {
        final Intent intent = new Intent();
        intent.putExtra(RESULT_SELECTED_DIR, path);
        setResult(RESULT_CODE_DIR_SELECTED, intent);
        finish();
    }

    @Override
    public void onCancelChooser() {
        setResult(RESULT_CANCELED);
        finish();
    }
}
