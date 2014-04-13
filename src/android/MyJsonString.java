package org.ogis.h2gis;

import org.json.JSONString;


public class MyJsonString implements JSONString {
    private String string;

    public MyJsonString (String s) {
        this.string=s;
    }

    public String toJSONString() {
        return this.string;
    }
}
