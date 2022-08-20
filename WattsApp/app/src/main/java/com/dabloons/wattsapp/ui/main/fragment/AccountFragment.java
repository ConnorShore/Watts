package com.dabloons.wattsapp.ui.main.fragment;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dabloons.wattsapp.R;
import com.dabloons.wattsapp.manager.UserManager;

public class AccountFragment extends Fragment {

    private Button logoutBtn;
    private UserManager userManager = UserManager.getInstance();

    public AccountFragment() {
//        logoutBtn = findViewById(2);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View result =  inflater.inflate(R.layout.fragment_account, container, false);
        result.findViewById(R.id.signOutButton).setOnClickListener( v -> {
            userManager.signOut(this.getContext()).addOnSuccessListener(aVoid -> {
                this.getActivity().finish();
            });
        });
        return result;
    }
}
