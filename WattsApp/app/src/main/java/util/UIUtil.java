package util;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;

import com.dabloons.wattsapp.R;
import com.dabloons.wattsapp.manager.RoomManager;
import com.dabloons.wattsapp.manager.UserManager;
import com.dabloons.wattsapp.model.Room;
import com.dabloons.wattsapp.model.integration.IntegrationType;
import com.dabloons.wattsapp.ui.room.adapters.LightItemAdapter;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

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

    public static void addIntegrationChips(View v, ChipGroup chipGroup)
    {

        UserManager.getInstance().getUserIntegrations((integrationTypes, status) -> {
            for(IntegrationType type : integrationTypes)
            {
                LayoutInflater inflater = LayoutInflater.from(v.getContext());

                Chip chip = (Chip)inflater.inflate(R.layout.integration_chip, chipGroup, false);
                chip.setText(type.name());
                chip.setId(type.ordinal());
                chipGroup.addView(chip);
            }
        });
    }

    public static void addIntegrationChips(View v, Room room, ChipGroup chipGroup)
    {

        RoomManager.getInstance().getRoomIntegrationTypes(room, (integrationTypes, status) -> {
            for(IntegrationType type : integrationTypes)
            {
                LayoutInflater inflater = LayoutInflater.from(v.getContext());

                Chip chip = (Chip)inflater.inflate(R.layout.integration_chip, chipGroup, false);
                chip.setText(type.name());
                chip.setId(type.ordinal());
                chipGroup.addView(chip);
            }
        });
    }

    public static void updateUIChips(LightItemAdapter adapter)
    {
        new Handler(Looper.getMainLooper()).post(() -> adapter.notifyDataSetChanged());
    }
}
