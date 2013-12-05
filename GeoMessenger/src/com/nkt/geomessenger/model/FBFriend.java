package com.nkt.geomessenger.model;

public class FBFriend extends GsonConvertibleObject{
	
	private String uid;
	
	private String name;
	
	private String picSquare;

	public FBFriend(String uid, String name, String picSquare) {
		super();
		this.uid = uid;
		this.name = name;
		this.picSquare = picSquare;
	}

	public String getUid() {
		return uid;
	}

	public String getName() {
		return name;
	}

	public String getPicSquare() {
		return picSquare;
	}

}
