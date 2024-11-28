# COEN 390 App - Air Quality Monitoring and Data Logging

## Overview

This Android application was developed as part of the COEN 390 course to provide real-time monitoring and logging of sensor data. The app features an intuitive interface to display sensor values, provide air quality recommendations, and save data for future analysis. Key features include real-time data visualization, configurable air quality thresholds, and support for multiple units (Celsius/Fahrenheit).

## Features

### Air Quality Monitoring

- **Real-Time Sensor Data Display**: Displays temperature, humidity, CO₂ levels, dust levels, and various gas concentrations from sensors over a UDP connection.
- **Comprehensive Sensor Support**: Monitors multiple air quality parameters including TVOC, CO, Alcohol, NH₄, Acetone, Propane, Hydrogen (H₂), and Toluene.
- **Threshold-Based Recommendations**: Provides actionable air quality recommendations based on user-configured thresholds.
- **AI-Powered Recommendations**: Uses GPT-based API integration to enhance suggestions for improving air quality.

### Data Logging

- **Real-Time Graphing**: Visualize incoming sensor data on interactive graphs using MPAndroidChart.
- **Save to CSV**: Save received sensor data locally in CSV format for offline analysis.
- **Data Export**: Easily share or transfer saved CSV files for reporting or further analysis.

### Configurable Settings

- **Custom Thresholds**: Adjust thresholds for temperature, humidity, CO₂ levels, dust levels, and other gas concentrations.
- **Unit Preferences**: Switch between Celsius and Fahrenheit for temperature, as well as metric and imperial units for other measurements.
- **Theme Settings**: Toggle between light and dark modes for optimal viewing.
- **Room Management**: Add, remove, and switch between multiple rooms, each with individual sensor configurations.

## Components

### Activities

- **`MainActivity`**: Entry point of the app; initializes background services and provides access to other features.
- **`RoomActivity`**: Displays real-time sensor data, provides recommendations, and supports threshold configurations.
- **`DataLogActivity`**: Visualizes sensor data over time through graphs and allows exporting data to CSV.
- **`SettingActivity`**: Allows users to change app-wide settings such as temperature units, theme preferences, and notification toggles.

### Key UI Elements

- **TextViews**: Display real-time sensor data and recommendations.
- **LineChart**: Graphical representation of incoming sensor data for each monitored parameter.
- **Buttons**: Export data to CSV, configure network settings, switch rooms, and adjust thresholds.
- **Spinners**: Facilitate room selection and unit preferences.

### Backend Components

- **`SensorDataService`**: Background service that continuously listens for incoming sensor data over UDP and broadcasts it within the app.
- **Local Broadcast Manager**: Facilitates communication between `SensorDataService` and activities without direct coupling.
- **AI Integration**: Uses OpenAI GPT models to generate air quality improvement suggestions based on current readings.

## Supported Sensors

- **Temperature** (°C/°F)
- **Humidity** (%)
- **CO₂ Level** (ppm)
- **Dust Level** (µg/m³)
- **TVOC** (ppm)
- **CO** (ppm)
- **Alcohol** (ppm)
- **NH₄** (ppm)
- **Acetone** (ppm)
- **Propane** (ppm)
- **Hydrogen (H₂)** (ppm)
- **Toluene** (ppm)

## Libraries Used

- **MPAndroidChart**: For rendering real-time graphs.
- **Retrofit**: For API calls to fetch AI recommendations.
- **LocalBroadcastManager**: For efficient in-app broadcast communication.
- **Material Components**: For consistent UI design.

## Installation

1. **Clone the Repository**:

   ```bash
   git clone https://github.com/Frankies1002/coen_390_app.git
   cd coen_390_app
   ```

2. **Configure Dependencies**:

   Add the following dependencies in your `build.gradle` file:

   ```gradle
   implementation 'com.github.PhilJay:MPAndroidChart:v3.1.0'
   implementation 'com.squareup.retrofit2:retrofit:2.9.0'
   implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
   implementation 'androidx.localbroadcastmanager:localbroadcastmanager:1.1.0'
   implementation 'com.google.android.material:material:1.2.1'
   ```

3. **Sync and Build**:

   Open the project in Android Studio, sync Gradle files, and build the project.

4. **Permissions**:

   Ensure the following permissions are declared in your `AndroidManifest.xml`:

   ```xml
   <uses-permission android:name="android.permission.INTERNET" />
   <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
   <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
   ```

   *Note:* For Android 10 and above, `WRITE_EXTERNAL_STORAGE` may not be necessary if you are writing to your app-specific directory.

## Usage

### Real-Time Monitoring

1. **Launch the App**:

   The `SensorDataService` starts automatically to collect data in the background.

2. **Select or Add a Room**:

   Use the room spinner to select an existing room or add a new one with specific settings.

3. **View Sensor Data**:

   Real-time sensor data for the selected room is displayed and updated automatically.

4. **Air Quality Recommendations**:

   Recommendations appear based on thresholds and AI insights to help improve air quality.

### Settings

1. **Open Settings**:

   Navigate to the settings menu to customize app preferences.

2. **Adjust Thresholds**:

   Set custom thresholds for each sensor to tailor recommendations.

3. **Change Units and Themes**:

   Switch between Celsius/Fahrenheit and toggle between light/dark modes.

4. **Manage Rooms**:

   Add, edit, or remove rooms, each with individual configurations.

### Data Logging

1. **Collect Data**:

   Allow the app to collect sensor data over time while it is running.

2. **View Graphs**:

   Access the `DataLogActivity` to view graphs of sensor data over time.

3. **Select Sensors to Display**:

   Use the sensor buttons to display data for specific sensors on the graph.

4. **Export Data to CSV**:

   Click **Export Data** to save the collected sensor data in CSV format.

## How It Works

1. **Sensor Data Reception**:

   - The app uses a `SensorDataService` to listen for UDP packets containing sensor data.
   - Upon receiving data, it broadcasts it within the app using `LocalBroadcastManager`.

2. **UI Updates**:

   - Parsed sensor values are displayed on `TextView`s and plotted on the graph.
   - Activities like `DataLogActivity` register receivers to listen for sensor data broadcasts.

3. **Background Data Collection**:

   - The `SensorDataService` runs as long as the app is open, ensuring continuous data collection.

4. **Threshold-Based Logic**:

   - Recommendations are generated based on user-configured thresholds for each sensor.
   - Alerts or notifications can be triggered when values exceed thresholds.

5. **AI Recommendations**:

   - Additional suggestions are fetched via GPT-based API calls to provide insights on improving air quality.

6. **Data Export**:

   - Collected sensor data is stored and can be exported as a CSV file for external analysis.

## Screenshots

![image](https://github.com/user-attachments/assets/4f89027b-b204-4720-9588-ad672466e230)

![image](https://github.com/user-attachments/assets/46acc958-acea-4604-9109-a92bf7cbee8d)

![image](https://github.com/user-attachments/assets/56e25bb6-13c9-47c7-8ddd-74e28da3659a)


- **Real-Time Monitoring**: Displays comprehensive sensor values with thresholds and recommendations.
- **Settings Page**: Change preferences for units, thresholds, themes, and manage rooms.
- **Graphing and Logging**: Visualize and save data for all supported sensors.

## Prerequisites

- **Android 8.0+ (API 26)**: The app supports devices running Android API level 26 and above.
- **Network Access**: Ensure the device is on the same network as the sensor sender.
- **Sensors**: Compatible sensors that send data in the expected JSON format over UDP.

## Known Issues

- **BroadcastReceiver**: Ensure the local broadcast is properly received to update displays across activities.
- **UDP Packet Loss**: If packets are dropped due to network issues, the graph may show missing data points.
- **Permissions**: On newer Android versions, you may need to adjust permissions for writing to external storage.

## Future Enhancements

- **Enhanced Error Handling**: Improve handling of malformed UDP packets and unexpected data.
- **User-Defined UDP Port**: Add a UI option to configure the UDP listening port for each room.
- **Detailed Analytics**: Include statistics like averages, trends, and alerts for better air quality insights.
- **Persistent Data Storage**: Implement a local database to store sensor data persistently across sessions.
- **Foreground Service**: Convert `SensorDataService` to a foreground service to ensure data collection continues even when the app is minimized.

## License

This project is licensed under the MIT License.

## Contact

For questions or suggestions, contact the COEN 390 development team.
