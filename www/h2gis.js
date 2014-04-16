var h2gis = {
    query: function(query, successCallback, errorCallback) {
        cordova.exec(
            successCallback, // success callback function
            errorCallback, // error callback function
            'H2GIS', // mapped to our native Java class called "CalendarPlugin"
            'query', // with this action name
            [{                  // and this array of custom arguments to create our entry
                "query": query
            }]
        );
    };
}
module.exports = h2gis;