package com.example.coen_390_app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

public class SettingFragment extends DialogFragment {

    private static final String PREFS_NAME = "RoomSettings";

    // Keys for sensors
    private static final String KEY_TEMPERATURE = "temperature_";
    private static final String KEY_HUMIDITY = "humidity_";
    private static final String KEY_CO2 = "co2_";
    private static final String KEY_VOC = "voc_";
    private static final String KEY_CO = "co_";
    private static final String KEY_ALCOHOL = "alcohol_";
    private static final String KEY_NH4 = "nh4_";
    private static final String KEY_ACETONE = "acetone_";
    private static final String KEY_PROPANE = "propane_";
    private static final String KEY_H2MQ2 = "h2mq2_";
    private static final String KEY_TOLUENE = "toluene_";


    // Keys for thresholds
    private static final String KEY_CO2_THRESHOLD = "co2_threshold_";
    private static final String KEY_VOC_THRESHOLD = "voc_threshold_";
    private static final String KEY_CO_THRESHOLD = "co_threshold_";
    private static final String KEY_ALCOHOL_THRESHOLD = "alcohol_threshold_";
    private static final String KEY_NH4_THRESHOLD = "nh4_threshold_";
    private static final String KEY_ACETONE_THRESHOLD = "acetone_threshold_";
    private static final String KEY_PROPANE_THRESHOLD = "propane_threshold_";
    private static final String KEY_H2MQ2_THRESHOLD = "h2mq2_threshold_";
    private static final String KEY_TOLUENE_THRESHOLD = "toluene_threshold_";

    // UI components
    private TextView dialogTitle;
    private TextView co2ThresholdValue, vocThresholdValue, coThresholdValue, alcoholThresholdValue, nh4ThresholdValue, acetoneThresholdValue, propaneThresholdValue, h2mq2ThresholdValue, tolueneThresholdValue;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch switchTemperature, switchHumidity, switchCO2, switchVOC, switchCO, switchAlcohol, switchNH4, switchAcetone, switchPropane, switchH2MQ2, switchToluene;
    private SeekBar co2ThresholdSeekBar, vocThresholdSeekBar, coThresholdSeekBar, alcoholThresholdSeekBar, nh4ThresholdSeekBar, acetoneThresholdSeekBar, propaneThresholdSeekBar, h2mq2ThresholdSeekBar, tolueneThresholdSeekBar;

    private String roomId;
    private OnSettingsChangedListener settingsChangedListener;

    public SettingFragment(String roomId) {
        this.roomId = roomId;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSettingsChangedListener) {
            settingsChangedListener = (OnSettingsChangedListener) context;
        } else {
            throw new RuntimeException(context + " must implement OnSettingsChangedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        settingsChangedListener = null;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (settingsChangedListener != null) {
            settingsChangedListener.onSettingsChanged();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting_, container, false);
        initializeViews(view);
        loadRoomSettings();
        setupListeners();
        return view;
    }

    @SuppressLint("SetTextI18n")
    private void initializeViews(View view) {
        dialogTitle = view.findViewById(R.id.dialog_title);
        dialogTitle.setText("Sensor Settings");

        switchTemperature = view.findViewById(R.id.switch_temperature);
        switchHumidity = view.findViewById(R.id.switch_humidity);
        switchCO2 = view.findViewById(R.id.switch_co2);
        switchVOC = view.findViewById(R.id.switch_voc);
        switchCO = view.findViewById(R.id.switch_co);
        switchAlcohol = view.findViewById(R.id.switch_alcohol);
        switchNH4 = view.findViewById(R.id.switch_nh4);
        switchAcetone = view.findViewById(R.id.switch_acetone);
        switchPropane = view.findViewById(R.id.switch_propane);
        switchH2MQ2 = view.findViewById(R.id.switch_h2_mq2);
        switchToluene = view.findViewById(R.id.switch_toluene);

        co2ThresholdValue = view.findViewById(R.id.co2ThresholdValue);
        vocThresholdValue = view.findViewById(R.id.vocThresholdValue);
        coThresholdValue = view.findViewById(R.id.coThresholdValue);
        alcoholThresholdValue = view.findViewById(R.id.alcoholThresholdValue);
        nh4ThresholdValue = view.findViewById(R.id.nh4ThresholdValue);
        acetoneThresholdValue = view.findViewById(R.id.acetoneThresholdValue);
        propaneThresholdValue = view.findViewById(R.id.propaneThresholdValue);
        h2mq2ThresholdValue = view.findViewById(R.id.h2_mq2ThresholdValue);
        tolueneThresholdValue = view.findViewById(R.id.tolueneThresholdValue);

        co2ThresholdSeekBar = view.findViewById(R.id.co2ThresholdSeekBar);
        vocThresholdSeekBar = view.findViewById(R.id.vocThresholdSeekBar);
        coThresholdSeekBar = view.findViewById(R.id.coThresholdSeekBar);
        alcoholThresholdSeekBar = view.findViewById(R.id.alcoholThresholdSeekBar);
        nh4ThresholdSeekBar = view.findViewById(R.id.nh4ThresholdSeekBar);
        acetoneThresholdSeekBar = view.findViewById(R.id.acetoneThresholdSeekBar);
        propaneThresholdSeekBar = view.findViewById(R.id.propaneThresholdSeekBar);
        h2mq2ThresholdSeekBar = view.findViewById(R.id.h2_mq2ThresholdSeekBar);
        tolueneThresholdSeekBar = view.findViewById(R.id.tolueneThresholdSeekBar);
    }

    private void loadRoomSettings() {
        SharedPreferences preferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        switchTemperature.setChecked(preferences.getBoolean(KEY_TEMPERATURE + roomId, true));
        switchHumidity.setChecked(preferences.getBoolean(KEY_HUMIDITY + roomId, true));
        switchCO2.setChecked(preferences.getBoolean(KEY_CO2 + roomId, true));
        switchVOC.setChecked(preferences.getBoolean(KEY_VOC + roomId, true));
        switchCO.setChecked(preferences.getBoolean(KEY_CO + roomId, true));
        switchAlcohol.setChecked(preferences.getBoolean(KEY_ALCOHOL + roomId, true));
        switchNH4.setChecked(preferences.getBoolean(KEY_NH4 + roomId, true));
        switchAcetone.setChecked(preferences.getBoolean(KEY_ACETONE + roomId, true));
        switchPropane.setChecked(preferences.getBoolean(KEY_PROPANE + roomId, true));
        switchH2MQ2.setChecked(preferences.getBoolean(KEY_H2MQ2 + roomId, true));
        switchToluene.setChecked(preferences.getBoolean(KEY_TOLUENE + roomId, true));

        co2ThresholdSeekBar.setProgress(preferences.getInt(KEY_CO2_THRESHOLD + roomId, 1000));
        vocThresholdSeekBar.setProgress(preferences.getInt(KEY_VOC_THRESHOLD + roomId, 400));
        coThresholdSeekBar.setProgress(preferences.getInt(KEY_CO_THRESHOLD + roomId, 50));
        alcoholThresholdSeekBar.setProgress(preferences.getInt(KEY_ALCOHOL_THRESHOLD + roomId, 50));
        nh4ThresholdSeekBar.setProgress(preferences.getInt(KEY_NH4_THRESHOLD + roomId, 5));
        acetoneThresholdSeekBar.setProgress(preferences.getInt(KEY_ACETONE_THRESHOLD + roomId, 10));
        propaneThresholdSeekBar.setProgress(preferences.getInt(KEY_PROPANE_THRESHOLD + roomId, 10));
        h2mq2ThresholdSeekBar.setProgress(preferences.getInt(KEY_H2MQ2_THRESHOLD + roomId, 5));
        tolueneThresholdSeekBar.setProgress(preferences.getInt(KEY_TOLUENE_THRESHOLD + roomId, 5));

        co2ThresholdValue.setText(String.valueOf(co2ThresholdSeekBar.getProgress()));
        vocThresholdValue.setText(String.valueOf(vocThresholdSeekBar.getProgress()));
        coThresholdValue.setText(String.valueOf(coThresholdSeekBar.getProgress()));
        alcoholThresholdValue.setText(String.valueOf(alcoholThresholdSeekBar.getProgress()));
        nh4ThresholdValue.setText(String.valueOf(nh4ThresholdSeekBar.getProgress()));
        acetoneThresholdValue.setText(String.valueOf(acetoneThresholdSeekBar.getProgress()));
        propaneThresholdValue.setText(String.valueOf(propaneThresholdSeekBar.getProgress()));
        h2mq2ThresholdValue.setText(String.valueOf(h2mq2ThresholdSeekBar.getProgress()));
        tolueneThresholdValue.setText(String.valueOf(tolueneThresholdSeekBar.getProgress()));
    }

    private void saveRoomSettings() {
        SharedPreferences preferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean(KEY_TEMPERATURE + roomId, switchTemperature.isChecked());
        editor.putBoolean(KEY_HUMIDITY + roomId, switchHumidity.isChecked());
        editor.putBoolean(KEY_CO2 + roomId, switchCO2.isChecked());
        editor.putBoolean(KEY_VOC + roomId, switchVOC.isChecked());
        editor.putBoolean(KEY_CO + roomId, switchCO.isChecked());
        editor.putBoolean(KEY_ALCOHOL + roomId, switchAlcohol.isChecked());
        editor.putBoolean(KEY_NH4 + roomId, switchNH4.isChecked());
        editor.putBoolean(KEY_ACETONE + roomId, switchAcetone.isChecked());
        editor.putBoolean(KEY_PROPANE + roomId, switchPropane.isChecked());
        editor.putBoolean(KEY_H2MQ2 + roomId, switchH2MQ2.isChecked());
        editor.putBoolean(KEY_TOLUENE + roomId, switchToluene.isChecked());

        editor.putInt(KEY_CO2_THRESHOLD + roomId, co2ThresholdSeekBar.getProgress());
        editor.putInt(KEY_VOC_THRESHOLD + roomId, vocThresholdSeekBar.getProgress());
        editor.putInt(KEY_CO_THRESHOLD + roomId, coThresholdSeekBar.getProgress());
        editor.putInt(KEY_ALCOHOL_THRESHOLD + roomId, alcoholThresholdSeekBar.getProgress());
        editor.putInt(KEY_NH4_THRESHOLD + roomId, nh4ThresholdSeekBar.getProgress());
        editor.putInt(KEY_ACETONE_THRESHOLD + roomId, acetoneThresholdSeekBar.getProgress());
        editor.putInt(KEY_PROPANE_THRESHOLD + roomId, propaneThresholdSeekBar.getProgress());
        editor.putInt(KEY_H2MQ2_THRESHOLD + roomId, h2mq2ThresholdSeekBar.getProgress());
        editor.putInt(KEY_TOLUENE_THRESHOLD + roomId, tolueneThresholdSeekBar.getProgress());

        editor.apply();
    }

    private void setupListeners() {
        switchTemperature.setOnCheckedChangeListener((buttonView, isChecked) -> notifySettingsChanged());
        switchHumidity.setOnCheckedChangeListener((buttonView, isChecked) -> notifySettingsChanged());
        switchCO2.setOnCheckedChangeListener((buttonView, isChecked) -> notifySettingsChanged());
        switchVOC.setOnCheckedChangeListener((buttonView, isChecked) -> notifySettingsChanged());
        switchCO.setOnCheckedChangeListener((buttonView, isChecked) -> notifySettingsChanged());
        switchAlcohol.setOnCheckedChangeListener((buttonView, isChecked) -> notifySettingsChanged());
        switchNH4.setOnCheckedChangeListener((buttonView, isChecked) -> notifySettingsChanged());
        switchAcetone.setOnCheckedChangeListener((buttonView, isChecked) -> notifySettingsChanged());
        switchPropane.setOnCheckedChangeListener((buttonView, isChecked) -> notifySettingsChanged());
        switchH2MQ2.setOnCheckedChangeListener((buttonView, isChecked) -> notifySettingsChanged());
        switchToluene.setOnCheckedChangeListener((buttonView, isChecked) -> notifySettingsChanged());

        co2ThresholdSeekBar.setOnSeekBarChangeListener(createThresholdChangeListener(co2ThresholdValue));
        vocThresholdSeekBar.setOnSeekBarChangeListener(createThresholdChangeListener(vocThresholdValue));
        coThresholdSeekBar.setOnSeekBarChangeListener(createThresholdChangeListener(coThresholdValue));
        alcoholThresholdSeekBar.setOnSeekBarChangeListener(createThresholdChangeListener(alcoholThresholdValue));
        nh4ThresholdSeekBar.setOnSeekBarChangeListener(createThresholdChangeListener(nh4ThresholdValue));
        acetoneThresholdSeekBar.setOnSeekBarChangeListener(createThresholdChangeListener(acetoneThresholdValue));
        propaneThresholdSeekBar.setOnSeekBarChangeListener(createThresholdChangeListener(propaneThresholdValue));
        h2mq2ThresholdSeekBar.setOnSeekBarChangeListener(createThresholdChangeListener(h2mq2ThresholdValue));
        tolueneThresholdSeekBar.setOnSeekBarChangeListener(createThresholdChangeListener(tolueneThresholdValue));
    }

    private SeekBar.OnSeekBarChangeListener createThresholdChangeListener(TextView textView) {
        return new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textView.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                notifySettingsChanged();
            }
        };
    }

    private void notifySettingsChanged() {
        saveRoomSettings();
        if (settingsChangedListener != null) {
            settingsChangedListener.onSettingsChanged();
        }
    }
}
