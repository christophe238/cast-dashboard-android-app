package com.silver.dan.castdemo.settingsFragments;

/**
 * Created by dan on 5/26/16.
 */

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.silver.dan.castdemo.R;
import com.silver.dan.castdemo.WidgetOption;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CountdownSettings extends WidgetSettingsFragment {

    @Bind(R.id.countdown_date)
    TwoLineSettingItem countdownDate;

    @Bind(R.id.countdown_time)
    TwoLineSettingItem countdownTime;

    @Bind(R.id.countdown_text)
    TwoLineSettingItem countdownText;

    WidgetOption dateOption, textOption;

    public static String COUNTDOWN_DATE = "COUNTDOWN_DATE";
    public static String COUNTDOWN_TEXT = "COUNTDOWN_TEXT";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.countdown_settings, container, false);
        ButterKnife.bind(this, view);

        dateOption = loadOrInitOption(COUNTDOWN_DATE);
        textOption = loadOrInitOption(COUNTDOWN_TEXT);

        updateCountdownTextLabel();
        updateCountdownDateAndTimeText();

        return view;
    }


    public void updateCountdownTextLabel() {
        countdownText.setSubHeaderText(textOption.value);
    }

    public void updateCountdownDateAndTimeText() {
        Date countdownTarget = dateOption.getDate();

        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getContext());
        DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);

        String dateString = dateFormat.format(countdownTarget);
        String timeString = timeFormat.format(countdownTarget);

        countdownDate.setSubHeaderText(dateString);
        countdownTime.setSubHeaderText(timeString);
    }

    @OnClick(R.id.countdown_text)
    public void updateCountdownText() {
        new MaterialDialog.Builder(getContext())
                .title(R.string.countdown_timer_text)
                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
                .input(getString(R.string.countdown_timer_text), textOption.value, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        textOption.update(input.toString());
                        updateWidgetProperty(COUNTDOWN_TEXT, textOption.value);
                        updateCountdownTextLabel();
                    }
                }).show();
    }

    @OnClick(R.id.countdown_date)
    public void updateCountdownDate() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateOption.getDate());
        DatePickerDialog dpd = DatePickerDialog.newInstance(
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar curDate = Calendar.getInstance();
                        curDate.setTime(dateOption.getDate());

                        curDate.set(year, monthOfYear, dayOfMonth);

                        dateOption.setDate(curDate.getTime());
                        dateOption.save();
                        updateWidgetProperty(COUNTDOWN_DATE, dateOption.value);
                        updateCountdownDateAndTimeText();
                    }
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );

        dpd.show(getActivity().getFragmentManager(), "Datepickerdialog");

    }



    @OnClick(R.id.countdown_time)
    public void updateCountdownTime() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateOption.getDate());
        TimePickerDialog dpd = TimePickerDialog.newInstance(
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int second) {
                        Calendar curDate = Calendar.getInstance();
                        curDate.setTime(dateOption.getDate());

                        curDate.set(curDate.get(Calendar.YEAR), curDate.get(Calendar.MONTH), curDate.get(Calendar.DAY_OF_MONTH), hourOfDay, minute, second);

                        dateOption.setDate(curDate.getTime());
                        dateOption.save();
                        updateWidgetProperty(COUNTDOWN_DATE, dateOption.value);
                        updateCountdownDateAndTimeText();

                    }
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                false
        );

        dpd.show(getActivity().getFragmentManager(), "timepickerdialog");

    }
}