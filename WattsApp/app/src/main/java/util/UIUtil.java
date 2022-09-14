package util;

import android.os.Handler;
import android.os.Looper;
import android.view.View;

public class UIUtil
{
    public static void toggleViews(int setPlaceholder, View placeHolder, View rV)
    {

        new Handler(Looper.getMainLooper()).post(() ->
        {
            if(setPlaceholder == 0)
            {
                rV.setVisibility(View.GONE);
                placeHolder.setVisibility(View.VISIBLE);
            }
            else
            {
                rV.setVisibility(View.VISIBLE);
                placeHolder.setVisibility(View.GONE);
            }

        });

    }
}
