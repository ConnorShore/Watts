package com.dabloons.wattsapp.ui;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.dabloons.wattsapp.R;
import com.dabloons.wattsapp.databinding.ActivityLoginBinding;
import com.dabloons.wattsapp.databinding.ActivityMainBinding;
import com.dabloons.wattsapp.manager.UserManager;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;

import java.util.Arrays;
import java.util.List;

import util.UIMessageUtil;

public class LoginActivity extends BaseActivity<ActivityLoginBinding> {

    // Request codes
    private static final int RC_MAIN_ACTIVITY = 1;
    private static final int RC_SIGN_IN = 2;

    private UserManager userManager = UserManager.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                        .setTheme(R.style.LoginTheme)
                        .setAvailableProviders(providers)
                        .setIsSmartLockEnabled(false, true)
//                        .setLogo(R.drawable.ic_logo_auth)
                        .build(),
                RC_SIGN_IN);
    }

    // Method that handles response after SignIn Activity close
    private void handleResponseAfterSignIn(int requestCode, int resultCode, Intent data){

        IdpResponse response = IdpResponse.fromResultIntent(data);

        if (requestCode == RC_SIGN_IN) {
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
                    if(response.getError().getErrorCode() == ErrorCodes.NO_NETWORK){
                        UIMessageUtil.showShortToastMessage(this, "No internet connection");
                    } else if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                        UIMessageUtil.showShortToastMessage(this, "Unknown error");
                    }
                }
            }
        }
    }

    private void startMainActivity() {
        Intent gameActivity = new Intent(this, MainActivity.class);
        startActivityForResult(gameActivity, RC_MAIN_ACTIVITY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.handleResponseAfterSignIn(requestCode, resultCode, data);
    }

    @Override
    ActivityLoginBinding getViewBinding() {
        return ActivityLoginBinding.inflate(getLayoutInflater());
    }
}