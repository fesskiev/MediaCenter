package com.fesskiev.mediacenter.ui.chooser;


import android.content.Intent;
import android.os.Bundle;
import android.os.FileObserver;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.utils.CacheManager;
import com.fesskiev.mediacenter.utils.Utils;
import com.fesskiev.mediacenter.widgets.recycleview.ScrollingLinearLayoutManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileSystemChooserActivity extends AppCompatActivity {

    public static final String EXTRA_SELECT_TYPE = "com.fesskiev.mediacenter.EXTRA_SELECT_TYPE";
    public static final String EXTRA_EXTENSION = "com.fesskiev.mediacenter.EXTRA_EXTENSION";

    public static final String TYPE_FOLDER = "FOLDER";
    public static final String TYPE_FILE = "FILE";

    public static final String EXTENSION_CUE = "CUE";

    public static final String RESULT_SELECTED_PATH = "RESULT_SELECTED_PATH";
    public static final String KEY_CURRENT_PATH = "CURRENT_PATH";
    public static final int RESULT_CODE_PATH_SELECTED = 11;


    private TextView selectedPathText;
    private ChooserAdapter adapter;
    private File selectedPath;
    private FileObserver fileObserver;
    private String selectType;
    private String extension;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directory_chooser);

        Button buttonConfirm = findViewById(R.id.buttonConfirm);
        Button buttonCancel = findViewById(R.id.buttonCancel);
        ImageButton buttonNavUp = findViewById(R.id.buttonNavUp);

        selectedPathText = findViewById(R.id.textSelectedPath);

        buttonConfirm.setOnClickListener(v -> checkConfirm());

        buttonCancel.setOnClickListener(v -> cancelChooser());

        RecyclerView recyclerView = findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new ScrollingLinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false, 1000));
        adapter = new ChooserAdapter();
        recyclerView.setAdapter(adapter);

        buttonNavUp.setOnClickListener(v -> {
            File parent;
            if (selectedPath != null
                    && (parent = selectedPath.getParentFile()) != null) {
                changeDirectory(parent);
            }
        });

        String initialDirectory = CacheManager.EXTERNAL_STORAGE;
        if (savedInstanceState != null) {
            initialDirectory = savedInstanceState.getString(KEY_CURRENT_PATH);
        } else {
            Bundle extras = getIntent().getExtras();
            selectType = extras.getString(EXTRA_SELECT_TYPE);
            extension = extras.getString(EXTRA_EXTENSION);
        }

        changeDirectory(new File(initialDirectory));
    }

    private void checkConfirm() {
        if (extension != null) {
            if (!selectedPath.isDirectory() && isValidExtension(selectedPath)) {
                selectChooserFolder();
            } else {
                Utils.showCustomSnackbar(findViewById(R.id.chooserRoot), getApplicationContext(),
                        getString(R.string.snackbar_chooser_extension_error),
                        Snackbar.LENGTH_LONG).show();
            }
        } else {
            if (isValidFile(selectedPath)) {
                selectChooserFolder();
            } else {
                Utils.showCustomSnackbar(findViewById(R.id.chooserRoot), getApplicationContext(),
                        getString(R.string.snackbar_chooser_file_error),
                        Snackbar.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (selectedPath != null) {
            outState.putString(KEY_CURRENT_PATH, selectedPath.getAbsolutePath());
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        if (fileObserver != null) {
            fileObserver.stopWatching();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (fileObserver != null) {
            fileObserver.startWatching();
        }
    }

    private void changeDirectory(final File dir) {
        final File[] contents = dir.listFiles();
        if (contents != null) {
            selectedPath = dir;
            selectedPathText.setText(dir.getAbsolutePath());

            adapter.refresh(Arrays.asList(contents));

            fileObserver = createFileObserver(dir.getAbsolutePath());
            fileObserver.startWatching();

        }
    }


    private void refreshDirectory() {
        if (selectedPath != null) {
            changeDirectory(selectedPath);
        }
    }


    private FileObserver createFileObserver(final String path) {
        return new FileObserver(path, FileObserver.CREATE | FileObserver.DELETE
                | FileObserver.MOVED_FROM | FileObserver.MOVED_TO) {

            @Override
            public void onEvent(final int event, final String path) {
                runOnUiThread(() -> refreshDirectory());
            }
        };
    }


    private void selectChooserFolder() {
        final Intent intent = new Intent();
        intent.putExtra(RESULT_SELECTED_PATH, selectedPath.getAbsolutePath());
        setResult(RESULT_CODE_PATH_SELECTED, intent);
        finish();
    }

    private void cancelChooser() {
        setResult(RESULT_CANCELED);
        finish();
    }


    private boolean isValidFile(File file) {
        switch (selectType) {
            case TYPE_FOLDER:
                return (file != null && file.isDirectory() && file.canRead() && file.canWrite());
            case TYPE_FILE:
                return (file != null && file.isFile() && file.canRead() && file.canWrite());
        }
        return false;
    }

    private boolean isValidExtension(File file) {
        switch (extension) {
            case EXTENSION_CUE:
                return isFileCue(file);
        }
        return false;
    }

    private boolean isFileCue(File file) {
        String path = file.getAbsolutePath();
        String extension = path.substring(path.lastIndexOf("."));
        return extension.equalsIgnoreCase(".cue");
    }

    private class ChooserAdapter extends RecyclerView.Adapter<ChooserAdapter.ViewHolder> {

        private List<File> files;

        ChooserAdapter() {
            files = new ArrayList<>();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            ImageView itemIcon;
            TextView fileName;

            public ViewHolder(View v) {
                super(v);

                itemIcon = v.findViewById(R.id.itemChooserIcon);
                fileName = v.findViewById(R.id.itemChooserFileName);

                v.setOnClickListener(view -> processFileSystem(getAdapterPosition()));
            }
        }

        private void processFileSystem(int position) {
            File file = files.get(position);
            if (file.isDirectory()) {
                changeDirectory(file);
            } else {
                selectedPath = file;
                selectedPathText.setText(file.getAbsolutePath());
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chooser, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            File file = files.get(position);
            if (file != null) {
                holder.fileName.setText(file.getName());
                if (file.isDirectory()) {
                    holder.itemIcon.setImageResource(R.drawable.icon_choose_folder);
                } else if (file.isFile()) {
                    if (extension != null) {
                        if (isFileCue(file)) {
                            holder.itemIcon.setImageResource(R.drawable.icon_cue);
                        } else {
                            holder.itemIcon.setImageResource(R.drawable.icon_choose_file);
                        }
                    } else {
                        holder.itemIcon.setImageResource(R.drawable.icon_choose_file);
                    }
                } else {
                    holder.itemIcon.setImageResource(0);
                }
            }
        }

        @Override
        public int getItemCount() {
            return files.size();
        }

        public void refresh(List<File> files) {
            this.files.clear();
            this.files.addAll(files);
            notifyDataSetChanged();
        }
    }
}
