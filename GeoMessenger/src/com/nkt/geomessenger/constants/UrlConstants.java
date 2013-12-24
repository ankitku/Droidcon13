package com.nkt.geomessenger.constants;

public class UrlConstants {
	
	private static String baseUrl = "http://serene-river-4368.herokuapp.com";
	
	private static String createGeoMsg = "/geo_messages/create";
	private static String getNearbyMsgs = "/geo_messages/get_nearby_messages";
	private static String getUserMsgs = "/geo_messages/get_messages_posted_by_user";
	
	public static String getBaseUrl() {
		return baseUrl;
	}
	
	public static String getCreateGMUrl() {
		return baseUrl + createGeoMsg;
	}
	
	public static String getNearGeoMsgsUrl() {
		return baseUrl + getNearbyMsgs;
	}
	
	public static String getUserGeoMsgsUrl() {
		return baseUrl + getUserMsgs;
	}
}
