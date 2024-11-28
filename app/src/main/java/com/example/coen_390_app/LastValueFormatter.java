package com.example.coen_390_app;

import android.annotation.SuppressLint;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;

public class LastValueFormatter extends ValueFormatter {
    private float lastXValue;

    public void setLastXValue(float lastXValue) {
        this.lastXValue = lastXValue;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public String getPointLabel(Entry entry) {
        if (entry.getX() == lastXValue) {
            return String.format("%.1f", entry.getY());
        } else {
            return ""; // Hide value labels for other entries
        }
    }
}

