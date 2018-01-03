package com.silver.dan.castdemo;

import android.content.Context;

import com.google.android.gms.auth.api.Auth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by dan on 1/7/17.
 */

public class Dashboard {
    private List<Widget> widgets;
    AppSettingsBindings settings;
    private OnCompleteCallback onDataRefreshListener;

    public Widget getWidgetById(String widgetKey) {
        if (widgets == null)
            return null;
        for (Widget w : widgets) {
            if (w.guid.equals(widgetKey)) {
                return w;
            }
        }
        return null;
    }

    void clearData() {
        this.widgets = null;
        this.settings = null;
    }

    void setOnDataRefreshListener(OnCompleteCallback callback) {
        if (this.settings != null) {
            callback.onComplete();
        }
        this.onDataRefreshListener = callback;
    }

    public Dashboard() {

    }

    static DatabaseReference getFirebaseUserDashboardReference() {
        if (AuthHelper.user == null) {
            return null;
        }
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        return mDatabase
            .child("users")
            .child(AuthHelper.user.getUid());
    }

    private void addOptions(DataSnapshot options, Context ctx) {
        settings = options.getValue(AppSettingsBindings.class);

        if (settings == null) {
            settings = new AppSettingsBindings();
        }
        settings.initDefaults(ctx);
    }

    private void loadFromFirebase(final Context ctx, final OnCompleteCallback callback) {
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                addOptions(dataSnapshot.child("options"), ctx);
                addWidgets(dataSnapshot.child("widgets"));
                if (onDataRefreshListener != null)
                    onDataRefreshListener.onComplete();
                callback.onComplete();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if (onDataRefreshListener != null)
                    onDataRefreshListener.onError(databaseError.toException());

                callback.onError(databaseError.toException());
            }
        };

        DatabaseReference userRef = getFirebaseUserDashboardReference();
        if (userRef != null)
            userRef.addListenerForSingleValueEvent(postListener);

    }


    private void addWidgets(DataSnapshot rawWidgets) {
        widgets = new ArrayList<>();
        for (DataSnapshot nextWidget : rawWidgets.getChildren()) {
            Widget widget = nextWidget.getValue(Widget.class);

            widget.guid = nextWidget.getKey();

            // native calendar widgets are deprecated
            // @todo eventually just remove them from database to remove this logic?
            if (widget.type == Widget.WidgetType.CALENDAR.getValue())
                continue;

            if (widget.getWidgetType() == null)
                continue;

            Widget.loadOptions(widget);
            widgets.add(widget);
        }

        Collections.sort(widgets, new Comparator<Widget>() {
            @Override
            public int compare(Widget w1, Widget w2) {
                if(w1.position == w2.position)
                    return 0;
                return w1.position < w2.position ? -1 : 1;
            }
        });
    }

    public List<Widget> getWidgetList() {
        return widgets;
    }


    void onLoaded(Context context, final OnCompleteCallback callback) {
        if (widgets != null) {
            callback.onComplete();
            return;
        }

        loadFromFirebase(context, callback);
    }
}
