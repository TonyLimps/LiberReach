package com.tonylimps.filerelay.core;

import java.io.IOException;

public abstract class ProfileManager {
    abstract String getUUID() throws IOException, InterruptedException;
    abstract String getDeviceName();
    abstract void createNewProfile();
    abstract void initProfile();
    abstract void saveProfile();
}
