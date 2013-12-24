package com.nkt.geomessenger.constants;

public class UrlConstants {
	
	private static String baseUrl = "http://serene-river-4368.herokuapp.com";
	
	private static String createGeoMsg = "/geo_messages/create";
	private static String getGeoMsg = "/geo_messages/get_nearby_messages";

	public static String getBaseUrl() {
		return baseUrl;
	}
	
	public static String getCreateGMUrl() {
		return baseUrl + createGeoMsg;
	}
	
	public static String getNearGeoMsgsUrl() {
		return baseUrl + getGeoMsg;
	}
}
