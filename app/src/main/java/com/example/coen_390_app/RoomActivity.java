package com.example.coen_390_app;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.button.MaterialButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import retrofit2.Call;

public class RoomActivity extends AppCompatActivity implements OnSettingsChangedListener {

    private static final String TAG = "RoomActivity";
    private static final long REQUEST_INTERVAL_MS = 20000; // 20 seconds (3 requests per minute)
    private long lastRequestTime = 0;

    private int udpPort;
    private String ipAddress;

    private static final int BUFFER_SIZE = 1024;

    private Toolbar toolbar;
    private String roomName;
    private String locationContext;

    private TextView temperatureTextView;
    private TextView humidityTextView;
    private TextView co2TextView;
    private TextView vocTextView;
    private TextView coTextView;
    private TextView alcoholTextView;
    private TextView recommendationTextView;
    private TextView tolueneTextView;
    private TextView nh4TextView;
    private TextView acetoneTextView;
    private TextView propaneTextView;
    private TextView h2Mq2TextView;

    private SharedPreferenceHelper sharedPreferenceHelper;
    private SharedPreferenceHelper sharedPreferenceHelperGlobal;

    private Thread udpListenerThread;
    private boolean isListening = true;

    private double lastTemperatureValue;
    private double lastHumidityValue;
    private double lastCO2Value;
    private double lastVOCValue;
    private double lastCOValue;
    private double lastAlcoholValue;
    private double lastTolueneValue;
    private double lastNh4Value;
    private double lastAcetoneValue;
    private double lastPropaneValue;
    private double lastH2Mq2Value;

    private static final String PREFS_NAME = "RoomSettings";

    private static final String KEY_TEMPERATURE = "temperature_";
    private static final String KEY_HUMIDITY = "humidity_";
    private static final String KEY_CO2 = "co2_";
    private static final String KEY_VOC = "voc_";
    private static final String KEY_CO = "co_";
    private static final String KEY_ALCOHOL = "alcohol_";
    private static final String KEY_LOCATION = "location_";
    private static final String KEY_TOLUENE = "toluene_";
    private static final String KEY_NH4 = "nh4_";
    private static final String KEY_ACETONE = "acetone_";
    private static final String KEY_PROPANE = "propane_";
    private static final String KEY_H2MQ2 = "h2mq2_";

    // Added constants for SharedPreferences used for room names
    private static final String ROOM_PREFS_NAME = "RoomPreferences";
    private static final String KEY_ROOMS = "rooms";

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve roomName from intent
        roomName = getIntent().getStringExtra("ROOM_NAME");

        // Initialize SharedPreferenceHelper with room-specific settings
        sharedPreferenceHelper = new SharedPreferenceHelper(this, roomName);
        sharedPreferenceHelperGlobal = new SharedPreferenceHelper(this);

        setContentView(R.layout.activity_room);

        registerReceiver(unitChangedReceiver, new IntentFilter("com.example.coen_390_app.UNIT_CHANGED"), Context.RECEIVER_NOT_EXPORTED);

        Log.d(TAG, "BroadcastReceiver registered.");

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (roomName != null) {
            getSupportActionBar().setTitle(roomName);
        }

        temperatureTextView = findViewById(R.id.sensor_data_placeholder1);
        humidityTextView = findViewById(R.id.sensor_data_placeholder2);
        co2TextView = findViewById(R.id.sensor_data_placeholder3);
        vocTextView = findViewById(R.id.sensor_data_placeholder4);
        coTextView = findViewById(R.id.sensor_data_placeholder5);
        alcoholTextView = findViewById(R.id.sensor_data_placeholder6);
        nh4TextView = findViewById(R.id.sensor_data_placeholder8);
        acetoneTextView = findViewById(R.id.sensor_data_placeholder9);
        propaneTextView = findViewById(R.id.sensor_data_placeholder10);
        h2Mq2TextView = findViewById(R.id.sensor_data_placeholder11);
        tolueneTextView = findViewById(R.id.sensor_data_placeholder7);

        recommendationTextView = findViewById(R.id.recommendationTextView);

        // Load network settings using SharedPreferenceHelper
        udpPort = sharedPreferenceHelper.getUdpPort();
        ipAddress = sharedPreferenceHelper.getIpAddress();

        // Initialize buttons
        MaterialButton networkSettingsButton = findViewById(R.id.network_settings_button);
        MaterialButton renameRoomButton = findViewById(R.id.rename_room_button);

        // Set onClickListener for Network Settings button
        networkSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNetworkSettingsDialog();
            }
        });

        // Set onClickListener for Rename Room button
        renameRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRenameRoomDialog();
            }
        });

        loadRoomSettings();
        startListeningForSensorData();
    }

    @Override
    public void onSettingsChanged() {
        // Reload room settings and update the displayed units
        loadRoomSettings();
        updateSensorDisplays();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister the broadcast receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(unitChangedReceiver);
        isListening = false;
        if (udpListenerThread != null && udpListenerThread.isAlive()) {
            udpListenerThread.interrupt();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            openSettingsFragment();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openSettingsFragment() {
        DialogFragment settingsFragment = new SettingFragment(roomName);
        settingsFragment.show(getSupportFragmentManager(), "Setting_fragment");
    }

    private void showRenameRoomDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rename Room and Update Location");

        // Create a vertical layout to hold both EditText fields
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);

        // EditText for room name
        final EditText roomNameInput = new EditText(this);
        roomNameInput.setInputType(InputType.TYPE_CLASS_TEXT);
        roomNameInput.setHint("Enter new room name");
        roomNameInput.setText(roomName); // Pre-fill with the current room name
        layout.addView(roomNameInput);

        // EditText for location context
        final EditText locationInput = new EditText(this);
        locationInput.setInputType(InputType.TYPE_CLASS_TEXT);
        locationInput.setHint("Enter location context");

        // Fetch existing location context from SharedPreferences
        String currentLocation = "No location specified";
        SharedPreferences prefs = getSharedPreferences(ROOM_PREFS_NAME, MODE_PRIVATE);
        Set<String> roomSet = prefs.getStringSet(KEY_ROOMS, new HashSet<>());

        for (String roomEntry : roomSet) {
            try {
                JSONObject json = new JSONObject(roomEntry);
                if (json.getString("name").equals(roomName)) {
                    currentLocation = json.optString("location", "No location specified");
                    break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        locationInput.setText(currentLocation);
        layout.addView(locationInput);

        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newRoomName = roomNameInput.getText().toString().trim();
            String newLocationContext = locationInput.getText().toString().trim();

            if (!newRoomName.isEmpty()) {
                // Update the room name and location in the shared data source
                updateRoomNameInDataSource(roomName, newRoomName, newLocationContext);

                // Update the local roomName variable
                String oldRoomName = roomName;
                roomName = newRoomName;
                getSupportActionBar().setTitle(roomName);

                // Notify MainActivity about the change
                Intent resultIntent = new Intent();
                resultIntent.putExtra("OLD_ROOM_NAME", oldRoomName);
                resultIntent.putExtra("NEW_ROOM_NAME", newRoomName);
                setResult(RESULT_OK, resultIntent);

                // Update SharedPreferenceHelper with the new room name
                sharedPreferenceHelper = new SharedPreferenceHelper(RoomActivity.this, roomName);

                // Reload network settings with the new room name
                udpPort = sharedPreferenceHelper.getUdpPort();
                ipAddress = sharedPreferenceHelper.getIpAddress();

                // Restart UDP listener
                restartUdpListener();
            }
        });

        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateRoomNameInDataSource(String oldName, String newName, String newLocation) {
        SharedPreferences prefs = getSharedPreferences(ROOM_PREFS_NAME, MODE_PRIVATE);
        Set<String> roomSet = prefs.getStringSet(KEY_ROOMS, new HashSet<>());
        roomSet = new HashSet<>(roomSet); // Create a mutable copy

        for (String roomEntry : new HashSet<>(roomSet)) {
            try {
                // Parse the JSON string
                JSONObject json = new JSONObject(roomEntry);
                String roomName = json.getString("name");

                if (roomName.equals(oldName)) {
                    // Remove the old entry
                    roomSet.remove(roomEntry);

                    // Add the updated entry
                    JSONObject updatedRoom = new JSONObject();
                    updatedRoom.put("name", newName);
                    updatedRoom.put("location", newLocation);
                    roomSet.add(updatedRoom.toString());

                    // Save changes to SharedPreferences
                    prefs.edit().putStringSet(KEY_ROOMS, roomSet).apply();
                    break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadRoomSettings() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean showTemperature = preferences.getBoolean(KEY_TEMPERATURE + roomName, true);
        boolean showHumidity = preferences.getBoolean(KEY_HUMIDITY + roomName, true);
        boolean showCO2 = preferences.getBoolean(KEY_CO2 + roomName, true);
        boolean showVOC = preferences.getBoolean(KEY_VOC + roomName, true);
        boolean showCO = preferences.getBoolean(KEY_CO + roomName, true);
        boolean showAlcohol = preferences.getBoolean(KEY_ALCOHOL + roomName, true);
        boolean showToluene = preferences.getBoolean(KEY_TOLUENE + roomName, true);
        boolean showNh4 = preferences.getBoolean(KEY_NH4 + roomName, true);
        boolean showAcetone = preferences.getBoolean( KEY_ACETONE + roomName, true);
        boolean showPropane = preferences.getBoolean(KEY_PROPANE + roomName, true);
        boolean showH2Mq2 = preferences.getBoolean(KEY_H2MQ2 + roomName, true);

        // Show or hide the sensor data views based on the settings
        temperatureTextView.setVisibility(showTemperature ? View.VISIBLE : View.GONE);
        humidityTextView.setVisibility(showHumidity ? View.VISIBLE : View.GONE);
        co2TextView.setVisibility(showCO2 ? View.VISIBLE : View.GONE);
        vocTextView.setVisibility(showVOC ? View.VISIBLE : View.GONE);
        coTextView.setVisibility(showCO ? View.VISIBLE : View.GONE);
        alcoholTextView.setVisibility(showAlcohol ? View.VISIBLE : View.GONE);
        tolueneTextView.setVisibility(showToluene ? View.VISIBLE : View.GONE);
        nh4TextView.setVisibility(showNh4 ? View.VISIBLE : View.GONE);
        acetoneTextView.setVisibility(showAcetone ? View.VISIBLE : View.GONE);
        propaneTextView.setVisibility(showPropane ? View.VISIBLE : View.GONE);
        h2Mq2TextView.setVisibility(showH2Mq2 ? View.VISIBLE : View.GONE);
    }

    private void startListeningForSensorData() {
        isListening = true; // Ensure listening flag is true

        udpListenerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                DatagramSocket socket = null;
                try {
                    socket = new DatagramSocket(udpPort);

                    // Send initial packet if needed
                    byte[] buffer = "pop".getBytes();
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(ipAddress), udpPort);
                    socket.send(packet);

                    buffer = new byte[BUFFER_SIZE];
                    packet = new DatagramPacket(buffer, buffer.length);

                    while (isListening && !Thread.currentThread().isInterrupted()) {
                        socket.receive(packet);
                        String data = new String(packet.getData(), 0, packet.getLength());

                        Log.d(TAG, "Received UDP data: " + data);
                        Log.d(TAG, "Received UDP IP: " + packet.getAddress().getHostAddress());
                        // Parse and display sensor data
                        parseAndDisplaySensorData(data);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error in UDP listener", e);
                } finally {
                    if (socket != null && !socket.isClosed()) {
                        socket.close();
                    }
                }
            }
        });

        udpListenerThread.start();
    }

    private void parseAndDisplaySensorData(final String data) {
        try {
            if(!data.startsWith("{")){
                return;
            }
            JSONObject jsonData = new JSONObject(data);

            // Parse sensor values, including new sensors
            lastTemperatureValue = jsonData.getDouble("temperature");
            lastHumidityValue = jsonData.getDouble("humidity");
            lastCO2Value = jsonData.getDouble("co2");
            lastVOCValue = jsonData.getDouble("tvoc");
            lastCOValue = jsonData.getDouble("CO");
            lastAlcoholValue = jsonData.getDouble("Alcohol");
            lastNh4Value = jsonData.getDouble("NH4");
            lastAcetoneValue = jsonData.getDouble("Acetone");
            lastPropaneValue = jsonData.getDouble("Propane_MQ2");
            lastH2Mq2Value = jsonData.getDouble("H2_MQ2");
            lastTolueneValue = jsonData.getDouble("Toluene");

            // Retrieve the location context from SharedPreferences
            String locationContext = sharedPreferenceHelper.getString("location_" + roomName, "No location specified");

            // Update UI on the main thread
            runOnUiThread(() -> {
                updateSensorDisplays();

                // Generate a prompt for AI recommendations
                @SuppressLint("DefaultLocale") String prompt = String.format(
                        "Provide recommendations based on the following sensor data in a %s:\n" +
                                "Temperature: %.1f°C, Humidity: %.1f%%, CO₂: %.1f ppm, VOC: %.1f ppm, CO: %.1f ppm, Alcohol: %.1f ppm, NH₄: %.1f ppm, Acetone: %.1f ppm, Propane: %.1f ppm, H₂ MQ2: %.1f ppm, Toluene: %.1f ppm.",
                        locationContext, lastTemperatureValue, lastHumidityValue, lastCO2Value, lastVOCValue,
                        lastCOValue, lastAlcoholValue, lastNh4Value, lastAcetoneValue, lastPropaneValue, lastH2Mq2Value, lastTolueneValue
                );

                // Fetch AI recommendations
                fetchAIRecommendationsWithThresholdCheck(locationContext);
            });
        } catch (JSONException e) {
            Log.e(TAG, "Failed to parse sensor data", e);
        }
    }

    private void fetchAIRecommendationsWithThresholdCheck(String locationContext) {
        // Ensure rate limiting (3 requests per minute)
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastRequestTime < REQUEST_INTERVAL_MS) {
            Log.d(TAG, "Request blocked to maintain rate limit.");
            return;
        }
        lastRequestTime = currentTime;

        // Build threshold-based recommendations
        StringBuilder recommendations = new StringBuilder();
        int textColor = ContextCompat.getColor(this, android.R.color.holo_green_dark); // Default color

        // Threshold checks for each sensor
        if (lastCO2Value > sharedPreferenceHelper.getCustomThreshold("CO2")) {
            recommendations.append("CO₂ exceeds safe levels. Ventilate the room.\n");
            textColor = ContextCompat.getColor(this, android.R.color.holo_red_dark);
        }
        if (lastVOCValue > sharedPreferenceHelper.getCustomThreshold("VOC")) {
            recommendations.append("Avoid aerosols. VOC levels are too high.\n");
            textColor = ContextCompat.getColor(this, android.R.color.holo_red_dark);
        }
        if (lastHumidityValue > sharedPreferenceHelper.getCustomThreshold("Humidity")) {
            recommendations.append("High humidity detected. Use a dehumidifier.\n");
            textColor = ContextCompat.getColor(this, android.R.color.holo_orange_dark);
        }
        if (lastTemperatureValue > sharedPreferenceHelper.getCustomThreshold("Temperature")) {
            recommendations.append("Room is too hot. Consider cooling it.\n");
            textColor = ContextCompat.getColor(this, android.R.color.holo_orange_dark);
        }
        if (lastCOValue > sharedPreferenceHelper.getCustomThreshold("CO")) {
            recommendations.append("Carbon monoxide detected! Ensure proper ventilation.\n");
            textColor = ContextCompat.getColor(this, android.R.color.holo_orange_dark);
        }
        if (lastAlcoholValue > sharedPreferenceHelper.getCustomThreshold("Alcohol")) {
            recommendations.append("Alcohol levels are unusually high. Investigate the source.\n");
            textColor = ContextCompat.getColor(this, android.R.color.holo_orange_dark);
        }
        if (lastNh4Value > sharedPreferenceHelper.getCustomThreshold("NH4")) {
            recommendations.append("Ammonia (NH₄) levels are high. Check for leaks or contaminants.\n");
            textColor = ContextCompat.getColor(this, android.R.color.holo_red_dark);
        }
        if (lastAcetoneValue > sharedPreferenceHelper.getCustomThreshold("Acetone")) {
            recommendations.append("High acetone detected. Investigate possible chemical sources.\n");
            textColor = ContextCompat.getColor(this, android.R.color.holo_red_dark);
        }
        if (lastPropaneValue > sharedPreferenceHelper.getCustomThreshold("Propane")) {
            recommendations.append("Propane detected! Check for potential gas leaks.\n");
            textColor = ContextCompat.getColor(this, android.R.color.holo_red_dark);
        }
        if (lastH2Mq2Value > sharedPreferenceHelper.getCustomThreshold("H2_MQ2")) {
            recommendations.append("Hydrogen detected. Investigate potential leaks or sources.\n");
            textColor = ContextCompat.getColor(this, android.R.color.holo_red_dark);
        }
        if (lastTolueneValue > sharedPreferenceHelper.getCustomThreshold("Toluene")) {
            recommendations.append("Toluene detected. Investigate potential leaks or sources.\n");
            textColor = ContextCompat.getColor(this, android.R.color.holo_red_dark);
        }

        // Add threshold recommendations
        String thresholdRecommendations = recommendations.toString().trim();

        // Generate prompt for AI
        @SuppressLint("DefaultLocale") String prompt = String.format(
                "In the context of a %s, based on these air quality readings:\n" +
                        "Temperature: %.1f°C, Humidity: %.1f%%, CO₂: %.1f ppm, VOC: %.1f ppm, CO: %.1f ppm, Alcohol: %.1f ppm, NH₄: %.1f ppm, Acetone: %.1f ppm, Propane: %.1f ppm, H₂ MQ2: %.1f ppm, Toluene: %.1f ppm.\n" +
                        "Current recommendations: %s\n" +
                        "Provide additional guidance to improve air quality.",
                locationContext, lastTemperatureValue, lastHumidityValue, lastCO2Value, lastVOCValue,
                lastCOValue, lastAlcoholValue, lastNh4Value, lastAcetoneValue, lastPropaneValue, lastH2Mq2Value, lastTolueneValue,
                thresholdRecommendations.isEmpty() ? "None so far" : thresholdRecommendations
        );

        // Call OpenAI API for AI-based recommendations
        AIService aiService = RetrofitClient.getInstance().create(AIService.class);
        List<AIRequest.Message> messages = new ArrayList<>();
        messages.add(new AIRequest.Message("system", "You are an assistant that provides recommendations based on air quality data."));
        messages.add(new AIRequest.Message("user", prompt));

        AIRequest request = new AIRequest("gpt-4o-mini", messages, 150, 0.7);

        int finalTextColor = textColor;
        aiService.getResponse(request).enqueue(new retrofit2.Callback<AIResponse>() {
            @Override
            public void onResponse(Call<AIResponse> call, retrofit2.Response<AIResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String aiOutput = response.body().getChoices().get(0).getMessage().getContent().trim();

                    // Combine threshold recommendations and AI recommendations
                    final String combinedRecommendations = thresholdRecommendations.isEmpty()
                            ? aiOutput
                            : thresholdRecommendations + "\n\nAI Recommendations:\n" + aiOutput;

                    runOnUiThread(() -> {
                        recommendationTextView.setText(combinedRecommendations);
                        recommendationTextView.setTextColor(finalTextColor);
                        recommendationTextView.setVisibility(View.VISIBLE);
                    });
                } else {
                    try {
                        if (response.errorBody() != null) {
                            String errorDetails = response.errorBody().string();
                            Log.e(TAG, "API Response Error: " + errorDetails);
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                    Log.e(TAG, "HTTP Status Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<AIResponse> call, Throwable t) {
                Log.e(TAG, "API Call Failed: " + t.getMessage());
            }
        });
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void updateSensorDisplays() {
        // Get units from settings
        String tempUnit = sharedPreferenceHelperGlobal.getTemperatureUnit();

        // Update temperature display
        double displayedTempValue = "Fahrenheit".equals(tempUnit)
                ? celsiusToFahrenheit(lastTemperatureValue)
                : lastTemperatureValue;
        String tempUnitSymbol = "Fahrenheit".equals(tempUnit) ? "°F" : "°C";
        temperatureTextView.setText(String.format("Temperature: %.1f%s", displayedTempValue, tempUnitSymbol));
        humidityTextView.setText(String.format("Humidity: %.1f%%", lastHumidityValue));
        co2TextView.setText(String.format("CO₂ Level: %.1f ppm", lastCO2Value));
        vocTextView.setText(String.format("VOC Level: %.1f ppm", lastVOCValue));
        coTextView.setText(String.format("CO Level: %.1f ppm", lastCOValue));
        alcoholTextView.setText(String.format("Alcohol Level: %.1f ppm", lastAlcoholValue));
        tolueneTextView.setText(String.format("Toluene Level: %.1f ppm", lastTolueneValue));
        nh4TextView.setText(String.format("NH4 Level: %.1f ppm", lastNh4Value));
        acetoneTextView.setText(String.format("Acetone Level: %.1f ppm", lastAcetoneValue));
        propaneTextView.setText(String.format("Propane Level: %.1f ppm", lastPropaneValue));
        h2Mq2TextView.setText(String.format("H2 (MQ2) Level: %.1f ppm", lastH2Mq2Value));
    }

    private double celsiusToFahrenheit(double celsius) {
        return (celsius * 9 / 5) + 32;
    }

    // Broadcast receiver to listen for unit changes
    private final BroadcastReceiver unitChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Unit change broadcast received.");
            updateSensorDisplays(); // Update all sensor displays when settings change
        }
    };

    private void showNetworkSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Network Settings");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_network_settings, null);

        EditText udpPortInput = dialogView.findViewById(R.id.udp_port_input);
        EditText ipAddressInput = dialogView.findViewById(R.id.ip_address_input);

        // Pre-fill with current settings from SharedPreferenceHelper
        udpPortInput.setText(String.valueOf(sharedPreferenceHelper.getUdpPort()));
        ipAddressInput.setText(sharedPreferenceHelper.getIpAddress());

        builder.setView(dialogView);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                try {
                    int newUdpPort = Integer.parseInt(udpPortInput.getText().toString());
                    String newIpAddress = ipAddressInput.getText().toString();

                    // Save new settings using SharedPreferenceHelper
                    sharedPreferenceHelper.setUdpPort(newUdpPort);
                    sharedPreferenceHelper.setIpAddress(newIpAddress);

                    // Update local variables
                    udpPort = newUdpPort;
                    ipAddress = newIpAddress;

                    // Restart the UDP listener with new settings
                    restartUdpListener();
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Invalid input in network settings", e);
                    // Show an error message or toast to the user
                }
            }
        });

        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void restartUdpListener() {
        // Stop the current listener
        isListening = false;
        if (udpListenerThread != null && !udpListenerThread.isInterrupted()) {
            udpListenerThread.interrupt();
        }

        // Start a new listener with updated settings
        startListeningForSensorData();
    }
}
