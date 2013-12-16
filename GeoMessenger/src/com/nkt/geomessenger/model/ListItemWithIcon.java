package com.nkt.geomessenger.model;

public class ListItemWithIcon implements Comparable<Object> {

	private int id;
	private String title;
	private String subtitle;
	private boolean checked;

	public ListItemWithIcon(String title, String subtitle) {
		this(0, title, subtitle);
	}

	public ListItemWithIcon(int id, String title, String subtitle) {
		super();
		this.id = id;
		this.title = title;
		this.subtitle = subtitle;
		this.checked = false;
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String name) {
		this.title = name;
	}

	public String getSubtitle() {
		return subtitle;
	}

	public void setSubtitle(String value) {
		this.subtitle = value;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	public void toggleChecked() {
		checked = !checked;
	}

	@Override
	public int compareTo(Object another) {
		return this.title.compareTo(((ListItemWithIcon) another).getTitle());
	}

	@Override
	public boolean equals(Object o) {
		if (subtitle.equals(((ListItemWithIcon) o).getSubtitle()))
			return true;
		else
			return false;
	}

}
