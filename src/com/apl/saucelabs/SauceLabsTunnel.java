package com.apl.saucelabs;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONObject;


/**
 * To Create SauceLabs Tunnel and handle it
 * @author Akshay Lakhmani
 *
 */
public class SauceLabsTunnel {

	private String userName;
	private String accessKey;
	private String scExe_path;
	private String scConfig_path;
	private int scPort;
	private String tunnelName;
	private String tunnelId;
	private String tempPath;
	private int maxWait;
	private int tunnelIterator;
	private Process process;
	private SauceLabsAPI api;
	
	private String proxyHost;
    private int proxyPort;
    private String proxyAuthUserName;
    private String proxyAuthPassword;

	private String scExe_FileName="sc.exe";
	protected static String scConfig_FileName="config.yml";
	private String scZip_FileName="sc.zip";
	private String sc_FolerName="sc";
	private String scUnzip_FolerName=sc_FolerName+"\\sc-4.6.2-win32\\bin\\";
	
	private String scExe_Ver="4.6.2";
	private String scExe_download_Path_Win="https://saucelabs.com/downloads/sc-4.6.2-win32.zip";
	private String scExe_download_Path_Mac="https://saucelabs.com/downloads/sc-4.6.2-osx.zip";
	private String scExe_download_Path_Linux32="https://saucelabs.com/downloads/sc-4.6.2-linux32.tar.gz";
	private String scExe_download_Path_Linux64="https://saucelabs.com/downloads/sc-4.6.2-linux.tar.gz";

	public SauceLabsTunnel(String userName, String accessKey)
	{
		this.userName = userName;
		this.accessKey = accessKey;
		this.tempPath = System.getProperty("java.io.tmpdir");
		this.scExe_path = tempPath + scExe_FileName;
		this.scConfig_path  = tempPath + scConfig_FileName;
		this.tunnelName = "SauceLabs_Tunnel_"+userName.replace(".", "_").replace("-", "_");
		this.scPort = 4450;
		this.maxWait = 120;
		this.tunnelIterator=1;
		this.tunnelId = null;
		this.api = new SauceLabsAPI(userName, accessKey);
	}
	
	public SauceLabsTunnel(String userName, String accessKey, SauceLabsAPI api)
	{
		this.userName = userName;
		this.accessKey = accessKey;
		this.tempPath = System.getProperty("java.io.tmpdir");
		this.scExe_path = tempPath + scExe_FileName;
		this.scConfig_path  = tempPath + scConfig_FileName;
		this.tunnelName = "SauceLabs_Tunnel_"+userName.replace(".", "_").replace("-", "_");
		this.scPort = 4450;
		this.maxWait = 120;
		this.tunnelIterator=1;
		this.tunnelId = null;
		this.api = api;
	}
	
	
	/**
	 * Set Existing Sc.exe file if available
	 * 
	 * @author Akshay Lakhmani
	 * @param File - sc.exe file object
	 */
	public void setScExeFile(File file)
	{
		if(file.isDirectory())
		{
			file = new File(file, "sc.exe");
			if(file.exists())
				this.scExe_path = file.getAbsolutePath();
			else
				SauceLabsLog.error("sc.exe does not exist on provided path - "+file.getAbsolutePath());
		}
		else
		{
			if(file.exists())
				this.scExe_path = file.getAbsolutePath();
			else
				SauceLabsLog.error("sc.exe does not exist on provided path - "+file.getAbsolutePath());
		}
	}
	
	/**
	 * Set Existing Sc.exe file if available
	 * 
	 * @author Akshay Lakhmani
	 * @param String - sc.exe file path
	 */
	public void setScExeFile(String Path)
	{
		File file = new File(Path);
		setScExeFile(file);
	}
	
	/**
	 * Set Existing Sc.exe file if available
	 * 
	 * @author Akshay Lakhmani
	 * @param File - sc.exe file path
	 */
	public void setScExePath(File Path)
	{
		setScExeFile(Path);
	}
	
	/**
	 * Set Existing Sc.exe file if available
	 * 
	 * @author Akshay Lakhmani
	 * @param String - sc.exe Path
	 */
	public void setScExePath(String Path)
	{
		File file = new File(Path);
		setScExeFile(file);
	}
	
	/**
	 * Get Tunnel Name
	 * @author Akshay Lakhmani
	 * @return
	 */
	public String getTunnelName()
	{
		return tunnelName;
	}
	
	/**
	 * Set Customize Tunnel Name
	 * 
	 * @author Akshay Lakhmani
	 * @param tunnelName
	 */
	public void setTunnelName(String tunnelName)
	{
		this.tunnelName = tunnelName.replace(".", "_").replace("-", "_").replace(" ", "_");
	}
	
	/**
	 * Get created Tunnel Id</BR>
	 * If tunnel Id is NULL and tunnel is created than call isTunnelActive() before calling this method 
	 * 
	 * @author Akshay Lakhmani
	 * @return
	 */
	public String getTunnelID()
	{
		return tunnelId;
	}
	
	/**
	 * Set Proxy to for creating Tunnel
	 * 
	 * @author Akshay Lakhmani
	 * @param proxyHost
	 * @param proxyPort
	 * @param proxyAuthUserName
	 * @param proxyAuthPassword
	 */
	public void setProxy(String proxyHost, int proxyPort, String proxyAuthUserName, String proxyAuthPassword)
	{
		this.proxyHost = proxyHost;
		this.proxyPort = proxyPort;
		this.proxyAuthUserName = proxyAuthUserName;
		this.proxyAuthPassword = proxyAuthPassword;
		this.api.setProxy(proxyHost, proxyPort);
	}

	/**
	 * Create Tunnelwith procided details<BR>
	 * use ScTunnelConfig class to configure Advance Properties. Reference site : https://wiki.saucelabs.com/display/DOCS/Sauce+Connect+Proxy+Command-Line+Quick+Reference+Guide
	 * 
	 * @author Akshay Lakhmani
	 * @return
	 */
	public boolean createTunnel()
	{
		try
		{
			verifyFileLocatin();
			String command="";
			ScTunnelConfig.generatedConfigFile(scConfig_path);
			
			if(proxyHost!=null)
			{
				command=scExe_path+" -c "+scConfig_path+
						" --user "+userName+
						" --api-key "+accessKey+
						" --se-port "+scPort+
						" --proxy-userpwd "+proxyAuthUserName+":"+proxyAuthPassword+
						" --auth "+proxyHost+":"+proxyPort+":"+proxyAuthUserName+":"+proxyAuthPassword+
						" --tunnel-identifier "+tunnelName;
			}
			else
			{
				command=scExe_path+" -c "+scConfig_path+
						" --user "+userName+
						" --api-key "+accessKey+
						" --se-port "+scPort+
						" --tunnel-identifier "+tunnelName;
			}
			
			process = Runtime.getRuntime().exec(command);
			InputStream is= process.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			
			command="";
			String commandOutput="";
			for(int i=0;i<=maxWait;i++)
			{
				if((commandOutput = reader.readLine()) != null)
				{
					command+=commandOutput+"\n";
					SauceLabsLog.info(commandOutput);
					Thread.sleep(1000);
					
					if(commandOutput.contains("Tunnel ID: "))
					{
						tunnelId=commandOutput.split("Tunnel ID: ")[1].trim();
						SauceLabsLog.info("Tunnel ID: "+tunnelId);
					}
					else if(commandOutput.contains("Sauce Connect is up"))
					{
						SauceLabsLog.info("Tunnel Created Successfully!");
						break;
					}
					else if(commandOutput.contains("Failed to start Selenium listene") && tunnelIterator<=1)
					{
						SauceLabsLog.error("ERROR : Tunnel Creation Failed. Retrying...");
						tunnelIterator++;
						scPort+=10;
						return createTunnel();
					}
					else if(commandOutput.contains("Goodbye"))
					{
						SauceLabsLog.error("ERROR : Tunnel Creation Failed. Please check logs");
						break;
					}
				}
				else
				{
					break;
				}
			}
		}
		catch (Exception e) {
			SauceLabsLog.error(e);
		}

		return false;
	}
	
	/**
	 * Verify Tunnel is Active
	 * 
	 * @author Akshay Lakhmani
	 * @return True - Tunnel is Active<BR>
	 * False - Tunnel is not Active
	 * 
	 */
	public boolean isTunnelActive()
	{
		String tunnelList = api.getTunnels();
		tunnelList = tunnelList.replace("[", "").replace("]", "").replace("\"", "").trim();
		
		if(!tunnelList.equals(""))
		{
			for(String tunnel:tunnelList.split(","))
			{
				SauceLabsLog.info("Tunnel - "+tunnel);
				JSONObject obj = new JSONObject(api.getTunnelInformation(tunnel));
				if(obj.has("tunnel_identifier"))
				{
					String tunnelName = obj.getString("tunnel_identifier");
					if(tunnelName.equalsIgnoreCase(this.tunnelName))
					{
						tunnelId = obj.getString("id");
						return true;
					}
				}
				SauceLabsLog.info(obj.toString());
			}
		}
		else
		{
			SauceLabsLog.info("No Tunnel Active");
		}

		return false;
	}
	
	/**
	 * Get All Active Tunnel Names
	 * 
	 * @author Akshay Lakhmani
	 * @return ArrayList of String - Active Tunnel List<BR>
	 * NULL - No active Tunnel found
	 */
	public ArrayList<String> getAllActiveTunnelNames()
	{
		ArrayList<String> arr_tunnelList = new ArrayList<String>(); 
		String tunnelList = api.getTunnels();
		tunnelList = tunnelList.replace("[", "").replace("]", "").replace("\"", "").trim();
		
		if(!tunnelList.equals(""))
		{
			for(String tunnel:tunnelList.split(","))
			{
				SauceLabsLog.info("Tunnel - "+tunnel);
				JSONObject obj = new JSONObject(api.getTunnelInformation(tunnel));
				if(obj.has("tunnel_identifier"))
				{
					arr_tunnelList.add(obj.getString("tunnel_identifier"));
				}
				SauceLabsLog.info(obj.toString());
			}
		}
		else
		{
			SauceLabsLog.info("No Tunnel Active");
		}
		return null;
	}
	
	/**
	 * Close existing created Tunnel
	 * 
	 * @author Akshay Lakhmani
	 */
	public void closeTunnel()
	{
		if(process!=null || tunnelId!=null)
		{
			SauceLabsLog.info("Closing Tunnel...");
			api.deleteTunnel(tunnelId);

			if(process!=null)
			{
				SauceLabsLog.info("Closing sc.exe...");
				process.destroy();
			}

			SauceLabsLog.info("Tunnel Closed");
		}
		else
		{
			SauceLabsLog.info("No Active Tunnel found to close it");
		}
	}
	
	/**
	 * Verify sc.exe file presence.<BR>
	 * If file not found than it will download from SauceLabs site and kept in temp folder.
	 * 
	 * @author Akshay Lakhmani
	 * @throws Exception
	 */
	private void verifyFileLocatin() throws Exception
	{
		if(!new File(scExe_path).exists())
		{
			SauceLabsLog.info("sc.zip Downloading...");
			downloadFile(tempPath+scZip_FileName);
			SauceLabsLog.info("Download Completed");
			
			SauceLabsLog.info("sc.zip Unzipping...");
			UnzipUtility.unzip(tempPath+scZip_FileName, tempPath+sc_FolerName);
			SauceLabsLog.info("Unzip Completed");
			
			File file = new File(tempPath+scUnzip_FolerName+scExe_FileName);
			if(file.exists())
			{
				file.renameTo(new File(scExe_path));
				SauceLabsLog.info("sc.exe updated!");
			}
			else
			{
				SauceLabsLog.error("sc.exe is not found in Unzip folder -  "+file.getAbsolutePath());
			}
		}
		else
		{
			SauceLabsLog.info("sc.exe already available!");
		}
		
		/**	Commented code of Verify Versio of Sc.exe **/
//		else
//		{
//			String str_ScVersion="";
//			String readValue="";
//			Runtime rt = Runtime.getRuntime();
//			String[] comands = {scExe_path, "---version"};
//			Process proc = rt.exec(comands);
//
//			BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
//			while((readValue = br.readLine())!= null)
//			{
//				str_ScVersion+=readValue;
//			}
//			str_ScVersion=str_ScVersion.split(",")[0].replace("Sauce Connect ", "");
//
//			if(str_ScVersion.equals(scExe_Ver))
//			{
//				myLog.info("sc.exe is up to date");
//			}
//			else
//			{
//				myLog.info("Downloading sc.zip ...");
//				downloadFile(scExe_download_Path, tempPath+scZip_FileName);
//				
//				myLog.info("unzipping sc.zip ...");
//				UnzipUtility.unzip(tempPath+scZip_FileName, tempPath+sc_FolerName);
//				
//				myLog.info("sc.exe updated!");
//			}
//		}
	}

	/**
	 * Download Sc.exe from SauceLabs according to Operating System.
	 * 
	 * @author Akshay Lakhmani
	 * @param toPath
	 * @throws Exception
	 */
	private void downloadFile(String toPath) throws Exception
	{
		String fromUrl = "";
		
		String OS = System.getProperty("os.name").toLowerCase();
		if(OS.indexOf("win") >= 0)
		{
			fromUrl = scExe_download_Path_Win;
		}
		else if(OS.indexOf("mac") >= 0)
		{
			fromUrl = scExe_download_Path_Mac;
		}
		else if(OS.indexOf("nix") >= 0 || OS.indexOf("sunos") >= 0)
		{
			if(System.getProperty("sun.arch.data.model").equals("64"))
			{
				fromUrl = scExe_download_Path_Linux64;
			}
			else
			{
				fromUrl = scExe_download_Path_Linux32;
			}
		}
		else			
		{
			throw new Exception("Invalid OS Name - "+OS);
		}
				
        BufferedInputStream in = new BufferedInputStream(new URL(fromUrl).openStream());
        FileOutputStream fos = new FileOutputStream(toPath);
        BufferedOutputStream bout = new BufferedOutputStream(fos);
        byte data[] = new byte[1<<24];
        int read;
        
        while((read = in.read(data,0,1<<24))>=0)
        {
            bout.write(data, 0, read);
        }
        bout.close();
        in.close();
	}
}
