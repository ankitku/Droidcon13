package com.nkt.geomessenger.model;

import java.util.List;
import java.util.Map;

public class QueryGeoMessagesResult extends GsonConvertibleObject{
	private List<GeoMessage> geoMessages;

	public List<GeoMessage> getResult() {
		return geoMessages;
	}
}
