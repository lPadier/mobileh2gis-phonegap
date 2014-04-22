mobileh2gis-phonegap
====================

##The phonegap plugin repositry for using H2GIS. 
The *.jar files included in the /lib folder might not always be the most recent. 

###How to use:

To be used in a phonegap project folder by using the command:

phonegap plugin add https://github.com/lPadier/mobileh2gis-phonegap.git

If plugin fails to load you may want to change the following line in your index.html calling this plugin:
```html
<script type="text/javascript" src="phonegap.js"></script>
```
Change phonegap.js to cordova.js
