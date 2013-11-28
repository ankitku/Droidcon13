package com.nkt.geomessenger.model;

public class GeoMessage extends GsonConvertibleObject{

	private String fromEmail;
	private String fromName;
	private String latitude;
	private String longitude;
	private String geoMessage;
	
	public String getFromEmail() {
		return fromEmail;
	}
	public String getFromName() {
		return fromName;
	}
	public String getLatitude() {
		return latitude;
	}
	public String getLongitude() {
		return longitude;
	}
	public String getGeoMessage() {
		return geoMessage;
	}	
}
