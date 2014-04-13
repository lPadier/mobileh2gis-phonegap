package org.ogis.h2gis;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONStringer;
import android.content.Context;

import org.h2gis.h2spatialext.CreateSpatialExtension;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.SQLException;




public class MyJsonString implements JSONString {
    private String string;

    public MyJsonString (String s) {
        this.string=s;
    }

    public String toJSONString() {
        return this.string;
    }
}
