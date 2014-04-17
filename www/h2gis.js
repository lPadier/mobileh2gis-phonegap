/***
* FILE HEADER
*	 
* Authors:	Loïc Padier, Thomas Buick, Alexis 
* 			Students at Ecole Centrale de Nantes, France.  
*
* Licence: GPL V2
*
****/

// A phonegap plugin on the js side works through a standard form cordova function
// which is normally built to support more than one plugin, with more than one action
//
// The function is of the form:
// exec( sucessCallback , errorCallback , pluginName , actionName , arguments )
//
// Here we simplify things by only using an 'h2gis.query' function , since our plugin has only
// one valid action

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