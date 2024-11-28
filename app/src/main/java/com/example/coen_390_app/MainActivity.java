package com.example.coen_390_app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.AlertDialog;
import android.content.Intent;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "RoomPreferences";
    private static final String KEY_ROOMS = "rooms";

    private List<Button> roomButtons = new ArrayList<>();
    private LinearLayout layout;
    private SharedPreferenceHelper sharedPreferenceHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPreferenceHelper = new SharedPreferenceHelper(this);

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize layout for dynamic room buttons
        layout = findViewById(R.id.dynamic_room_buttons_container);

        // Initialize fixed buttons
        initializeFixedButtons();

        // Load saved rooms
        loadSavedRooms();

        // Adjust padding for Edge-to-Edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize the Add Room button
        Button addRoomButton = findViewById(R.id.AddRoomButton);
        addRoomButton.setOnClickListener(v -> {
            AddRoomFragment dialog = new AddRoomFragment();
            dialog.setRoomCreationListener(this::addButton); // Pass the room creation listener
            dialog.show(getSupportFragmentManager(), "fragment_add_room_fragment");
        });

        // Initialize the Delete Room button
        Button deleteRoomButton = findViewById(R.id.DeleteRoomButton);
        deleteRoomButton.setOnClickListener(v -> showDeleteRoomDialog());
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Reload rooms when the activity resumes to reflect any changes
        reloadRooms();
    }

    // Method to reload rooms and refresh the UI
    private void reloadRooms() {
        // Remove all existing room buttons from the layout
        layout.removeAllViews();
        roomButtons.clear();

        // Reload saved rooms and recreate buttons
        loadSavedRooms();
    }

    // Method to initialize fixed "Settings" and "Data" buttons
    private void initializeFixedButtons() {
        Button settingsButton = findViewById(R.id.buttonSettings);
        Button dataButton = findViewById(R.id.buttonData);

        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingActivity.class);
            startActivity(intent);
        });

        dataButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DataLogActivity.class);
            startActivity(intent);
        });
    }

    // Method to add a new room button with location context
    public void addButton(String name, String locationContext) {
        Button newRoomButton = new Button(this);

        // Set the button text to include the room name and location context
        String buttonText = name + " (" + locationContext + ")";
        newRoomButton.setText(buttonText);

        layout.addView(newRoomButton);
        roomButtons.add(newRoomButton); // Add button to list

        newRoomButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RoomActivity.class);
            intent.putExtra("ROOM_NAME", name);  // Pass room name to RoomActivity
            intent.putExtra("LOCATION_CONTEXT", locationContext); // Pass location context to RoomActivity
            startActivity(intent);
        });

        // Save room with its location context to SharedPreferences
        saveRoom(name, locationContext);
    }


    // Save room name and location context to SharedPreferences
    private void saveRoom(String name, String locationContext) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Set<String> roomSet = prefs.getStringSet(KEY_ROOMS, new HashSet<>());
        roomSet = new HashSet<>(roomSet); // Copy to modify

        // Save as a JSON string to include location context
        String roomEntry = "{\"name\":\"" + name + "\",\"location\":\"" + locationContext + "\"}";
        roomSet.add(roomEntry);
        prefs.edit().putStringSet(KEY_ROOMS, roomSet).apply();
    }


    // Load saved rooms and recreate buttons
    private void loadSavedRooms() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Set<String> roomSet = prefs.getStringSet(KEY_ROOMS, new HashSet<>());

        for (String roomEntry : roomSet) {
            try {
                // Parse the JSON string
                JSONObject json = new JSONObject(roomEntry);
                String roomName = json.getString("name");
                String locationContext = json.getString("location");

                // Recreate button for each saved room
                addButton(roomName, locationContext);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void showDeleteRoomDialog() {
        if (roomButtons.isEmpty()) {
            Toast.makeText(this, "No rooms to delete", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] roomNames = new String[roomButtons.size()];
        for (int i = 0; i < roomButtons.size(); i++) {
            roomNames[i] = roomButtons.get(i).getText().toString();
        }

        new AlertDialog.Builder(this)
                .setTitle("Select Room to Delete")
                .setItems(roomNames, (dialog, which) -> {
                    Button roomButtonToDelete = roomButtons.get(which);
                    layout.removeView(roomButtonToDelete);
                    roomButtons.remove(which);

                    // Remove room from SharedPreferences
                    removeRoom(roomNames[which]);

                    Toast.makeText(MainActivity.this, "Room deleted", Toast.LENGTH_SHORT).show();
                })
                .create()
                .show();
    }

    // Remove a room from SharedPreferences
    private void removeRoom(String roomNameWithLocation) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Set<String> roomSet = prefs.getStringSet(KEY_ROOMS, new HashSet<>());
        roomSet = new HashSet<>(roomSet); // Copy to modify

        String roomName = roomNameWithLocation.split(" \\(")[0]; // Extract room name from "RoomName (Location)"
        String roomToRemove = null;

        for (String roomEntry : roomSet) {
            try {
                JSONObject json = new JSONObject(roomEntry);
                if (json.getString("name").equals(roomName)) {
                    roomToRemove = roomEntry;
                    break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (roomToRemove != null) {
            roomSet.remove(roomToRemove);
        }

        prefs.edit().putStringSet(KEY_ROOMS, roomSet).apply();
    }
}
