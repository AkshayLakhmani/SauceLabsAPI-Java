package com.apl.saucelabs;

/**
 * Data Center enum
 *
 */
public enum DataCenter {
	
	US ("https://saucelabs.com/", "https://api.us-west-1.saucelabs.com/v1/eds/", "https://app.saucelabs.com/"),
    EU ("https://eu-central-1.saucelabs.com/", "https://api.eu-central-1.saucelabs.com/v1/eds/", "https://app.eu-central-1.saucelabs.com/"),
    US_EAST ("https://us-east-1.saucelabs.com/", "https://api.us-east-1.saucelabs.com/v1/eds/", "https://app.us-east-1.saucelabs.com/");
    
	public final String serverUrl;
    public final String edsServerUrl;
    public final String appServerUrl;

    DataCenter(String serverUrl, String edsServerUrl, String appServerUrl) {
        this.serverUrl = serverUrl;
        this.edsServerUrl = edsServerUrl;
        this.appServerUrl = appServerUrl;
    }

    public String server() {
        return serverUrl;
    }

    public String edsServer() {
        return edsServerUrl;
    }

    public String appServer() {
        return appServerUrl;
    }

    public static DataCenter fromString(String dataCenter) {
        for (DataCenter dc : DataCenter.values()) {
            if (dc.name().equals(dataCenter)) {
                return dc;
            }
        }
        return US; // default to US
    }
}
