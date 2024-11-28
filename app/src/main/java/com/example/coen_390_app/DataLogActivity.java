package com.example.coen_390_app;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import com.github.mikephil.charting.utils.ColorTemplate;

import android.util.Log;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ArrayAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DataLogActivity extends AppCompatActivity {

    LineChart lineChart;
    Spinner roomSpinner;

    private boolean isListening = true;
    private static final String TAG = "DataLogActivity";
    private static final int UDP_PORT = 12345; // Default UDP port
    private static final int BUFFER_SIZE = 1024;
    private Thread udpListenerThread;
    private DatagramSocket udpSocket;
    private int dataIndex = 0;

    // Data entries for each sensor
    private final List<Entry> temperatureEntries = new ArrayList<>();
    private final List<Entry> humidityEntries = new ArrayList<>();
    private final List<Entry> co2Entries = new ArrayList<>();
    private final List<Entry> tvocEntries = new ArrayList<>();
    private final List<Entry> coEntries = new ArrayList<>();
    private final List<Entry> alcoholEntries = new ArrayList<>();
    private final List<Entry> nh4Entries = new ArrayList<>();
    private final List<Entry> acetoneEntries = new ArrayList<>();
    private final List<Entry> propaneEntries = new ArrayList<>();
    private final List<Entry> h2mq2Entries = new ArrayList<>();
    private final List<Entry> tolueneEntries = new ArrayList<>();

    // Datasets for each sensor
    LineDataSet temperatureDataSet;
    LineDataSet humidityDataSet;
    LineDataSet co2DataSet;
    LineDataSet tvocDataSet;
    LineDataSet coDataSet;
    LineDataSet alcoholDataSet;
    LineDataSet nh4DataSet;
    LineDataSet acetoneDataSet;
    LineDataSet propaneDataSet;
    LineDataSet h2mq2DataSet;
    LineDataSet tolueneDataSet;

    private static final int REQUEST_WRITE_STORAGE = 112;
    Button exportButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_log);

        // Get the room name and settings from the intent
        String roomName = getIntent().getStringExtra("ROOM_NAME");
        int udpPort = getIntent().getIntExtra("UDP_PORT", 12345); // Default port
        String ipAddress = getIntent().getStringExtra("IP_ADDRESS");

        Log.d(TAG, "Using settings for room: " + roomName + " (Port: " + udpPort + ", IP: " + ipAddress + ")");

        lineChart = findViewById(R.id.lineChart);
        roomSpinner = findViewById(R.id.room_spinner); // Spinner to switch rooms

        populateRoomSpinner(roomName);

        Button switchRoomButton = findViewById(R.id.switch_room_button);
        switchRoomButton.setOnClickListener(v -> switchRoom());

        setupChart();
        setupSensorButtons();

        // Start listening using the specific port
        startListeningForSensorData(udpPort, ipAddress);

        // Initialize the export button and set its click listener
        exportButton = findViewById(R.id.SaveToCSV);
        exportButton.setOnClickListener(v -> exportChartDataToCSV());

        // Check and request storage permissions if needed
        checkStoragePermissions();
    }

    private void populateRoomSpinner(String currentRoomName) {
        SharedPreferences prefs = getSharedPreferences("RoomPreferences", MODE_PRIVATE);
        Set<String> roomSet = prefs.getStringSet("rooms", new HashSet<>());

        if (roomSet.isEmpty()) {
            Log.w(TAG, "No rooms available to populate the spinner.");
            return; // No rooms to populate
        }

        List<String> roomList = new ArrayList<>();
        for (String roomEntry : roomSet) {
            try {
                JSONObject json = new JSONObject(roomEntry);
                roomList.add(json.getString("name")); // Extract the room name
            } catch (JSONException e) {
                Log.e(TAG, "Failed to parse room entry: " + roomEntry, e);
            }
        }

        if (roomList.isEmpty()) {
            Log.w(TAG, "No valid room names found in SharedPreferences.");
            return;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roomList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roomSpinner.setAdapter(adapter);

        // Pre-select the current room in the spinner
        if (currentRoomName != null && roomList.contains(currentRoomName)) {
            int position = roomList.indexOf(currentRoomName);
            roomSpinner.setSelection(position);
        }
    }

    @Override
    protected void onDestroy() {
        isListening = false;
        if (udpListenerThread != null && udpListenerThread.isAlive()) {
            udpListenerThread.interrupt();
        }
        if (udpSocket != null && !udpSocket.isClosed()) {
            udpSocket.close();
        }
        super.onDestroy();
    }

    // Updated setupChart method with new sensors
    private void setupChart() {
        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setBackgroundColor(ContextCompat.getColor(this, R.color.backgroundColor));
        lineChart.setPinchZoom(true);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setTextColor(ContextCompat.getColor(this, R.color.textColor));

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setGranularity(1f);
        leftAxis.setLabelCount(6, true);
        leftAxis.setTextColor(ContextCompat.getColor(this, R.color.textColor));

        lineChart.getAxisRight().setEnabled(false);

        LineData data = new LineData();

        // Use modular indexing to cycle through colors
        int[] colors = ColorTemplate.COLORFUL_COLORS;

        temperatureDataSet = createSet("Temperature °C", colors[0 % colors.length]);
        humidityDataSet = createSet("Humidity %", colors[1 % colors.length]);
        co2DataSet = createSet("CO₂ ppm", colors[2 % colors.length]);
        tvocDataSet = createSet("TVOC ppm", colors[3 % colors.length]);
        coDataSet = createSet("CO ppm", colors[4 % colors.length]);
        alcoholDataSet = createSet("Alcohol ppm", colors[5 % colors.length]);
        nh4DataSet = createSet("NH₄ ppm", colors[6 % colors.length]);
        acetoneDataSet = createSet("Acetone ppm", colors[7 % colors.length]);
        propaneDataSet = createSet("Propane ppm", colors[8 % colors.length]);
        h2mq2DataSet = createSet("H₂ MQ2 ppm", colors[9 % colors.length]);
        tolueneDataSet = createSet("Toluene ppm", colors[10 % colors.length]);

        // Add all datasets to the chart
        data.addDataSet(temperatureDataSet);
        data.addDataSet(humidityDataSet);
        data.addDataSet(co2DataSet);
        data.addDataSet(tvocDataSet);
        data.addDataSet(coDataSet);
        data.addDataSet(alcoholDataSet);
        data.addDataSet(nh4DataSet);
        data.addDataSet(acetoneDataSet);
        data.addDataSet(propaneDataSet);
        data.addDataSet(h2mq2DataSet);
        data.addDataSet(tolueneDataSet);

        lineChart.setData(data);

        Legend legend = lineChart.getLegend();
        legend.setForm(Legend.LegendForm.LINE);
    }

    private LineDataSet createSet(String label, int color) {
        LineDataSet set = new LineDataSet(null, label);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(color);
        set.setLineWidth(2f);
        set.setDrawCircles(false);
        return set;
    }

    // Updated setupSensorButtons method with new sensor buttons
    private void setupSensorButtons() {
        findViewById(R.id.temp_button).setOnClickListener(v -> showOnlyDataSet(temperatureDataSet));
        findViewById(R.id.humidity_button).setOnClickListener(v -> showOnlyDataSet(humidityDataSet));
        findViewById(R.id.co2_button).setOnClickListener(v -> showOnlyDataSet(co2DataSet));
        findViewById(R.id.tvoc_button).setOnClickListener(v -> showOnlyDataSet(tvocDataSet));
        findViewById(R.id.co_button).setOnClickListener(v -> showOnlyDataSet(coDataSet));
        findViewById(R.id.alcohol_button).setOnClickListener(v -> showOnlyDataSet(alcoholDataSet));
        findViewById(R.id.nh4_button).setOnClickListener(v -> showOnlyDataSet(nh4DataSet));
        findViewById(R.id.acetone_button).setOnClickListener(v -> showOnlyDataSet(acetoneDataSet));
        findViewById(R.id.propane_button).setOnClickListener(v -> showOnlyDataSet(propaneDataSet));
        findViewById(R.id.h2mq2_button).setOnClickListener(v -> showOnlyDataSet(h2mq2DataSet));
        findViewById(R.id.toluene_button).setOnClickListener(v -> showOnlyDataSet(tolueneDataSet));
    }

    private void showOnlyDataSet(LineDataSet selectedDataSet) {
        LineData data = lineChart.getData();
        if (data != null) {
            for (int i = 0; i < data.getDataSetCount(); i++) {
                LineDataSet set = (LineDataSet) data.getDataSetByIndex(i);
                set.setVisible(set == selectedDataSet);
            }
            lineChart.invalidate();
        }
    }

    // Updated parseAndDisplaySensorData method with new sensors
    private void parseAndDisplaySensorData(String data) {
        try {
            if (!data.startsWith("{")) {
                return;
            }
            JSONObject jsonData = new JSONObject(data);

            // Ensure all expected keys are present in the JSON
            if (!jsonData.has("temperature") || !jsonData.has("humidity") ||
                    !jsonData.has("co2") || !jsonData.has("tvoc") || !jsonData.has("CO") ||
                    !jsonData.has("Alcohol") || !jsonData.has("NH4") ||
                    !jsonData.has("Acetone") || !jsonData.has("Propane_MQ2") ||
                    !jsonData.has("H2_MQ2") || !jsonData.has("Toluene")) {
                Log.e(TAG, "Missing keys in received data: " + data);
                return; // Skip processing if any key is missing
            }

            // Parse sensor data
            final float temperature = (float) jsonData.getDouble("temperature");
            final float humidity = (float) jsonData.getDouble("humidity");
            final float co2 = (float) jsonData.getDouble("co2");
            final float tvoc = (float) jsonData.getDouble("tvoc");
            final float co = (float) jsonData.getDouble("CO");
            final float alcohol = (float) jsonData.getDouble("Alcohol");
            final float nh4 = (float) jsonData.getDouble("NH4");
            final float acetone = (float) jsonData.getDouble("Acetone");
            final float propane = (float) jsonData.getDouble("Propane_MQ2");
            final float h2mq2 = (float) jsonData.getDouble("H2_MQ2");
            final float toluene = (float) jsonData.getDouble("Toluene");

            // Update UI on the main thread
            runOnUiThread(() -> {
                addEntry(temperature, temperatureDataSet);
                addEntry(humidity, humidityDataSet);
                addEntry(co2, co2DataSet);
                addEntry(tvoc, tvocDataSet);
                addEntry(co, coDataSet);
                addEntry(alcohol, alcoholDataSet);
                addEntry(nh4, nh4DataSet);
                addEntry(acetone, acetoneDataSet);
                addEntry(propane, propaneDataSet);
                addEntry(h2mq2, h2mq2DataSet);
                addEntry(toluene, tolueneDataSet);

                dataIndex++;
            });

        } catch (JSONException e) {
            Log.e(TAG, "Malformed JSON data: " + data, e);
        }
    }

    private void addEntry(float value, LineDataSet set) {
        LineData data = lineChart.getData();
        if (data != null && set != null) {
            try {
                // Add entry safely
                set.addEntry(new Entry(dataIndex, value));
                data.notifyDataChanged();
                lineChart.notifyDataSetChanged();
                lineChart.setVisibleXRangeMaximum(50);
                lineChart.moveViewToX(data.getXMax());
            } catch (IndexOutOfBoundsException e) {
                Log.e(TAG, "IndexOutOfBoundsException in addEntry for dataset: " + set.getLabel(), e);
            }
        } else {
            Log.e(TAG, "LineData or LineDataSet is null. Cannot add entry.");
        }
    }

    private void switchRoom() {
        String selectedRoom = roomSpinner.getSelectedItem().toString();

        // Retrieve room-specific settings
        SharedPreferenceHelper roomPreferences = new SharedPreferenceHelper(this, selectedRoom);
        int newUdpPort = roomPreferences.getUdpPort();
        String newIpAddress = roomPreferences.getIpAddress();

        Toast.makeText(this, "Switched to room: " + selectedRoom, Toast.LENGTH_SHORT).show();

        // Restart the activity with the new room's settings
        Intent intent = new Intent(this, DataLogActivity.class);
        intent.putExtra("ROOM_NAME", selectedRoom);
        intent.putExtra("UDP_PORT", newUdpPort);
        intent.putExtra("IP_ADDRESS", newIpAddress);
        startActivity(intent);
        finish();
    }

    private void startListeningForSensorData(int udpPort, String ipAddress) {
        udpListenerThread = new Thread(() -> {
            try (DatagramSocket socket = new DatagramSocket(udpPort)) {
                byte[] buffer = new byte[BUFFER_SIZE];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                while (isListening) {
                    socket.receive(packet);
                    String data = new String(packet.getData(), 0, packet.getLength());
                    Log.d(TAG, "Received data from IP: " + ipAddress);
                    parseAndDisplaySensorData(data);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error in UDP listener for room: " + ipAddress, e);
            }
        });
        udpListenerThread.start();
    }

    // Permission handling for storage access
    private void checkStoragePermissions() {
        boolean hasPermission = (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_STORAGE) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "Cannot write to storage without permission.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    // Method to export chart data to CSV
    private void exportChartDataToCSV() {
        StringBuilder csvData = new StringBuilder();
        csvData.append("Index,Temperature,Humidity,CO2,TVOC,CO,Alcohol,NH4,Acetone,Propane,H2MQ2,Toluene\n");

        int dataSize = temperatureDataSet.getEntryCount();

        for (int i = 0; i < dataSize; i++) {
            float temperature = getYValueFromDataSet(temperatureDataSet, i);
            float humidity = getYValueFromDataSet(humidityDataSet, i);
            float co2 = getYValueFromDataSet(co2DataSet, i);
            float tvoc = getYValueFromDataSet(tvocDataSet, i);
            float co = getYValueFromDataSet(coDataSet, i);
            float alcohol = getYValueFromDataSet(alcoholDataSet, i);
            float nh4 = getYValueFromDataSet(nh4DataSet, i);
            float acetone = getYValueFromDataSet(acetoneDataSet, i);
            float propane = getYValueFromDataSet(propaneDataSet, i);
            float h2mq2 = getYValueFromDataSet(h2mq2DataSet, i);
            float toluene = getYValueFromDataSet(tolueneDataSet, i);

            csvData.append(i).append(",")
                    .append(temperature).append(",")
                    .append(humidity).append(",")
                    .append(co2).append(",")
                    .append(tvoc).append(",")
                    .append(co).append(",")
                    .append(alcohol).append(",")
                    .append(nh4).append(",")
                    .append(acetone).append(",")
                    .append(propane).append(",")
                    .append(h2mq2).append(",")
                    .append(toluene).append("\n");
        }

        try {
            // Create a file in the external files directory
            String fileName = "chart_data.csv";
            File csvFile = new File(getExternalFilesDir(null), fileName);

            FileWriter writer = new FileWriter(csvFile);
            writer.write(csvData.toString());
            writer.close();

            // Notify the user of success
            Toast.makeText(this, "Data exported to " + csvFile.getAbsolutePath(),
                    Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error exporting data: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private float getYValueFromDataSet(LineDataSet dataSet, int index) {
        if (dataSet != null && index < dataSet.getEntryCount()) {
            Entry entry = dataSet.getEntryForIndex(index);
            if (entry != null) {
                return entry.getY();
            }
        }
        return Float.NaN; // Return NaN if data is not available
    }
}
