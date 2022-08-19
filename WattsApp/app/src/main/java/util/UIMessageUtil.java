package util;

import android.content.Context;
import android.widget.Toast;

import androidx.viewbinding.ViewBinding;

import com.google.android.material.snackbar.Snackbar;

public class UIMessageUtil {

    /**
     * Show short toast message on screen
     * @param context The context to use (usually Activity class)
     * @param message Message to display
     */
    public static void showShortToastMessage(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Show short toast message on screen
     * @param context The context to use (usually Activity class)
     * @param message Message to display
     */
    public static void showLongToastMessage(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}
