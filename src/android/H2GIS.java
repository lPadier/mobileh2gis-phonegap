package org.ogis.h2gis;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
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
            Class.forName("org.h2.Driver");
            Context context=this.cordova.getActivity().getApplicationContext();
            String path=context.getApplicationInfo().dataDir;
            try {
                connection = DriverManager.getConnection("jdbc:h2:"+path+";FILE_LOCK=FS;PAGE_SIZE=1024;CACHE_SIZE=8192;IFEXISTS=TRUE");
            } catch (Exception e) {
                connection = DriverManager.getConnection("jdbc:h2:"+path+";FILE_LOCK=FS;PAGE_SIZE=1024;CACHE_SIZE=8192;");
                CreateSpatialExtension.initSpatialExtension(connection);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        try {
            if (ACTION_QUERY.equals(action)) {
                JSONObject arg_object = args.getJSONObject(0);
                String query = arg_object.getString("query").trim();
                String firstWord = query.split(" ", 2)[0].toUpperCase();
                Statement st=this.connection.createStatement();
                // if (st.execute(query)) {
                //     ResultSet rs = st.getResultSet();
                //     JSONArray a= this.convert(rs);
                //     callbackContext.success(a.toString());
                // } else {
                //     this.connection.createStatement().execute(query);
                //     callbackContext.success("Success");
                // }


                //old code

                if (firstWord.equals("SELECT") || firstWord.equals("SHOW")) {
                    ResultSet rs = this.connection.createStatement().executeQuery(query);
                    JSONArray a= this.convert(rs);
                    callbackContext.success(a.toString());
                } else {
                    this.connection.createStatement().execute(query);
                    callbackContext.success("Success");
                }
                return true;
            }
            callbackContext.error("Invalid action");
            return false;
        } catch(Exception e) {
            System.err.println("Exception: " + e.getMessage());
            callbackContext.error(e.getMessage());
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
}
