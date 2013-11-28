package com.nkt.geomessenger.model;

import com.nkt.geomessenger.GeoMessenger;

public class GsonConvertibleObject {
	
	// class members common to all server responses
	protected String status;

	protected String requestType, requestId;
	
	protected String reason, header, text, message;
	
	public String getStatus() {
		return status;
	}

	public String getRequestType() {
		return requestType;
	}
	
	public String getRequestId() {
		return requestId;
	}

	public String getReason() {
		return reason;
	}
	
	public String getHeader() {
		return header;
	}
	
	public String getText() {
		return text;
	}
	
	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return GeoMessenger.gson.toJson(this);
	}

	public static <T extends GsonConvertibleObject> T getObjectFromJson(
			String jsonString, Class<T> tClass) {
		if (jsonString == null || "".equals(jsonString))
			return null;

		T tDetails = GeoMessenger.gson.fromJson(jsonString, tClass);
		return tDetails;
	}
}
