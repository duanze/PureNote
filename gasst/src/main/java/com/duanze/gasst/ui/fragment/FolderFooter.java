package com.duanze.gasst.ui.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.duanze.gasst.R;

/**
 * Created by Duanze on 2015/5/22.
 */
public class FolderFooter extends Fragment {
    public static final String TAG = "FolderFooter";
    public static final int FLAG = 0;

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

        view.findViewById(R.id.btn_folder_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((FooterInterface) mContext).changeFooter();
            }
        });

        view.findViewById(R.id.btn_folder_new).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((FooterInterface) mContext).actionClick();
            }
        });
        return view;
    }
}
