package com.nkt.geomessenger.constants;

public enum GMConstants {
	SENT_MSGS("Sent"), INVITE_FRIENDS("Invite Friends"), FEEDBACK(
			"Feedback"), HOME("Nearby Messages");

	private GMConstants(String name) {
		this.name = name;
	}

	private final String name;

	@Override
	public String toString() {
		return name;
	}

	public static GMConstants fromString(String text) {
		if (text != null) {
			for (GMConstants b : GMConstants.values()) {
				if (text.equalsIgnoreCase(b.name)) {
					return b;
				}
			}
		}
		return null;
	}

}
