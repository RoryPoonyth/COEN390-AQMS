package com.example.coen_390_app;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class SharedPreferenceHelper {

    private final SharedPreferences sharedPreferences;
    private final String roomName;
    private final boolean isGlobal;

    // Preference keys
    private static final String KEY_TEMPERATURE_UNIT = "TemperatureUnit";
    private static final String KEY_DARK_THEME_ENABLED = "DarkThemeEnabled";
    private static final String KEY_NOTIFICATIONS_ENABLED = "NotificationsEnabled";
    private static final String KEY_THRESHOLD_PREFIX = "Threshold_";
    private static final String ROOM_PREFS_NAME = "RoomPreferences";
    private static final String KEY_ROOMS = "rooms";
    private static final String KEY_UDP_PORT = "UdpPort";
    private static final String KEY_IP_ADDRESS = "IpAddress";

    // Constructor for global settings
    public SharedPreferenceHelper(Context context) {
        this.isGlobal = true;
        this.roomName = null;
        this.sharedPreferences = context.getSharedPreferences("AppSettings_Global", Context.MODE_PRIVATE);
    }

    // Constructor for room-specific settings
    public SharedPreferenceHelper(Context context, String roomName) {
        this.isGlobal = false;
        this.roomName = roomName;
        this.sharedPreferences = context.getSharedPreferences("AppSettings_Rooms", Context.MODE_PRIVATE);
    }

    // Room management methods
    public Set<String> getRoomNames() {
        return sharedPreferences.getStringSet(KEY_ROOMS, new HashSet<>());
    }

    public void addRoom(String roomName) {
        Set<String> roomSet = new HashSet<>(getRoomNames());
        roomSet.add(roomName);
        sharedPreferences.edit().putStringSet(KEY_ROOMS, roomSet).apply();
    }

    public void removeRoom(String roomName) {
        Set<String> roomSet = new HashSet<>(getRoomNames());
        if (roomSet.contains(roomName)) {
            roomSet.remove(roomName);
            sharedPreferences.edit().putStringSet(KEY_ROOMS, roomSet).apply();
        }
    }

    // Temperature unit management
    public void setTemperatureUnit(String unit) {
        if ("Celsius".equals(unit) || "Fahrenheit".equals(unit)) {
            sharedPreferences.edit().putString(KEY_TEMPERATURE_UNIT, unit).apply();
        }
    }

    public String getTemperatureUnit() {
        return sharedPreferences.getString(KEY_TEMPERATURE_UNIT, "Celsius");
    }

    // Dark theme management
    public void setDarkThemeEnabled(boolean enabled) {
        sharedPreferences.edit().putBoolean(KEY_DARK_THEME_ENABLED, enabled).apply();
    }

    public boolean isDarkThemeEnabled() {
        return sharedPreferences.getBoolean(KEY_DARK_THEME_ENABLED, false);
    }

    public boolean containsDarkThemePreference() {
        return sharedPreferences.contains(KEY_DARK_THEME_ENABLED);
    }

    // Room-specific network settings
    public void setUdpPort(int port) {
        if (!isGlobal && roomName != null) {
            sharedPreferences.edit().putInt(getRoomSpecificKey(KEY_UDP_PORT), port).apply();
        }
    }

    public int getUdpPort() {
        return sharedPreferences.getInt(getRoomSpecificKey(KEY_UDP_PORT), 12345);
    }

    public void setIpAddress(String ipAddress) {
        if (!isGlobal && roomName != null) {
            sharedPreferences.edit().putString(getRoomSpecificKey(KEY_IP_ADDRESS), ipAddress).apply();
        }
    }

    public String getIpAddress() {
        return sharedPreferences.getString(getRoomSpecificKey(KEY_IP_ADDRESS), "192.168.1.100");
    }

    // Threshold management
    public void setCustomThreshold(String parameter, double value) {
        if (value < 0) {
            throw new IllegalArgumentException("Threshold value cannot be negative");
        }
        sharedPreferences.edit().putFloat(getThresholdKey(parameter), (float) value).apply();
    }

    public double getCustomThreshold(String parameter) {
        return sharedPreferences.getFloat(getThresholdKey(parameter), (float) getDefaultThreshold(parameter));
    }

    private double getDefaultThreshold(String parameter) {
        switch (parameter) {
            case "CO2":
                return 1000.0;
            case "VOC":
            case "CO":
            case "Alcohol":
                return 50.0;
            case "Humidity":
                return 60.0;
            case "Temperature":
                return 35.0;
            case "NH4":
            case "H2_MQ2":
                return 5.0;
            case "Acetone":
            case "Propane":
                return 10.0;
            default:
                return 0.0;
        }
    }

    // Reset preferences for global or room-specific settings
    public void resetPreferences() {
        sharedPreferences.edit().clear().apply();
    }

    // Utility methods
    private String getRoomSpecificKey(String baseKey) {
        return isGlobal ? baseKey : baseKey + "_" + roomName;
    }

    private String getThresholdKey(String parameter) {
        return getRoomSpecificKey(KEY_THRESHOLD_PREFIX + parameter);
    }

    // General-purpose methods for string values
    public void setString(String key, String value) {
        sharedPreferences.edit().putString(key, value).apply();
    }

    public String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }
}
