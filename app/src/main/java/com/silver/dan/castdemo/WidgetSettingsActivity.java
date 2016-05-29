package com.silver.dan.castdemo;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.silver.dan.castdemo.settingsFragments.WidgetSettingsFragment;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WidgetSettingsActivity extends AppCompatActivity {

    private Widget widget;

    @Bind(R.id.widget_settings_type_specific)
    FrameLayout widgetTypeSettings;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind(R.id.scroll_view_header)
    FrameLayout scrollViewHeader;
    private long widgetId = -1;


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        savedInstanceState.putLong(Widget.ID, widgetId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_settings);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        Bundle b = getIntent().getExtras();
        widgetId = b.getLong(Widget.ID);


        // lookup widget in the database
        // display appropriate settings for that widget type
        widget = new Select().from(Widget.class).where(Widget_Table.id.eq(widgetId)).querySingle();

        setTitle(getApplicationContext().getString(widget.getHumanNameRes()) + " Widget");

        WidgetSettingsFragment typeSettingsFragment = widget.getUIWidget(getApplicationContext()).createSettingsFragment();

        Bundle bundle = new Bundle();
        bundle.putLong(Widget.ID, widget.id);
        typeSettingsFragment.setArguments(bundle);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.widget_settings_type_specific, typeSettingsFragment);

        transaction.commit();

        if (typeSettingsFragment.hasScrollViewHeader()) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            inflater.inflate(typeSettingsFragment.getScrollViewHeader(), scrollViewHeader, true);
        }
    }

    @OnClick(R.id.widget_settings_delete_button)
    void deleteWidget() {
        new MaterialDialog.Builder(this)
                .title("Delete Widget")
                .content("Are you sure you want to remove this widget?")
                .positiveText(R.string.delete)
                .negativeText(R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        CastCommunicator.deleteWidget(widget);
                        widget.delete();
                        onBackPressed();
                    }
                })
                .show();
    }

}
