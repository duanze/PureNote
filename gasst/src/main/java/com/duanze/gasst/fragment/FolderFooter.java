package com.duanze.gasst.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.duanze.gasst.R;

/**
 * Created by Duanze on 2015/5/22.
 */
public class FolderFooter extends Fragment {
    public static final String TAG  = "FolderFooter";

    private Button folderDelete;
    private Button folderNew;
    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.footer_folder, container, false);
        folderDelete = (Button) view.findViewById(R.id.btn_folder_delete);
        folderNew = (Button) view.findViewById(R.id.btn_folder_new);
        folderDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((FooterInterface)mContext).changeFooter();
            }
        });

        folderNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((FooterInterface)mContext).actionClick();
            }
        });
        return view;
    }
}
