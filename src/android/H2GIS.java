package org.ogis.h2gis;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.json.JSONException;
import org.json.JSONArray;
import org.json2.*;
import android.content.Context;

import org.h2gis.h2spatialext.CreateSpatialExtension;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.SQLException;




public class H2GIS extends CordovaPlugin {
    public static final String ACTION_QUERY = "query";
    private Connection connection;
    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        // your init code here
        try {
            Context context=this.cordova.getActivity().getApplicationContext();
            String path=context.getApplicationInfo().dataDir;
            Class.forName("org.h2.Driver");
            try {
                connection = DriverManager.getConnection("jdbc:h2:"+path+"/data;FILE_LOCK=FS;PAGE_SIZE=1024;CACHE_SIZE=8192;IFEXISTS=TRUE");
            } catch (Exception e) {
                connection = DriverManager.getConnection("jdbc:h2:"+path+"/data;FILE_LOCK=FS;PAGE_SIZE=1024;CACHE_SIZE=8192;");
                CreateSpatialExtension.initSpatialExtension(connection);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        String path="";
        try {
            Context context=this.cordova.getActivity().getApplicationContext();
            path=context.getApplicationInfo().dataDir;
            if (ACTION_QUERY.equals(action)) {
                JSONObject arg_object = args.getJSONObject(0);
                String query = arg_object.getString("query").trim();
                String firstWord = query.split(" ", 2)[0].toUpperCase();

                // Statement st=this.connection.createStatement();
                // if (st.execute(query)) {
                //     ResultSet rs = st.getResultSet();
                //     JSONArray a= H2GIS.convert(rs);
                //     callbackContext.success(a.toString());
                // } else {
                //     this.connection.createStatement().execute(query);
                //     callbackContext.success("Success");
                // }


                //old code

                if (firstWord.equals("SELECT") || firstWord.equals("SHOW")) {
                    ResultSet rs = this.connection.createStatement().executeQuery(query);
                    // JSONArray a= H2GIS.convert(rs);
                    // callbackContext.success(a.toString());
                    callbackContext.success(H2GIS.convert2(rs));
                } else {
                    this.connection.createStatement().execute(query);
                    callbackContext.success("Success");
                }
                return true;
            }
            callbackContext.error("Invalid action");
            return false;
        } catch(Exception e) {
            System.err.println("Exception: " + e.getMessage() + "path= "+path);
            callbackContext.error(e.getMessage()+ "path= "+path);
            return false;
        }
    }


    public static JSONArray convert( ResultSet rs ) throws SQLException, JSONException {
        JSONArray json = new JSONArray();
        ResultSetMetaData rsmd = rs.getMetaData();

        while(rs.next()) {
            int numColumns = rsmd.getColumnCount();
            JSONObject obj = new JSONObject();

            for (int i=1; i<numColumns+1; i++) {
                String column_name = rsmd.getColumnName(i);

                if(rsmd.getColumnType(i)==java.sql.Types.ARRAY) {
                    obj.put(column_name, rs.getArray(column_name));
                } else if(rsmd.getColumnType(i)==java.sql.Types.BIGINT) {
                    obj.put(column_name, rs.getInt(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.BOOLEAN){
                    obj.put(column_name, rs.getBoolean(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.BLOB){
                    obj.put(column_name, rs.getBlob(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.DOUBLE){
                    obj.put(column_name, rs.getDouble(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.FLOAT){
                    obj.put(column_name, rs.getFloat(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.INTEGER){
                    obj.put(column_name, rs.getInt(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.NVARCHAR){
                    String s=rs.getNString(column_name);
                    String start=s.substring(0,Math.min(s.length(),8));
                    if(start.equals("{\"type\":")) {
                        obj.put(column_name, new JSONObject(rs.getNString(column_name)));
                    } else {
                        obj.put(column_name, rs.getNString(column_name));
                    }
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.VARCHAR){
                    String s=rs.getNString(column_name);
                    String start=s.substring(0,Math.min(s.length(),8));
                    if (start.equals("{\"type\":")) {
                        obj.put(column_name, new JSONObject(rs.getString(column_name)));
                    } else {
                        obj.put(column_name, rs.getString(column_name));
                    }
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.TINYINT){
                    obj.put(column_name, rs.getInt(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.SMALLINT){
                    obj.put(column_name, rs.getInt(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.DATE){
                    obj.put(column_name, rs.getDate(column_name));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.TIMESTAMP){
                    obj.put(column_name, rs.getTimestamp(column_name));
                }
                else{
                    obj.put(column_name, rs.getObject(column_name));
                }
            }

            json.put(obj);
        }

        return json;
    }


    public static String convert2( ResultSet rs ) throws SQLException, org.json2.JSONException {
        org.json2.JSONStringer result= new org.json2.JSONStringer();
        result.array();
        ResultSetMetaData rsmd = rs.getMetaData();

        while(rs.next()) {
            int numColumns = rsmd.getColumnCount();
            result.object();

            for (int i=1; i<numColumns+1; i++) {
                String column_name = rsmd.getColumnName(i);

                if(rsmd.getColumnType(i)==java.sql.Types.ARRAY) {
                    // Not implemented
                } else if(rsmd.getColumnType(i)==java.sql.Types.BIGINT) {
                    result.key(column_name).value(rs.getInt(i));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.BOOLEAN){
                    result.key(column_name).value(rs.getBoolean(i));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.BLOB){
                    // Not implemented
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.DOUBLE){
                    result.key(column_name).value(rs.getDouble(i));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.FLOAT){
                    result.key(column_name).value(rs.getFloat(i));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.INTEGER){
                    result.key(column_name).value(rs.getInt(i));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.NVARCHAR){
                    String s=rs.getNString(column_name);
                    String start=s.substring(0,Math.min(s.length(),8));
                    if(start.equals("{\"type\":")) {
                        result.key(column_name).value( new MyJsonString(rs.getNString(i)));
                    } else {
                        result.key(column_name).value(rs.getNString(i));
                    }
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.VARCHAR){
                    String s=rs.getNString(column_name);
                    String start=s.substring(0,Math.min(s.length(),8));
                    if (start.equals("{\"type\":")) {
                        result.key(column_name).value( new MyJsonString(rs.getString(i)));
                    } else {
                        result.key(column_name).value(rs.getString(column_name));
                    }
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.TINYINT){
                    result.key(column_name).value(rs.getInt(i));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.SMALLINT){
                    result.key(column_name).value(rs.getInt(i));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.DATE){
                    result.key(column_name).value(rs.getDate(i));
                }
                else if(rsmd.getColumnType(i)==java.sql.Types.TIMESTAMP){
                    result.key(column_name).value(rs.getTimestamp(i));
                }
                else{
                    // Not implemented
                }
            }

            result.endObject();
        }
        result.endArray();
        return result.toString();
    }
}
