package org.tonylimps.filerelay.core.managers;

import org.tonylimps.filerelay.core.Profile;

import java.io.IOException;

public abstract class ProfileManager {
	protected Profile profile;
    public abstract String getUUID() throws IOException, InterruptedException;
	protected abstract String getDeviceName();
	protected  abstract void createNewProfile();
	protected abstract void initProfile();
	public abstract void saveProfile();
	public Profile getProfile() {
		return profile;
	}
}
