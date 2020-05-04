/**
 * Copyright (C) 2017-2019 The ParanoidAndroid Project
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

package android.app;

import android.app.ISecureAppsCallback;

/** @hide */
interface ISecureAppsService {

    void addAppToList(in String packageName);

    void addAppExtraToList(in String packageName, in String extraName);

    void removeAppFromList(in String packageName);

    void removeAppExtraFromList(in String packageName, in String extraName);

    boolean isAppLocked(in String packageName);

    boolean hasAppExtra(in String packageName, in String extraName);
    
    int getLockedAppsCount();

    void addAppLockCallback(ISecureAppsCallback callback);

    void removeAppLockCallback(ISecureAppsCallback callback);
}
