package org.ogis.h2gis;
import org.ogis.h2gis.MyJsonString;

import org.json.*;

public class Test {
	public static void main(String[] args) {
		try {
			JSONStringer j = new JSONStringer();
			j.object();
			j.key("a").value(new MyJsonString("{\"test\": a}")).endObject();
			System.out.println(j.toString());
		} catch (JSONException e) {}
	}
}