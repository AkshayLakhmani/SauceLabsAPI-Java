package com.apl.saucelabs;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.rmi.UnexpectedException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * @author Akshay Lakhmani
 *
 */
public class SauceLabsAPI {

	private SauceLabsTunnel Tunnel;
	
	private String userName;
	private String accessKey;
    
	private String proxyHost;
    private int proxyPort;
    private DataCenter dataCenter;
    
    private static int HTTP_READ_TIMEOUT_SECONDS = 10;
    private static int HTTP_CONNECT_TIMEOUT_SECONDS = 10;
    private final String userAgent = "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0"; 
        
    public SauceLabsAPI(String userName, String accessKey)
	{
		this.userName = userName;
		this.accessKey = accessKey;
		this.dataCenter = DataCenter.US;
		this.Tunnel = new SauceLabsTunnel(userName, accessKey, this);
	}
	
	public SauceLabsAPI(String userName, String accessKey, DataCenter dataCenter)
	{
		this.userName = userName;
		this.accessKey = accessKey;
		this.dataCenter = dataCenter;
		this.Tunnel = new SauceLabsTunnel(userName, accessKey, this);
	}
	
	public SauceLabsTunnel Tunnel()
	{
		return Tunnel;
	}
	
	/**
	 * Set proxy while connect to SauceLabs API
	 * 
	 * @author Akshay Lakhmani
	 * @param proxyHost
	 * @param proxyPort
	 */
	public void setProxy(String proxyHost, int proxyPort)
	{
		this.proxyHost = proxyHost;
		this.proxyPort = proxyPort;
	}
	
	/**
	 * Get SauceLabs User Name
	 * 
	 * @author Akshay Lakhmani
	 * @return
	 */
	public String getUserName()
	{
		return userName;
	}
	
	/**
	 * Set Read Timeout while connect to SauceLabs Server
	 * @author Akshay Lakhmani
	 * @param TimeInSecond
	 */
	public void setReadTimeout(int TimeInSecond)
	{
		HTTP_READ_TIMEOUT_SECONDS = TimeInSecond;
	}
	
	/**
	 * Set Connection Timeout while connect to SauceLabs Server
	 * @author Akshay Lakhmani
	 * @param TimeInSecond
	 */
	public void setConnectTimeout(int TimeInSecond)
	{
		HTTP_CONNECT_TIMEOUT_SECONDS = TimeInSecond;
	}

	/**
	 * Open Connection to connect to the server
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 */
    private HttpURLConnection openConnection(URL url) throws IOException
    {
        HttpURLConnection con;
        if(proxyHost!=null)
        {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
            con = (HttpURLConnection) url.openConnection(proxy);
        } else {
            con = (HttpURLConnection) url.openConnection();
        }
        con.setReadTimeout((int) HTTP_READ_TIMEOUT_SECONDS*1000);
        con.setConnectTimeout((int) HTTP_CONNECT_TIMEOUT_SECONDS*1000);
        return con;
    }
    
    
    /**
     * Add SauceLabs UserName and AccessKey for Authentication
     * 
     * @author Akshay Lakhmani
     * @param connection
     */
    private void addAuthenticationProperty(HttpURLConnection connection) {
        if (userName != null && accessKey != null) {
            String auth = userName+ ":" + accessKey;
            auth = "Basic " + Base64.encodeBase64String(auth.getBytes());
            connection.setRequestProperty("Authorization", auth);
        }

    }
    
    /**
     * Retrieve result from SauceLabs Server
     * 
     * @param restEndpoint
     * @return String - JSON output
     */
	
	private String retrieveResults(URL restEndpoint) {
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();
        try {

            HttpURLConnection connection = openConnection(restEndpoint);
            connection.setRequestProperty("User-Agent", this.userAgent);

            if (connection instanceof HttpsURLConnection) {
            	CustomSSLSocketFactory factory = new CustomSSLSocketFactory();
                ((HttpsURLConnection) connection).setSSLSocketFactory(factory);
            }

            connection.setRequestProperty("charset", "utf-8");
            connection.setDoOutput(true);
            addAuthenticationProperty(connection);

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String inputLine;
            while ((inputLine = reader.readLine()) != null) {
                builder.append(inputLine);
            }
        } catch (SocketTimeoutException e) {
        	SauceLabsLog.error("SocketTimeoutException when invoking Sauce REST API, check status.saucelabs.com for network outages", e);
        } catch (IOException | NoSuchAlgorithmException | KeyManagementException e) {
        	SauceLabsLog.error(e);
        }
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (IOException e) {
        	SauceLabsLog.error("Error closing Sauce input stream", e);
        }
        return builder.toString();
    }
	
	/**
	 * Close Input Stream
	 * 	
	 * @param connection
	 */
	private void closeInputStream(HttpURLConnection connection) {
        try {
            if (connection != null) {
                connection.getInputStream().close();
            }
        } catch (SocketTimeoutException e) {
        	SauceLabsLog.error("SocketTimeoutException when invoking Sauce REST API, check status.saucelabs.com for network outages", e);
        } catch (IOException e) {
        	SauceLabsLog.error("Error closing result stream", e);
            try {
                int responseCode = connection.getResponseCode();
                if (responseCode == 401) {
                	throw new IOException("NotAuthorized - Invalid Credentials");
                } else if (responseCode == 429) {
                	throw new IOException("Too Many Requests received");
                }
            } catch (IOException ex) {
            	SauceLabsLog.error("Error determining response code", e);
            }
        }

    }
	
	/**
	 * Append Host to end-point url according to Data Server
	 * @author Akshay Lakhmani
	 * @param endpoint
	 * @return
	 */
	private URL getURL(String endpoint)
	{
        try {
            return new URL(new URL(dataCenter.serverUrl), "/rest/" + endpoint);
        } catch (MalformedURLException e) {
        	SauceLabsLog.error("Error constructing Sauce URL", e);
            return null;
        }
    }

    
    /**
     * Uploads a file to Sauce storage.
     *
     * @param file the file to upload -param fileName uses file.getName() to store in sauce -param
     *             overwrite set to true
     * @return the md5 hash returned by sauce of the file
     * @throws IOException can be thrown when server returns an error (tcp or http status not in the
     *                     200 range)
     */
    public String uploadFile(File file) throws IOException {
        return uploadFile(file, file.getName());
    }

    /**
     * Uploads a file to Sauce storage.
     *
     * @param file     the file to upload
     * @param fileName name of the file in sauce storage -param overwrite set to true
     * @return the md5 hash returned by sauce of the file
     * @throws IOException can be thrown when server returns an error (tcp or http status not in the
     *                     200 range)
     */
    public String uploadFile(File file, String fileName) throws IOException {
        return uploadFile(file, fileName, true);
    }

    /**
     * Uploads a file to Sauce storage.
     *
     * @param file      the file to upload
     * @param fileName  name of the file in sauce storage
     * @param overwrite boolean flag to overwrite file in sauce storage if it exists
     * @return the md5 hash returned by sauce of the file
     * @throws IOException can be thrown when server returns an error (tcp or http status not in the
     *                     200 range)
     */
    public String uploadFile(File file, String fileName, Boolean overwrite) throws IOException {
        try (FileInputStream is = new FileInputStream(file)) {
            return uploadFile(is, fileName, overwrite);
        }
    }

    /**
     * Uploads a file to Sauce storage.
     *
     * @param is        Input stream of the file to be uploaded
     * @param fileName  name of the file in sauce storage
     * @param overwrite boolean flag to overwrite file in sauce storage if it exists
     * @return the md5 hash returned by sauce of the file
     * @throws IOException can be thrown when server returns an error (tcp or http status not in the
     *                     200 range)
     */
    public String uploadFile(InputStream is, String fileName, Boolean overwrite) throws IOException {
        try {
            URL restEndpoint = getURL("v1/storage/" + userName + "/" + fileName + "?overwrite=" + overwrite.toString());

            HttpURLConnection connection = openConnection(restEndpoint);

            if (connection instanceof HttpsURLConnection) {
                CustomSSLSocketFactory factory = new CustomSSLSocketFactory();
                ((HttpsURLConnection) connection).setSSLSocketFactory(factory);
            }

            connection.setRequestProperty("User-Agent", this.userAgent);
            addAuthenticationProperty(connection);
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Cache-Control", "no-cache");
            connection.setRequestProperty("Content-Type", "application/octet-stream");

            DataOutputStream oos = new DataOutputStream(connection.getOutputStream());

            int c;
            byte[] buf = new byte[8192];

            while ((c = is.read(buf, 0, buf.length)) > 0) {
                oos.write(buf, 0, c);
                oos.flush();
            }
            oos.close();

            BufferedReader rd = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            StringBuilder builder = new StringBuilder();
            while ((line = rd.readLine()) != null) {
                builder.append(line);
            }

            JSONObject sauceUploadResponse = new JSONObject(builder.toString());
            if (sauceUploadResponse.has("error")) {
                throw new UnexpectedException("Failed to upload to sauce-storage: "
                    + sauceUploadResponse.getString("error"));
            }
            return sauceUploadResponse.getString("md5");
        } catch (JSONException e) {
            throw new UnexpectedException("Failed to parse json response.", e);
        } catch (NoSuchAlgorithmException e) {
            throw new UnexpectedException("Failed to get algorithm.", e);
        } catch (KeyManagementException e) {
            throw new UnexpectedException("Failed to get key management.", e);
        }

    }
    
    /**
     * Generates a link to the job page on Saucelabs.com, which can be accessed without the user's
     * credentials. Auth token is HMAC/MD5 of the job ID with the key &lt;username&gt;:&lt;api key&gt;
     * (see <a href="http://saucelabs.com/docs/integration#public-job-links">http://saucelabs.com/docs/integration#public-job-links</a>).
     *
     * @param jobId the Sauce Job Id, typically equal to the Selenium/WebDriver sessionId
     * @return link to the job page with authorization token
     */
//    public String getPublicJobLink(String jobId) {
//        try {
//            String key = userName + ":" + accessKey;
//            String auth_token = SecurityUtils.hmacEncode("HmacMD5", jobId, key);
//            return server + "jobs/" + jobId + "?auth=" + auth_token;
//        } catch (IllegalArgumentException ex) {
//            // someone messed up on the algorithm to hmacEncode
//            // For available algorithms see {@link http://docs.oracle.com/javase/7/docs/api/javax/crypto/Mac.html}
//            // we only want to use 'HmacMD5'
//            logger.log(Level.WARNING, "Unable to create an authenticated public link to job:", ex);
//            return "";
//        }
//    }
    
    /**
     * Invokes the Sauce REST API to delete a tunnel.
     *
     * @param tunnelId Identifier of the tunnel to delete
     */
    public void deleteTunnel(String tunnelId) {

        HttpURLConnection connection = null;
        try {
            URL restEndpoint = getURL("v1/" + userName + "/tunnels/" + tunnelId);
            connection = openConnection(restEndpoint);
            connection.setRequestProperty("User-Agent", this.userAgent);
            connection.setDoOutput(true);
            connection.setRequestMethod("DELETE");
            addAuthenticationProperty(connection);
            connection.getOutputStream().write("".getBytes());
        } catch (IOException e) {
        	SauceLabsLog.error(e);
        }

        closeInputStream(connection);
    }
    
    /**
     * Invokes the Sauce REST API to retrieve the details of the tunnels currently associated with the
     * user.
     *
     * @return String (in JSON format) representing the tunnel information
     */
    public String getTunnels() {
        URL restEndpoint = this.getURL("v1/" + userName + "/tunnels");
        return retrieveResults(restEndpoint);
    }

    /**
     * Invokes the Sauce REST API to retrieve the details of the tunnel.
     *
     * @param tunnelId the Sauce Tunnel id
     * @return String (in JSON format) representing the tunnel information
     */
    public String getTunnelInformation(String tunnelId) {
        URL restEndpoint = this.getURL("v1/" + userName + "/tunnels/" + tunnelId);
        return retrieveResults(restEndpoint);
    }

    /**
     * Invokes the Sauce REST API to retrieve the concurrency details of the user.
     *
     * @return String (in JSON format) representing the concurrency information
     */
    public String getConcurrency() {
        URL restEndpoint = this.getURL("v1/users/" + userName + "/concurrency");
        return retrieveResults(restEndpoint);
    }

    /**
     * Invokes the Sauce REST API to retrieve the activity details of the user.
     *
     * @return String (in JSON format) representing the activity information
     */
    public String getActivity() {
        URL restEndpoint = this.getURL("v1/" + userName + "/activity");
        return retrieveResults(restEndpoint);
    }

    /**
     * Returns a String (in JSON format) representing the stored files list
     *
     * @return String (in JSON format) representing the stored files list
     */
    public String getStoredFiles() {
        URL restEndpoint = this.getURL("v1/storage/" + userName);
        return retrieveResults(restEndpoint);
    }

    /**
     * Returns a String (in JSON format) representing the basic account information
     *
     * @return String (in JSON format) representing the basic account information
     */
    public String getUser() {
        URL restEndpoint = this.getURL("v1/users/" + userName);
        return retrieveResults(restEndpoint);
    }

    /**
     * Returns a String (in JSON format) representing the list of objects describing all the OS and
     * browser platforms currently supported on Sauce Labs. (see <a href="https://docs.saucelabs.com/reference/rest-api/#get-supported-platforms">https://docs.saucelabs.com/reference/rest-api/#get-supported-platforms</a>).
     *
     * @param automationApi the automation API name
     * @return String (in JSON format) representing the supported platforms information
     */
    public String getSupportedPlatforms(String automationApi) {
        URL restEndpoint = this.getURL("v1/info/platforms/" + automationApi);
        return retrieveResults(restEndpoint);
    }

    /**
     * Retrieve jobs associated with a build
     *
     * @param build Build Id
     * @param limit Max jobs to return
     * @return String (in JSON format) representing jobs associated with a build
     */
    public String getBuildFullJobs(String build, int limit) {
        URL restEndpoint = this.getURL(
            "v1/" + this.userName + "/build/" + build + "/jobs?full=1" +
                (limit == 0 ? "" : "&limit=" + limit)
        );
        return retrieveResults(restEndpoint);
    }

    public String getBuildFullJobs(String build) {
        return getBuildFullJobs(build, 0);
    }

    /**
     * Retrieve build info
     *
     * @param build Build name
     * @return String (in JSON format) representing the build
     */
    public String getBuild(String build) {
        URL restEndpoint = this.getURL(
            "v1/" + this.userName + "/builds/" + build); // yes, this goes to builds instead of build like the above
        return retrieveResults(restEndpoint);
    }
	
}
