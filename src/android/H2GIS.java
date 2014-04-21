/***
* FILE HEADER
*    
* Authors:  Loïc Padier, Thomas Buick, Alexis 
*           Students at Ecole Centrale de Nantes, France.  
*
* Licence: GPL V2
*
****/

package org.ogis.h2gis;

// Android Imports
import android.content.Context;

// Cordova Imports (the interface of the plugin)
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;

// JSON Imports ()
import org.json.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;

// Java SQL imports for the connection management
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.SQLException;

// H2GIS imports
import org.h2gis.h2spatialext.CreateSpatialExtension;

// Actual code follows

public class H2GIS extends CordovaPlugin {
    
    public static final String ACTION_QUERY = "query"; // Saving the only correct action string
    private Connection connection; // Declaring the jdbc connection used in our
                                                        // Whole Class

    // INITIALIZING METHODS

    // This is an educated guess;
    // initialize(..) is a cordova/phonegap plugin function which is called
    // for each plugin when the final web application first loads the different plugins
    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView); // for typical cordova plugin behaviour

        try {
            Context context=this.cordova.getActivity().getApplicationContext(); 
            String path=context.getApplicationInfo().dataDir; // Getting the path to the database file
            
            // Loading the jdbc driver for H2
            Class.forName("org.h2.Driver");

            // Opening the connection to a blank pre-initialized file
            // getConnection(location,user,password)
            try {
                // If the file exists we connect to it.
                connection = DriverManager.getConnection("jdbc:h2:"+path+"/data;FILE_LOCK=FS;PAGE_SIZE=1024;CACHE_SIZE=8192;IFEXISTS=TRUE");

            } catch (Exception e) {
                // If it Doesn't, the above doesn't work and it is created. 
                connection = DriverManager.getConnection("jdbc:h2:"+path+"/data;FILE_LOCK=FS;PAGE_SIZE=1024;CACHE_SIZE=8192;");
                CreateSpatialExtension.initSpatialExtension(connection);

            }

        } catch (Exception ex) {
            ex.printStackTrace();

        }
    }

    // QUERY METHOD

    //
    // Overrinding the java side of the standard cordova/phonegap execute() method.
    // What the native java code will do when called by cordova.exec()
    //
    // IT is of the form:
    // execute( actionName , arguments , callbackContext )
    // 
    // Mapping to js code:
    // exec( sucessCallback , errorCallback , pluginName , actionName , arguments )
    //
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        
        try {
            if (ACTION_QUERY.equals(action)) {
                
                // Getting the first JSON object in the JSON array
                JSONObject arg_object = args.getJSONObject(0);
                
                // Getting the string marked by the "query" tag, and trimming whitespace
                String query = arg_object.getString("query").trim();

                // QUERY SENDING
                // Here two different block codes are possible, one must be commented.
                //
                // The New Block sometimes gets empty result sets in some 'CALL' SQL commands
                // 
                // The Old Block parses the first word to see if a result set should be expected
                // And then uses either executeQuery() or execute()

                // NEW BLOCK

                Statement st=this.connection.createStatement();
                if (st.execute(query)) {
                    ResultSet rs = st.getResultSet();
                    callbackContext.success(H2GIS.rs2JSON(rs));
                } else {
                    this.connection.createStatement().execute(query);
                    callbackContext.success("Success");
                }


                // // OLD BLOCK

                // String firstWord = query.split(" ", 2)[0].toUpperCase();
                // if (firstWord.equals("SELECT") || firstWord.equals("SHOW")) {
                //     ResultSet rs = this.connection.createStatement().executeQuery(query);
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

    // CONVERSION METHODS

    // Takes a SQL ResultSet and converts it into a string following JavaScript Object Notation 
    public static String rs2JSON( ResultSet rs ) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        String array="";
        String object="";
        while(rs.next()) {
            object="";
            int numColumns = rsmd.getColumnCount();
            for (int i=1; i<numColumns+1; i++) {
                String column_name = rsmd.getColumnName(i);

                // If the column data is a string , we "check" if it is a valid JSON object
                //
                // This is to handle GeoJSON export from H2GIS
                //
                // Since H2GIS gives object gives only objects starting with: '{"type":'
                // We only test for those 8 characters

                if(rsmd.getColumnType(i)==java.sql.Types.NVARCHAR){
                    String s=rs.getNString(column_name);
                    String start=s.substring(0,Math.min(s.length(),8));
                    if(start.equals("{\"type\":")) {
                        object+="\""+column_name+"\":"+s+",";
                    } else {
                        object+="\""+column_name+"\":\""+s+"\",";
                    }
                } else if(rsmd.getColumnType(i)==java.sql.Types.VARCHAR){
                    String s=rs.getString(column_name);
                    String start=s.substring(0,Math.min(s.length(),8));
                    if(start.equals("{\"type\":")) {
                        object+="\""+column_name+"\":"+s+",";
                    } else {
                        object+="\""+column_name+"\":\""+s+"\",";
                    }

                // Other common Data types are handled normally, in decreasing order of expected
                // Frequency in a database

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
                    
                    // Other data types not implemented because not realistic
                    // In a SQL database 
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
