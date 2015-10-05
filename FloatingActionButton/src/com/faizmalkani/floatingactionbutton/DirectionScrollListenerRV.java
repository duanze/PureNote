/*
 * Copyright (c) 2014 SBG Apps
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.faizmalkani.floatingactionbutton;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AbsListView;

/**
 * Created by Duanze on 10/05/2015.
 */
class DirectionScrollListenerRV extends RecyclerView.OnScrollListener {

    private final FloatingActionButton mFloatingActionButton;
    int preDy;

    DirectionScrollListenerRV(FloatingActionButton floatingActionButton) {
        mFloatingActionButton = floatingActionButton;
    }

    @Override
    public void onScrolled(RecyclerView view, int dx, int dy) {
        if (dy * preDy > 0) {
            return;
        }

        mFloatingActionButton.hide(dy > 0);

        preDy = dy;
    }

    @Override
    public void onScrollStateChanged(RecyclerView view, int scrollState) {
    }
}