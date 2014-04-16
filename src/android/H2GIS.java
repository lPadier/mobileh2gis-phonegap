package org.ogis.h2gis;

import android.content.Context;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;

import org.json.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.SQLException;

import org.h2gis.h2spatialext.CreateSpatialExtension;




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
        try {
            if (ACTION_QUERY.equals(action)) {
                JSONObject arg_object = args.getJSONObject(0);
                String query = arg_object.getString("query").trim();


                Statement st=this.connection.createStatement();
                if (st.execute(query)) {
                    ResultSet rs = st.getResultSet();
                    callbackContext.success(H2GIS.rs2JSON(rs));
                } else {
                    this.connection.createStatement().execute(query);
                    callbackContext.success("Success");
                }


                //old code

                // String firstWord = query.split(" ", 2)[0].toUpperCase();
                // if (firstWord.equals("SELECT") || firstWord.equals("SHOW")) {
                //     ResultSet rs = this.connection.createStatement().executeQuery(query);
                //     // JSONArray a= H2GIS.convert(rs);
                //     // callbackContext.success(a.toString());
                //     callbackContext.success(H2GIS.rs2JSON(rs));
                // } else {
                //     this.connection.createStatement().execute(query);
                //     callbackContext.success("Success");
                // }
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

    public static String rs2JSON( ResultSet rs ) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        String array="";
        String object="";
        while(rs.next()) {
            object="";
            int numColumns = rsmd.getColumnCount();
            for (int i=1; i<numColumns+1; i++) {
                String column_name = rsmd.getColumnName(i);
                if(rsmd.getColumnType(i)==java.sql.Types.NVARCHAR){
                    String s=rs.getNString(column_name);
                    String start=s.substring(0,Math.min(s.length(),8));
                    if(start.equals("{\"type\":")) {
                        object+="\"geometry\":"+s+",";
                    } else {
                        object+="\""+column_name+"\":\""+s+"\",";
                    }
                } else if(rsmd.getColumnType(i)==java.sql.Types.VARCHAR){
                    String s=rs.getString(column_name);
                    String start=s.substring(0,Math.min(s.length(),8));
                    if(start.equals("{\"type\":")) {
                        object+="\"geometry\":"+s+",";
                    } else {
                        object+="\""+column_name+"\":\""+s+"\",";
                    }
                } else if(rsmd.getColumnType(i)==java.sql.Types.BIGINT) {
                    object+="\""+column_name+"\":"+rs.getInt(i)+",";
                } else if(rsmd.getColumnType(i)==java.sql.Types.BOOLEAN){
                    if (rs.getBoolean(i)) {
                        object+="\""+column_name+"\":true,";
                    } else {
                        object+="\""+column_name+"\":false,";
                    }
                } else if(rsmd.getColumnType(i)==java.sql.Types.DOUBLE){
                    object+="\""+column_name+"\":"+rs.getDouble(i)+",";
                } else if(rsmd.getColumnType(i)==java.sql.Types.FLOAT){
                    object+="\""+column_name+"\":"+rs.getFloat(i)+",";
                } else if(rsmd.getColumnType(i)==java.sql.Types.INTEGER){
                    object+="\""+column_name+"\":"+rs.getInt(i)+",";
                } else if(rsmd.getColumnType(i)==java.sql.Types.TINYINT){
                    object+="\""+column_name+"\":"+rs.getInt(i)+",";
                } else if(rsmd.getColumnType(i)==java.sql.Types.SMALLINT){
                    object+="\""+column_name+"\":"+rs.getInt(i)+",";
                } else if(rsmd.getColumnType(i)==java.sql.Types.DATE){
                    object+="\""+column_name+"\":\""+rs.getDate(i)+"\",";
                } else if(rsmd.getColumnType(i)==java.sql.Types.TIMESTAMP){
                    object+="\""+column_name+"\":"+rs.getTimestamp(i)+",";
                } else{
                    // Not implemented
                }
            }
            if (!object.equals("")) {
                object="{"+object.substring(0,object.length() -1)+"},";
            }
            array+=object;
        }
        if (!array.equals("")) {
            array=array.substring(0,array.length() -1);
        }
        array="["+array+"]";
        return array;
    }
}
