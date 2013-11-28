package com.nkt.geomessenger.model;

import java.util.List;
import java.util.Map;

public class Result extends GsonConvertibleObject{
	private List<GeoMessage> result;

	public List<GeoMessage> getResult() {
		return result;
	}
}
