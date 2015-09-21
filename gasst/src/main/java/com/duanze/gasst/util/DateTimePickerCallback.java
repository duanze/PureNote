package com.duanze.gasst.util;

/**
 * Created by Duanze on 2015/3/19.
 */
public interface DateTimePickerCallback {
    void onFinish(String result);
    void onError(Exception e);
}
