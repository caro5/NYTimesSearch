package com.example.cwong.nytimessearch.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.DatePicker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by cwong on 8/8/16.
 */
public class DatePickerFragment extends android.support.v4.app.DialogFragment implements DatePickerDialog.OnDateSetListener {
    public interface DatePickerDialogListener {
        void onFinishDatePicking(int year, int month, int day);
    }
    public DatePickerFragment() {}

    public static DatePickerFragment newInstance(String date) {
        String format = "MM/dd/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
        final Calendar c = Calendar.getInstance();

        try {
            c.setTime(sdf.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerFragment frag = new DatePickerFragment();
        Bundle args = new Bundle();
        args.putInt("year", year);
        args.putInt("month", month);
        args.putInt("day", day);
        frag.setArguments(args);
        return frag;
    }

    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        DatePickerDialogListener listener = (DatePickerDialogListener) getTargetFragment();
        String date = getArguments().getString("date");
        String format = "MM/dd/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
        final Calendar c = Calendar.getInstance();

        try {
            c.setTime(sdf.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        listener.onFinishDatePicking(year, month, day);
        dismiss();
    }
}
