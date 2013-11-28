package com.nkt.geomessenger.model;

import java.util.List;
import java.util.Map;

public class Result extends GsonConvertibleObject{
	private List<Map<String,String>> result;

	public List<Map<String, String>> getResult() {
		return result;
	}
}
