package com;

import java.io.IOException;

import com.apl.saucelabs.SauceLabsAPI;
import com.apl.saucelabs.SauceLabsTunnel;
import com.apl.saucelabs.SauceLabsLog;

public class SampleRun_MainClass {

	public static void main(String[] args) throws IOException {

		String userName;
		String accessKey;
		
		if(args.length>=0)
		{
			userName = args[0];
			accessKey = args[1];
		}
		else
		{
			//Set SauceLabs userName and accessKey
			userName = "";
			accessKey = "";
		}
		
		//Create Object
		SauceLabsTunnel t= new SauceLabsTunnel(userName, accessKey);
		
		//[Optional] Set Tunnel Name - Default Tunnel Name - SauceLabs_Tunnel_<UserName>
		t.setTunnelName("SauceLabs_Tunnel_Test");
		
		//[Optional] Set Proxy authentication
//		t.setProxy("host", 0000, "AuthUserName", "AuthUserPass");
		
		//[Optional] Advance Configuration supported by SauceLabs. Reference site : https://wiki.saucelabs.com/display/DOCS/Sauce+Connect+Proxy+Command-Line+Quick+Reference+Guide
//		ScTunnelConfig.auth.add("www.google.com");
//		ScTunnelConfig.auth.add("www.facebook.com");
		
		//Check Tunnel is Active before creating it
		if(!t.isTunnelActive())
		{
			//Create Tunnel
			t.createTunnel();
		}
		else
		{
			SauceLabsLog.info("Tunnel is already active");
		}
		
		//Close Tunnel
		t.closeTunnel();
		
		
		
		/***************************************/
		/****		Alternate Method		****/
		/***************************************/
		
		
		SauceLabsAPI api= new SauceLabsAPI(userName, accessKey);
		
		if(api.Tunnel().isTunnelActive())
			api.Tunnel().createTunnel();
		else
			SauceLabsLog.info("Tunnel is already active");
		api.Tunnel().closeTunnel();



	}

}
