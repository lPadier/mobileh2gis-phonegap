package org.ogis.h2gis;

import org.json.*;

public class MyJsonString implements JSONString {
    private String string;

    public MyJsonString (String s) {
        this.string=new String(s);
    }

    public String toJSONString() {
        return this.string;
    }
}
