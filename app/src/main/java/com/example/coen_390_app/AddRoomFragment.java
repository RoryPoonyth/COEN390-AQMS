package com.example.coen_390_app;

import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Objects;

public class AddRoomFragment extends DialogFragment {

    private Button cancelButton, createButton;
    private EditText roomName, customLocation;
    private Spinner locationSpinner;
    private RoomCreationListener roomCreationListener;

    // Interface for creating a room
    public interface RoomCreationListener {
        void onRoomCreated(String roomName, String locationContext);
    }

    public AddRoomFragment() {
        // Required empty public constructor
    }

    // Method to set the listener from the activity
    public void setRoomCreationListener(RoomCreationListener listener) {
        this.roomCreationListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_room, container, false);

        cancelButton = view.findViewById(R.id.buttonCancel);
        createButton = view.findViewById(R.id.buttonCreateRoom);
        roomName = view.findViewById(R.id.editTextRoomName);
        customLocation = view.findViewById(R.id.editTextCustomLocation);
        locationSpinner = view.findViewById(R.id.spinnerLocation);

        setupLocationSpinner();
        setupListeners();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null) {
            Objects.requireNonNull(getDialog().getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void setupLocationSpinner() {
        // Common presets for location context
        String[] locations = {"Living Room", "Bedroom", "Kitchen", "Office", "Custom"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, locations);
        locationSpinner.setAdapter(adapter);

        // Listener to toggle custom location input visibility
        locationSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if ("Custom".equals(locations[position])) {
                    customLocation.setVisibility(View.VISIBLE);
                } else {
                    customLocation.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                customLocation.setVisibility(View.GONE);
            }
        });
    }

    private void setupListeners() {
        cancelButton.setOnClickListener(v -> dismiss());

        createButton.setOnClickListener(v -> {
            String roomNameText = roomName.getText().toString().trim();
            String locationContext;

            if (roomNameText.isEmpty()) {
                Toast.makeText(getContext(), "Please enter a room name", Toast.LENGTH_SHORT).show();
                return;
            }

            if ("Custom".equals(locationSpinner.getSelectedItem().toString())) {
                locationContext = customLocation.getText().toString().trim();
                if (locationContext.isEmpty()) {
                    Toast.makeText(getContext(), "Please enter a custom location", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                locationContext = locationSpinner.getSelectedItem().toString();
            }

            if (roomCreationListener != null) {
                roomCreationListener.onRoomCreated(roomNameText, locationContext); // Notify listener
            }
            dismiss();
        });
    }
}