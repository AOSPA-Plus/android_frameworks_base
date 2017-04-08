/*
 * Copyright 2014-2016 ParanoidAndroid Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.statusbar.pie;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.UserHandle;
import android.text.format.DateFormat;
import android.util.Pair;

import com.android.systemui.R;
import com.android.systemui.statusbar.BaseStatusBar;

import com.android.internal.app.AssistUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Pie Helper
 * Util class: handles system status changes and getting system state.
 * Singleton that must be initialized.
 */
public class PieHelper {
    private static PieHelper sInstance;

    private int mBatteryLevel = 0;
    private BaseStatusBar mBar;
    private Context mContext;
    private OnClockChangedListener mClockChangedListener;

    public static PieHelper getInstance() {
        if (sInstance == null) {
            sInstance = new PieHelper();
        }
        return sInstance;
    }

    private BroadcastReceiver mClockReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mClockChangedListener == null) return;
            mClockChangedListener.onChange(getSimpleTime());
        }
    };

    private BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            mBatteryLevel = intent.getIntExtra("level", 0);
        }
    };

    public void init(Context context, BaseStatusBar bar) {
        mBar = bar;
        mContext = context;
        mContext.registerReceiver(mBatteryReceiver,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        mContext.registerReceiver(mClockReceiver, filter);
    }

    protected void setOnClockChangedListener(OnClockChangedListener l) {
        mClockChangedListener = l;
    }

    protected int getNotificationCount() {
        return mBar.getNotificationCount();
    }

    protected ArrayList<Pair<String, Icon>> getNotificationIcons() {
        return mBar.getNotificationIcons();
    }

    protected String getSimpleDate() {
        SimpleDateFormat sdf = new SimpleDateFormat(
                mContext.getString(R.string.pie_date_format), Locale.getDefault());
        String date = sdf.format(new Date());
        return date.toUpperCase();
    }

    private boolean is24Hours() {
        return DateFormat.is24HourFormat(mContext);
    }

    protected String getSimpleTime() {
        SimpleDateFormat sdf = new SimpleDateFormat(
                mContext.getString(is24Hours() ? R.string.pie_hour_format_24 :
                        R.string.pie_hour_format_12), Locale.getDefault());
        String time = sdf.format(new Date());
        return time.toUpperCase();
    }

    protected int getBatteryLevel() {
        return mBatteryLevel;
    }

    protected boolean isAssistantAvailable() {
        final AssistUtils mAssistUtils = new AssistUtils(mContext);
        return mAssistUtils.getAssistComponentForUser(UserHandle.USER_CURRENT) != null
                && mBar.getAssistManager() != null;
    }

    protected void startAssistActivity() {
        if (isAssistantAvailable()) {
            mBar.startAssist(new Bundle());
        }
    }

    interface OnClockChangedListener {
        void onChange(String s);
    }
}
