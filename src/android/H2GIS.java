/***
* FILE HEADER
*	 
* Authors:	Loïc Padier, Thomas Buick, Alexis 
* 			Students at Ecole Centrale de Nantes, France.  
*
* Licence: GPL V2
*
****/

package org.ogis.h2gis;

// Cordava Imports
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;

// JSON Imports
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

// JDBC Imports
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.SQLException;

// H2GIS Imports
import org.h2gis.h2spatialext.CreateSpatialExtension;

// Actual code follows

public class H2GIS extends CordovaPlugin {
    
    public static final String ACTION_QUERY = "query";  // Saving the only correct action string
    private Connection connection;                      // Declaring the jdbc connection used in our 
                                                        // Whole Class

    // INITIALIZING METHODS

    // This is an educated guess;
    // initialize(..) is a cordova/phonegap plugi function which is called
    // for each plugin when the final webapp first loads the different plugins
    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView); // for typical cordova plugin behaviour

        try {
            // Loading the jdbc driver for H2
            Class.forName("org.h2.Driver");


            // Opening the connection
            // getConnection(location,user,password)
            //
            // Here the connection is made in-memory
            // This should be changed to a already created and spatialy initialized
            // db file in a folder accessible to the android app
            this.connection = DriverManager.getConnection("jdbc:h2:mem:syntax", "sa", "sa");
            CreateSpatialExtension.initSpatialExtension(connection);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }    

    // QUERY METHOD

    // Overrinding the java side of the standard cordova/phonegap execute() method.
    // What the native java code will do when called by cordova.exec() 
    //
    // Is of the form:
    // execute( actionName , arguments , callbackContext )
    // Mapping to js code:
    // exec( sucessCallback , errorCallback , pluginName , actionName , arguments )
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        
        try {
            if (ACTION_QUERY.equals(action)) { 
                
                // Getting the first JSON object in the JSON array
                JSONObject arg_object = args.getJSONObject(0);

                // Getting the string marked by the "query" tag, and trimming whitespace
                String query = arg_object.getString("query").trim();

                // Test to see if the query expects a result set or not
                // Here naively, it is only the case if the first word is select or show
                String firstWord = query.split(" ", 2)[0].toUpperCase();
                if (firstWord.equals("SELECT") || firstWord.equals("SHOW")) {
                    // RS IS EXPECTED

                    // Getting and sending the result of the query in a stringified JSON Array
                    ResultSet rs = this.connection.createStatement().executeQuery(query);
                    JSONArray a= this.convert(rs);
                    callbackContext.success(a.toString());

                } else {
                    // RS IS NOT EXPECTED

                    // Simple execute query
                    this.connection.createStatement().execute(query);
                    callbackContext.success("Query Sucess");
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

    // CONVERT METHOD


    // This method adapted from a StackOverflow reply to handle cases where the rs contains
    // JSON objects, converts are result set into a JSON Array
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

                // If the column data is a string , we "check" if it is a valid JSON object
                // Since H2GIS gives object gives only objects starting with: '{"type":'
                // We only test for those 8 characters

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
