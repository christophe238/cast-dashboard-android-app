package com.silver.dan.castdemo;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.silver.dan.castdemo.settingsFragments.CalendarSettings;
import com.silver.dan.castdemo.widgetList.OnDragListener;
import com.silver.dan.castdemo.widgetList.SimpleItemTouchHelperCallback;
import com.silver.dan.castdemo.widgets.CanBeCreatedListener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WidgetList extends Fragment implements OnDragListener {

    MainActivity activity;

    ArrayList<CanBeCreatedListener> widgetCanBeCreatedListeners = new ArrayList<>();

    @Bind(R.id.widgetList)
    RecyclerView widgetList;

    public WidgetList() {

    }

    private ItemTouchHelper mItemTouchHelper;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.widget_list, container, false);
        ButterKnife.bind(this, view);


        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        widgetList.setLayoutManager(new LinearLayoutManager(getContext()));

    }

    @OnClick(R.id.fab)
    public void addWidget() {
        final ArrayList<Widget.WidgetType> widgetTypes = new ArrayList<Widget.WidgetType>() {{
            add(Widget.WidgetType.CALENDAR);
            add(Widget.WidgetType.MAP);
            add(Widget.WidgetType.CLOCK);
            add(Widget.WidgetType.COUNTDOWN);
            add(Widget.WidgetType.WEATHER);
            add(Widget.WidgetType.RSS);
            add(Widget.WidgetType.STOCKS);
            add(Widget.WidgetType.TEXT);
        }};

        new MaterialDialog.Builder(getContext())
                .title("New Widget")
                .items(R.array.newWidgetDialogList)
                .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        final Widget widget = new Widget();

                        widget.setType(widgetTypes.get(which));
                        widget.position = widgetList.getAdapter().getItemCount();



                        CanBeCreatedListener listener = new CanBeCreatedListener() {
                            @Override
                            public void onCanBeCreated() {
                                widget.save();

                                widget.initWidgetSettings(getContext());
                                refreshList();
                                CastCommunicator.sendWidget(widget);
                            }


                        };
                        widget.getUIWidget(getContext()).onCanBeCreated(listener);

                        if (!widget.getUIWidget(getContext()).canBeCreated()) {
                            widgetCanBeCreatedListeners.add(listener);
                        }

                        return true;
                    }
                })
                .show();
    }

    @Override
    public void onResume() {
        activity = (MainActivity) getActivity();
        activity.setDrawerItemChecked(MainActivity.NAV_VIEW_WIDGETS_ITEM);

        refreshList();
        super.onResume();
    }

    public void processPermissionReceivedCallback(int key, boolean permissionGranted) {
        for (Iterator<CanBeCreatedListener> iterator = widgetCanBeCreatedListeners.iterator(); iterator.hasNext();) {
            CanBeCreatedListener listener = iterator.next();
            if (listener.key == key) {
                if (permissionGranted) {
                    listener.onCanBeCreated();
                } else {
                    iterator.remove();
                }
            }
        }
    }

    public void refreshList() {
        final WidgetList ctx = this;

        // async fetch all saved widgets
        Widget.fetchAll(new FetchAllWidgetsListener() {
            @Override
            public void results(List<Widget> widgets) {

                WidgetListAdapter adapter = new WidgetListAdapter(widgets, activity, ctx);

                widgetList.setAdapter(adapter);

                ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
                mItemTouchHelper = new ItemTouchHelper(callback);
                mItemTouchHelper.attachToRecyclerView(widgetList);
            }
        });
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

}