package com.example.coen_390_app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SettingActivity extends AppCompatActivity {

    private TextView temperatureUnitTextView;
    private RadioGroup temperatureUnitRadioGroup;
    private RadioButton celsiusRadioButton, fahrenheitRadioButton;
    private SharedPreferenceHelper sharedPreferenceHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPreferenceHelper = new SharedPreferenceHelper(this);

        // Set the default theme based on the shared preferences
        if (sharedPreferenceHelper.isDarkThemeEnabled()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // Initialize the theme switch
        SwitchCompat themeSwitch = findViewById(R.id.themeSwitch);
        themeSwitch.setChecked(sharedPreferenceHelper.isDarkThemeEnabled());
        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferenceHelper.setDarkThemeEnabled(isChecked);
            AppCompatDelegate.setDefaultNightMode(isChecked
                    ? AppCompatDelegate.MODE_NIGHT_YES
                    : AppCompatDelegate.MODE_NIGHT_NO);
            recreate(); // Restart activity to apply the theme change
        });

        // Enable edge-to-edge adjustments
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.SettingPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initializeSettingsUI();
        updateText();
    }

    // Initialize UI elements and set listeners for each setting component
    private void initializeSettingsUI() {
        temperatureUnitTextView = findViewById(R.id.temperatureUnitTextView);
        temperatureUnitRadioGroup = findViewById(R.id.temperatureUnitRadioGroup);
        celsiusRadioButton = findViewById(R.id.celsiusRadioButton);
        fahrenheitRadioButton = findViewById(R.id.fahrenheitRadioButton);

        // Initial temperature unit selection
        setTemperatureUnitSelection();
        setupListeners();
    }

    private void setTemperatureUnitSelection() {
        String tempUnit = sharedPreferenceHelper.getTemperatureUnit();
        if ("Celsius".equals(tempUnit)) {
            celsiusRadioButton.setChecked(true);
        } else {
            fahrenheitRadioButton.setChecked(true);
        }
    }

    private void setupListeners() {
        // Listen for changes in temperature unit selection
        temperatureUnitRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String selectedUnit = (checkedId == R.id.celsiusRadioButton) ? "Celsius" : "Fahrenheit";
            sharedPreferenceHelper.setTemperatureUnit(selectedUnit);
            updateTemperatureUnitDisplay();

            // Broadcast unit change to all RoomActivity instances
            Intent intent = new Intent("com.example.coen_390_app.UNIT_CHANGED");
            Log.d("SettingActivity", "Broadcasting unit change: " + selectedUnit);
            sendBroadcast(intent);
        });
    }

    // UI text update methods
    private void updateText() {
        updateTemperatureUnitDisplay();
    }

    @SuppressLint("SetTextI18n")
    private void updateTemperatureUnitDisplay() {
        String unit = sharedPreferenceHelper.getTemperatureUnit();
        temperatureUnitTextView.setText("Temperature Unit: " + unit);
    }
}

