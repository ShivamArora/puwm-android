package com.shivora.puwifimanager.views;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.shivora.puwifimanager.R;

public class UserOptionsBottomSheet extends BottomSheetDialogFragment implements View.OnClickListener{
    public static interface UserOptionsClickListener{
        void onUserOptionClicked(View view);
    }

    private ConstraintLayout loginView,editUserView,changePasswordView,deleteUserView;
    private UserOptionsClickListener userOptionsClickListener;

    static BottomSheetDialogFragment newInstance(){
        return new BottomSheetDialogFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View bottomSheetView = inflater.inflate(R.layout.layout_bottomsheet_user_options,container,false);
        loginView = bottomSheetView.findViewById(R.id.item_login);
        editUserView = bottomSheetView.findViewById(R.id.item_edit_user);
        changePasswordView = bottomSheetView.findViewById(R.id.item_change_password);
        deleteUserView = bottomSheetView.findViewById(R.id.item_delete_user);

        loginView.setOnClickListener(this);
        editUserView.setOnClickListener(this);
        changePasswordView.setOnClickListener(this);
        deleteUserView.setOnClickListener(this);
        return bottomSheetView;
    }

    public void setOnUserOptionClickListener(UserOptionsClickListener userOptionClickListener){
        this.userOptionsClickListener = userOptionClickListener;
    }

    @Override
    public void onClick(View v) {

        userOptionsClickListener.onUserOptionClicked(v);
    }
}
