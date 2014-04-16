var h2gis = {
    query: function(query, successCallback, errorCallback) {
        cordova.exec(
            successCallback,    // success callback function
            errorCallback,      // error callback function
            'H2GIS',            // mapped to our native Java class called "H2GIS"
            'query',            // with this action name
            [{                  // and this JSON array of custom arguments
                "query": query
            }]
        );
    }
}

module.exports = h2gis;