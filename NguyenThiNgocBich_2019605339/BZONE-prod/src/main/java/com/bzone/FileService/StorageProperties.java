package com.bzone.FileService;


public final class StorageProperties {

	/**
	 * Folder location for storing files
	 */
	private String location = "src/main/resources/static/images";

	public StorageProperties() {
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

}
