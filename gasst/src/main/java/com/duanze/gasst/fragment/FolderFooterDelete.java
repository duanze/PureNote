package com.duanze.gasst.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.duanze.gasst.activity.StartActivity;
import com.duanze.gasst.R;

/**
 * Created by Duanze on 2015/5/22.
 */
public class FolderFooterDelete extends Fragment {
    public static final String TAG = "FolderFooterDelete";

    private Button folderCancel;
    private Button folderNum;
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
        View view = inflater.inflate(R.layout.footer_folder_delete, container, false);
        ;
        folderCancel = (Button) view.findViewById(R.id.btn_folder_cancel);
        folderNum = (Button) view.findViewById(R.id.btn_folder_num);
        folderCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((FooterInterface) mContext).changeFooter();
            }
        });

//        LogUtil.i(TAG, "updateDeleteNum(((StartActivity) mContext).getDeleteNum());");
        updateDeleteNum(((StartActivity) mContext).getDeleteNum());

        folderNum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((FooterInterface) mContext).actionClick();
            }
        });
        return view;
    }

    public void updateDeleteNum(int n) {
        folderNum.setText(getString(R.string.folder_delete_num, n));
        if (n == 0) {
            folderNum.setTextColor(getResources().getColor(R.color.grey));
        } else {
            folderNum.setTextColor(getResources().getColor(android.R.color.white));
        }
    }
}
