/*
 * Copyright 2011 Google Inc.
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
 * 
 * Modified by Uwe Trottmann for SeriesGuide
 */

package com.mot.ajaxwebapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * A base activity that defers common functionality across app activities to an
 * {@link ActivityHelper}.
 */
public abstract class BaseActivity extends Activity {
    final ActivityHelper mActivityHelper = ActivityHelper.createInstance(this);

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        return mActivityHelper.onKeyLongPress(keyCode, event)
                || super.onKeyLongPress(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (getActivityHelper().onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Returns the {@link ActivityHelper} object associated with this activity.
     */
    protected ActivityHelper getActivityHelper() {
        return mActivityHelper;
    }

    /**
     * Converts an intent into a {@link Bundle} suitable for use as fragment
     * arguments.
     */
    public static Bundle intentToFragmentArguments(Intent intent) {
        Bundle arguments = new Bundle();
        if (intent == null) {
            return arguments;
        }

        final Uri data = intent.getData();
        if (data != null) {
            arguments.putParcelable("_uri", data);
        }

        final Bundle extras = intent.getExtras();
        if (extras != null) {
            arguments.putAll(intent.getExtras());
        }

        return arguments;
    }

    /**
     * Converts a fragment arguments bundle into an intent.
     */
    public static Intent fragmentArgumentsToIntent(Bundle arguments) {
        Intent intent = new Intent();
        if (arguments == null) {
            return intent;
        }

        final Uri data = arguments.getParcelable("_uri");
        if (data != null) {
            intent.setData(data);
        }

        intent.putExtras(arguments);
        intent.removeExtra("_uri");
        return intent;
    }
    
    /**
     * read byte stream from an inputstream and convert it to string using stupid scanner.
     * http://stackoverflow.com/questions/309424/in-java-how-do-i-read-convert-an-inputstream-to-a-string
     * The reason it works is because Scanner iterates over tokens in the stream, and in this case  we separate token
     * using "beginning of the input boundary" (\A) thus giving us only one token for the entire contents of the stream.
     * Note, if you need to be specific about the input stream's encoding, you can provide the second argument to Scanner ctor that indicate
     */
    public static String convertStreamToString(java.io.InputStream is) {
        try {
            return new java.util.Scanner(is, "UTF-8").useDelimiter("\\A").next();
        } catch (java.util.NoSuchElementException e) {
            return "";
        }
    }
    
    /**
     * convert an inputstream to JsonElement with JsonParser.
     * Throw exception if json convertion failed, caller will handled the exception and close the stream. 
     */
    public static JsonElement unmarshall(InputStream jsonContent) throws Exception {
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(new InputStreamReader(jsonContent, "UTF-8"));
    
        if (element.isJsonObject()) {
            return element.getAsJsonObject();
        } else if (element.isJsonArray()) {
            return element.getAsJsonArray();
        } else {
            throw new Exception("Unknown content found in response." + element);
        }
    }
}
