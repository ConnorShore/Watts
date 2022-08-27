package com.dabloons.wattsapp.ui.main.fragment;

import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.dabloons.wattsapp.R;
import com.dabloons.wattsapp.WattsApplication;
import com.dabloons.wattsapp.manager.UserManager;

import util.UIMessageUtil;
import util.WattsCallback;
import util.WattsCallbackStatus;

public class AccountFragment extends Fragment {

    private final String LOG_TAG = "AccountFragment";

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
                            userManager.deleteUser(WattsApplication.getAppContext(), new WattsCallback<Void, Void>() {

                                @Override
                                public Void apply(Void var, WattsCallbackStatus status) {
                                    if(status.success) {
                                        UIMessageUtil.showLongToastMessage(
                                                WattsApplication.getAppContext(),
                                                "Successfully deleted user");
                                    } else {
                                        Log.e(LOG_TAG, "Failed to delete user: " + status.message);
                                        UIMessageUtil.showLongToastMessage(
                                                WattsApplication.getAppContext(),
                                                "Successfully deleted user");
                                    }
                                    return null;
                                }
                            })
                    )
                    .setNegativeButton("No", null)
                    .show();

        });

        return result;
    }
}
