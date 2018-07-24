package com.shivora.puwifimanager.views;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shivora.puwifimanager.R;

public class UserOptionsBottomSheet extends BottomSheetDialogFragment {
    static BottomSheetDialogFragment newInstance(){
        return new BottomSheetDialogFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View bottomSheetView = inflater.inflate(R.layout.layout_bottomsheet_user_options,container,false);
        return bottomSheetView;
    }
}
