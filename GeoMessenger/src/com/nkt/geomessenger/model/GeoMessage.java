package com.nkt.geomessenger.model;

public class GeoMessage extends GsonConvertibleObject {

	private String id;
	private long timestamp;
	private double[] loc;

	private String fromUserName;
	private String fromUserId;
	
	private String picName;

	private String toUserName;
	private String toUserId;

	public String getToUserId() {
		return toUserId;
	}

	private float rating;
	private boolean seen;

	public String getId() {
		return id;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public double[] getLoc() {
		return loc;
	}

	public String getPicName() {
		return picName;
	}
	
	public String getMessage() {
		return message;
	}

	public String getFromUserName() {
		return fromUserName;
	}

	public String getFromUserPic() {
		return "http://graph.facebook.com/" + fromUserId
				+ "/picture?type=small";
	}

	public String getToUserName() {
		return toUserName;
	}

	public String getToUserPic() {
		return "http://graph.facebook.com/" + toUserId + "/picture?type=small";
	}

	public boolean isSeen() {
		return seen;
	}

	public void setSeen(boolean b) {
		seen = b;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public float getRating() {
		return rating;
	}

	public void setRating(float rating) {
		this.rating = rating;
	}

	@Override
	public boolean equals(Object o) {
		GeoMessage gm = (GeoMessage) o;
		return (gm.getId() == id);
	}

	@Override
	public int hashCode() {
		return 1;
	}
}
