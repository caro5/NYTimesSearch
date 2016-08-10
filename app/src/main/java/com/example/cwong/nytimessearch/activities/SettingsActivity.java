package com.example.cwong.nytimessearch.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.cwong.nytimessearch.R;
import com.example.cwong.nytimessearch.fragments.DatePickerFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class SettingsActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {
    EditText etDate;
    Button btnSave;
    Spinner spinner;
    ArrayList<String> checkedValues;
    CheckBox checkArts;
    CheckBox checkFashionStyle;
    CheckBox checkSports;

    public void showDatePickerDialog(View v) {
        DatePickerFragment newFragment = new DatePickerFragment();
        Bundle args = new Bundle();
        args.putString("date", etDate.getText().toString());
        newFragment.setArguments(args);
        newFragment.show(getFragmentManager(), "datePicker");
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        final Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);
        String format = "MM/dd/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
        etDate.setText(sdf.format(c.getTime()));
    }

    public void setupCheckboxes() {
        // Fires every time a checkbox is checked or unchecked
        CompoundButton.OnCheckedChangeListener checkListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton view, boolean checked) {
                // compoundButton is the checkbox
                // boolean is whether or not checkbox is checked
                // Check which checkbox was clicked
                switch(view.getId()) {
                    case R.id.cbArts:
                        if (checked) {
                            checkedValues.add(checkArts.getText().toString());
                        } else {
                            checkedValues.remove(checkArts.getText().toString());
                        }
                        break;
                    case R.id.cbFashionStyle:
                        if (checked) {
                            checkedValues.add(checkFashionStyle.getText().toString());
                        } else {
                            checkedValues.remove(checkFashionStyle.getText().toString());
                        }
                        break;
                    case R.id.cbSports:
                        if (checked) {
                            checkedValues.add(checkSports.getText().toString());
                        } else {
                            checkedValues.remove(checkSports.getText().toString());
                        }
                        break;
                }
            }
        };


        checkArts.setOnCheckedChangeListener(checkListener);
        checkFashionStyle.setOnCheckedChangeListener(checkListener);
        checkSports.setOnCheckedChangeListener(checkListener);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        etDate = (EditText) findViewById(R.id.etDate);
        btnSave = (Button) findViewById(R.id.btnSaveSettings);
        spinner = (Spinner) findViewById(R.id.spinnerSort);
        checkArts = (CheckBox) findViewById(R.id.cbArts);
        checkFashionStyle = (CheckBox) findViewById(R.id.cbFashionStyle);
        checkSports = (CheckBox) findViewById(R.id.cbSports);
        checkedValues = new ArrayList<>();
        setSupportActionBar(toolbar);
        setupCheckboxes();

        etDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(v);
            }
        });


        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                data.putExtra("date", etDate.getText().toString());

                String value = spinner.getSelectedItem().toString().toLowerCase();
                data.putExtra("sortOrder", value);

                data.putStringArrayListExtra("newsDesk", checkedValues);
                setResult(RESULT_OK, data);
                onSubmit(v);
            }
        });
    }
    public void onSubmit(View v) {
        this.finish();
    }
}
