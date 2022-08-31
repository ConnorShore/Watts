package com.dabloons.wattsapp.ui;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;

import com.dabloons.wattsapp.R;
import com.dabloons.wattsapp.databinding.ActivityLoginBinding;
import com.dabloons.wattsapp.manager.UserManager;
import com.dabloons.wattsapp.ui.main.MainActivity;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;

import java.util.Arrays;
import java.util.List;

import util.RequestCodes;
import util.UIMessageUtil;

public class LoginActivity extends BaseActivity<ActivityLoginBinding> {

    private final String LOG_TAG = "LoginActivity";

    private UserManager userManager = UserManager.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        this.initializeListeners();
    }

    public void initializeListeners() {
        binding.buttonLogin.setOnClickListener(view -> {
            if(!userManager.isCurrentUserLogged())
                this.startSignInActivity();
            else
                this.startMainActivity();
        });
    }

    private void startSignInActivity(){

        // Choose authentication providers
        List<AuthUI.IdpConfig> providers =
                Arrays.asList(
                        new AuthUI.IdpConfig.GoogleBuilder().build()
                );

        // Launch the activity
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
//                        .setTheme(R.style.LoginTheme)
                        .setAvailableProviders(providers)
                        .setIsSmartLockEnabled(false, true)
                        .build(),
                RequestCodes.RC_SIGN_IN);
    }

    // Method that handles response after SignIn Activity close
    private void handleResponseAfterSignIn(int requestCode, int resultCode, Intent data){

        IdpResponse response = IdpResponse.fromResultIntent(data);

        if (requestCode == RequestCodes.RC_SIGN_IN) {
            // SUCCESS
            if (resultCode == RESULT_OK) {
                this.userManager.createUser();
                UIMessageUtil.showShortToastMessage(this, "Login successful!");
                this.startMainActivity();
            } else {
                // ERRORS
                if (response == null) {
                    UIMessageUtil.showShortToastMessage(this, "Login aborted.");
                } else if (response.getError()!= null) {
                    if(response.getError().getErrorCode() == ErrorCodes.NO_NETWORK ||
                            response.getError().getErrorCode() == ErrorCodes.INVALID_EMAIL_LINK_ERROR) {
                        UIMessageUtil.showShortToastMessage(this, "No internet connection");
                    } else if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                        UIMessageUtil.showShortToastMessage(this, "Unknown error");
                    }
                }
            }
        }
    }

    private void startMainActivity() {
        Intent mainActivity = new Intent(this, MainActivity.class);
        startActivityForResult(mainActivity, RequestCodes.RC_MAIN_ACTIVITY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.handleResponseAfterSignIn(requestCode, resultCode, data);
    }

    @Override
    protected ActivityLoginBinding getViewBinding() {
        return ActivityLoginBinding.inflate(getLayoutInflater());
    }
}