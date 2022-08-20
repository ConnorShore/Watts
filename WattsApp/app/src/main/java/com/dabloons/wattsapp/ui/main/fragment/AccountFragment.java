package com.dabloons.wattsapp.ui.main.fragment;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.dabloons.wattsapp.R;
import com.dabloons.wattsapp.manager.UserManager;

public class AccountFragment extends Fragment {

    private UserManager userManager = UserManager.getInstance();

    public AccountFragment() {

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

        result.findViewById(R.id.deleteButton).setOnClickListener(view -> {

            new AlertDialog.Builder(this.getActivity())
                    .setMessage("Are you sure you want to delete your account?")
                    .setPositiveButton("Yes", (dialogInterface, i) ->
                            userManager.deleteUser(this.getActivity())
                                    .addOnSuccessListener(aVoid -> {
                                                this.getActivity().finish();
                                            }
                                    )
                    )
                    .setNegativeButton("No", null)
                    .show();

        });

        return result;
    }
}
