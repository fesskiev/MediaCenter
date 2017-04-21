package com.fesskiev.mediacenter.ui.chooser;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.fesskiev.mediacenter.R;
import com.fesskiev.mediacenter.utils.CacheManager;
import com.fesskiev.mediacenter.widgets.recycleview.ScrollingLinearLayoutManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class FileSystemChooserFragment extends Fragment {

    interface OnFragmentChooserListener {

        void onSelectDirectory(String path);

        void onCancelChooser();
    }

    public static final String KEY_CURRENT_DIRECTORY = "CURRENT_DIRECTORY";
    private static final String TAG = FileSystemChooserFragment.class.getSimpleName();

    private OnFragmentChooserListener listener;

    private String newDirectoryName;
    private String initialDirectory;

    private Button buttonConfirm;
    private TextView selectedFolderTextView;

    private ChooserAdapter adapter;

    private File selectedDir;
    private FileObserver fileObserver;

    public static FileSystemChooserFragment newInstance() {
        final FileSystemChooserFragment fragment = new FileSystemChooserFragment();
        final Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (OnFragmentChooserListener) context;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        newDirectoryName = "TEST";
        initialDirectory = CacheManager.EXTERNAL_STORAGE;

        if (savedInstanceState != null) {
            initialDirectory = savedInstanceState.getString(KEY_CURRENT_DIRECTORY);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (selectedDir != null) {
            outState.putString(KEY_CURRENT_DIRECTORY, selectedDir.getAbsolutePath());
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.directory_chooser, container, false);

        buttonConfirm = (Button) view.findViewById(R.id.btnConfirm);
        Button buttonCancel = (Button) view.findViewById(R.id.btnCancel);
        ImageButton buttonNavUp = (ImageButton) view.findViewById(R.id.btnNavUp);

        selectedFolderTextView = (TextView) view.findViewById(R.id.txtvSelectedFolder);

        buttonConfirm.setOnClickListener(v -> {
            if (isValidFile(selectedDir)) {
                selectChooserFolder();
            }
        });

        buttonCancel.setOnClickListener(v -> cancelChooser());


        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new ScrollingLinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false, 1000));
        adapter = new ChooserAdapter();
        recyclerView.setAdapter(adapter);

        buttonNavUp.setOnClickListener(v -> {
            final File parent;
            if (selectedDir != null
                    && (parent = selectedDir.getParentFile()) != null) {
                changeDirectory(parent);
            }
        });


        final File initialDir;
        if (!TextUtils.isEmpty(initialDirectory) && isValidFile(new File(initialDirectory))) {
            initialDir = new File(initialDirectory);
        } else {
            initialDir = Environment.getExternalStorageDirectory();
        }

        changeDirectory(initialDir);

        return view;
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
            selectedDir = dir;
            selectedFolderTextView.setText(dir.getAbsolutePath());

            adapter.refresh(Arrays.asList(contents));

            fileObserver = createFileObserver(dir.getAbsolutePath());
            fileObserver.startWatching();

        }
        refreshButtonState();
    }


    private void refreshButtonState() {
        final Activity activity = getActivity();
        if (activity != null && selectedDir != null) {
            buttonConfirm.setEnabled(isValidFile(selectedDir));
        }
    }


    private void refreshDirectory() {
        if (selectedDir != null) {
            changeDirectory(selectedDir);
        }
    }


    private FileObserver createFileObserver(final String path) {
        return new FileObserver(path, FileObserver.CREATE | FileObserver.DELETE
                | FileObserver.MOVED_FROM | FileObserver.MOVED_TO) {

            @Override
            public void onEvent(final int event, final String path) {
                final Activity activity = getActivity();

                if (activity != null) {
                    activity.runOnUiThread(() -> refreshDirectory());
                }
            }
        };
    }


    private void selectChooserFolder() {
        if (listener != null) {
            listener.onSelectDirectory(selectedDir.getAbsolutePath());
        }
    }

    private void cancelChooser() {
        if (listener != null) {
            listener.onCancelChooser();
        }
    }


    private boolean isValidFile(final File file) {
        return (file != null && file.isDirectory() && file.canRead() && file.canWrite());
    }

    private class ChooserAdapter extends RecyclerView.Adapter<ChooserAdapter.ViewHolder> {

        private List<File> files;

        public ChooserAdapter() {
            files = new ArrayList<>();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            ImageView itemIcon;
            TextView fileName;

            public ViewHolder(View v) {
                super(v);

                itemIcon = (ImageView) v.findViewById(R.id.itemChooserIcon);
                fileName = (TextView) v.findViewById(R.id.itemChooserFileName);

                v.setOnClickListener(view -> processFileSystem(getAdapterPosition()));
            }
        }

        private void processFileSystem(int position) {
            changeDirectory(files.get(position));
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
                    holder.itemIcon.setImageResource(R.drawable.icon_chooser_folder);
                } else if (file.isFile()) {
                    holder.itemIcon.setImageResource(R.drawable.icon_chooser_file);
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
