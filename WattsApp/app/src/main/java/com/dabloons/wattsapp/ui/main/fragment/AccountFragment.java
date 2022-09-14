package com.dabloons.wattsapp.ui.main.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.dabloons.wattsapp.ui.LoginActivity;
import com.dabloons.wattsapp.ui.main.MainActivity;

import util.RequestCodes;
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
                SharedPreferences sharedPref = getActivity().getApplicationContext().getSharedPreferences("TEST", 0);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(getString(R.string.logged_in), false);
                editor.apply();
                this.getActivity().finish();
            });
        });

        result.findViewById(R.id.deleteButton).setOnClickListener(view -> {

            new AlertDialog.Builder(this.getActivity())
                    .setMessage("Are you sure you want to delete your account?")
                    .setPositiveButton("Yes", (dialogInterface, i) ->
                            userManager.deleteUser(WattsApplication.getAppContext(), (var, status) -> {
                                if(status.success) {
                                    UIMessageUtil.showLongToastMessage(
                                            WattsApplication.getAppContext(),
                                            "Successfully deleted user");
                                    startLoginActivity();
                                } else {
                                    Log.e(LOG_TAG, "Failed to delete user: " + status.message);
                                    UIMessageUtil.showLongToastMessage(
                                            WattsApplication.getAppContext(),
                                            "Successfully deleted user");
                                }
                                return null;
                            })
                    )
                    .setNegativeButton("No", null)
                    .show();

        });

        return result;
    }

    private void startLoginActivity() {
        Intent mainActivity = new Intent(WattsApplication.getAppContext(), LoginActivity.class);
        startActivityForResult(mainActivity, RequestCodes.RC_LOGIN_ACTIVITY);
    }

}
