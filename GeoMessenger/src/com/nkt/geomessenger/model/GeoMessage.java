package com.nkt.geomessenger.model;


public class GeoMessage extends GsonConvertibleObject {

	private String id;
	private long timestamp;
	private double[] loc;

	private String fromUserName;
	private String fromUserId;

	private String toUserName;
	private String toUserId;

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

	public boolean getSeenStatus() {
		return seen;
	}

	@Override
	public boolean equals(Object o) {
		GeoMessage gm = (GeoMessage) o;
		return (gm.getLoc()[0] == loc[0] && gm.getLoc()[1] == loc[1] && gm
				.getFromUserName() == fromUserName);
	}

	@Override
	public int hashCode() {
		return 1;
	}
}
