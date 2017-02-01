/**
 * Copyright (C) 2016 The ParanoidAndroid Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.server;

import android.app.IThemeCallback;
import android.app.IThemeService;
import android.content.Context;
import android.database.ContentObserver;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;

import com.android.internal.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A service to select and use custom themes.
 * The service is responsible for enabling and disabling the custom theme.
 *
 * @author Anas Karbila
 * @hide
 */
public class ThemeService extends IThemeService.Stub implements IBinder.DeathRecipient {

    private static final String TAG = ThemeService.class.getSimpleName();

    private final List<IThemeCallback> mCallbacks = new ArrayList<>();

    private ThemeObserver mObserver;
    private Context mContext;

    private boolean mSystemReady;

    public ThemeService(Context context) {
        mContext = context;
        mObserver = new ThemeObserver();
        mObserver.register();
    }

    @Override
    public void binderDied() {
        for (int i = mCallbacks.size() - 1; i >= 0; i--) {
            final IThemeCallback callback = mCallbacks.get(i);
            try {
                returnToDefaultTheme();
                if (callback != null) {
                    callback.onThemeChanged(getThemeMode(), getTheme(getAccentColor()));
                }
            } catch (DeadObjectException e) {
                Log.w(TAG, "Death object while calling onStateChanged: ", e);
            } catch (RemoteException e) {
                Log.w(TAG, "Failed to call onStateChanged: ", e);
            }
        }
        mCallbacks.clear();
        mObserver.unregister();
    }

    @Override
    public void addCallback(IThemeCallback callback) {
        synchronized (mCallbacks) {
            if (!mCallbacks.contains(callback)) {
                mCallbacks.add(callback);
            }
            dispatchCallbackAdded();
        }
    }

    // called by system server
    public void systemReady() {
        mSystemReady = true;
    }

    private int getTheme(int color) {
        final boolean isWhiteMode = getThemeMode() == 0;
        switch (color) {
            case 1:
                return isWhiteMode ? R.style.Theme_DeviceDefault_White_Red
                        : R.style.Theme_DeviceDefault_Dark_Red;
            case 2:
                return isWhiteMode ? R.style.Theme_DeviceDefault_White_Blue
                        : R.style.Theme_DeviceDefault_Dark_Blue;
            case 3:
                return isWhiteMode ? R.style.Theme_DeviceDefault_White_Orange
                        : R.style.Theme_DeviceDefault_Dark_Orange;
            case 4:
                return isWhiteMode ? R.style.Theme_DeviceDefault_White_Green
                        : R.style.Theme_DeviceDefault_Dark_Green;
            case 5:
                return isWhiteMode ? R.style.Theme_DeviceDefault_White_Yellow
                        : R.style.Theme_DeviceDefault_Dark_Yellow;
            case 6:
                return isWhiteMode ? R.style.Theme_DeviceDefault_White_Purple
                        : R.style.Theme_DeviceDefault_Dark_Purple;
            case 0:
            default:
                return isWhiteMode ? R.style.Theme_DeviceDefault_White
                        : R.style.Theme_DeviceDefault_Dark;
        }
    }

    private int getThemeMode() {
        return Settings.Secure.getInt(mContext.getContentResolver(),
                Settings.Secure.THEME_ENABLED, 0);
    }

    private int getAccentColor() {
        return Settings.Secure.getInt(mContext.getContentResolver(),
                Settings.Secure.THEME_ACCENT_COLOR, 0);
    }

    private void returnToDefaultTheme() {
        if (getThemeMode() != 0) {
            Settings.Secure.putInt(mContext.getContentResolver(),
                    Settings.Secure.THEME_ENABLED, 0);
        }
        if (getAccentColor() != 0) {
            Settings.Secure.putInt(mContext.getContentResolver(),
                    Settings.Secure.THEME_ACCENT_COLOR, 0);
        }
    }

    private void dispatchCallbackAdded() {
        if (!mSystemReady) return;
        for (int i = 0; i < mCallbacks.size(); i++) {
            final IThemeCallback callback = mCallbacks.get(i);
            try {
                if (callback != null) {
                    callback.onCallbackAdded(getThemeMode(), getTheme(getAccentColor()));
                }
            } catch (RemoteException ex) {
                // Callback is dead
            }
        }
    }

    private void dispatchThemeSettingChanged() {
        if (!mSystemReady) return;
        for (int i = 0; i < mCallbacks.size(); i++) {
            final IThemeCallback callback = mCallbacks.get(i);
            try {
                if (callback != null) {
                    callback.onThemeChanged(getThemeMode(), getTheme(getAccentColor()));
                }
            } catch (RemoteException ex) {
                // Callback is dead
            }
        }
    }

    private class ThemeObserver extends ContentObserver {
        private boolean mRegistered;

        public ThemeObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            dispatchThemeSettingChanged();
        }

        protected void register() {
            if (!mRegistered) {
                mContext.getContentResolver().registerContentObserver(
                        Settings.Secure.getUriFor(Settings.Secure.THEME_ENABLED), true, this);
                mContext.getContentResolver().registerContentObserver(
                        Settings.Secure.getUriFor(Settings.Secure.THEME_ACCENT_COLOR), true, this);
                mRegistered = true;
                dispatchCallbackAdded();
            }
        }

        protected void unregister() {
            if (mRegistered) {
                mContext.getContentResolver().unregisterContentObserver(this);
                mRegistered = false;
            }
        }
    }
}
