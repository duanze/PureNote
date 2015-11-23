/*
 * Copyright 2015 Duanze
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.duanze.litepreferences.model;

import android.content.Context;

/**
 * Created by Duanze on 15-11-21.
 */
public class StringPref extends Pref {
    private int defRes;
    private Context mContext;

    /**
     * Special Pref to support that Preference whose default String value
     * want to be expressed by a resId.
     * <p>
     * Pass the **Application Context** or **Main Activity**
     * as parameter to avoid your activity
     * cannot be recycled by GC.
     *
     * @param key     key of preference
     * @param defRes  the resource id of the default value
     * @param context Pass the **Application Context** or **Main Activity**
     * @see Pref,Pref#curValue,Pref#setValue(String)
     */
    public StringPref(String key, int defRes, Context context) {
        this.key = key;
        this.defRes = defRes;
        mContext = context;
    }

    @Override
    public String getDefString() {
        return mContext.getString(defRes);
    }
}
