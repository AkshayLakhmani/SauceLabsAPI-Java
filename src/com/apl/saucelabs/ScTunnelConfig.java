package com.apl.saucelabs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Configure Advance Properties before creating Tunnel.<BR>
 * Reference site : https://wiki.saucelabs.com/display/DOCS/Sauce+Connect+Proxy+Command-Line+Quick+Reference+Guide
 * 
 * @author Akshay Lakhmani
 *
 */
public class ScTunnelConfig {
	
//	public static String user = "";
//	public static String api_key = "";
//	public static String tunnel_identifier= "";
	public static List<String> auth=new ArrayList<String>();
	public static String cainfo = "";
	public static String capath = "";
	public static List<String> direct_domains=new ArrayList<String>();
	public static List<String> dns=new ArrayList<String>();
	public static List<String> fast_fail_regexps=new ArrayList<String>();
	public static int log_stats = 0;
	public static String logfile = "";
	public static int max_logsize = 0;
	public static int max_missed_acks = 30;
	public static String metrics_address = "localhost:8888";
	public static boolean no_autodetect = false;
	public static boolean no_proxy_caching = false;
	public static boolean no_remove_colliding_tunnels = false;
	public static List<String> no_ssl_bump_domains=new ArrayList<String>();
	public static String pac = "";
	public static String pidfile = "";
	public static String proxy = "";
	public static boolean proxy_tunnel = false;
//	public static String proxy_userpwd = "";
	public static String readyfile = "";
	public static int reconnect = 30;
	public static int scproxy_port = 0;
	public static int scproxy_read_limit = 0;
	public static int scproxy_write_limit = 0;
//	public static int se_port = 4445;
	public static boolean shared_tunnel = false;
	public static List<String> tunnel_domains=new ArrayList<String>();
	public static int verbose = 0;
	public static String vm_version = "";
	
	/**
	 * Generate Config.yml File for creating tunnel 
	 * 
	 * @author Akshay Lakhmani
	 * @param Path
	 * @throws IOException
	 */
	protected static void generatedConfigFile(String Path) throws IOException
	{
		File file =  new File(Path);
		if(file.isDirectory())
			file = new File(file, SauceLabsTunnel.scConfig_FileName);
		
		generatedConfigFile(file);
	}
	
	/**
	 * Generate Config.yml File for creating tunnel 
	 * 
	 * @author Akshay Lakhmani
	 * @param File
	 * @throws IOException
	 */
	protected static void generatedConfigFile(File file) throws IOException
	{
		if(file.exists())
			file.delete();
		
		StringBuilder sb = new StringBuilder();
//		sb.append("user: \"");
//		sb.append(user);
//		sb.append("\"\n");
		
//		sb.append("api-key: \"");
//		sb.append(api_key);
//		sb.append("\"\n");
		
//		sb.append("tunnel-identifier: \"");
//		sb.append(tunnel_identifier);
//		sb.append("\"\n");
		
		sb.append("auth: ");
		sb.append(auth);
		sb.append("\n");
		
		sb.append("cainfo: \"");
		sb.append(cainfo);
		sb.append("\"\n");
		
		sb.append("capath: \"");
		sb.append(capath);
		sb.append("\"\n");
		sb.append("direct-domains: ");
		sb.append(direct_domains);
		sb.append("\n");
		
		sb.append("dns: ");
		sb.append(dns);
		sb.append("\n");
		
		sb.append("fast-fail-regexps: ");
		sb.append(fast_fail_regexps);
		sb.append("\n");
		
		sb.append("log-stats: ");
		sb.append(log_stats);
		sb.append("\n");
		
		sb.append("logfile: \"");
		sb.append(logfile);
		sb.append("\"\n");
		
		sb.append("max-logsize: ");
		sb.append(max_logsize);
		sb.append("\n");
		
		sb.append("max-missed-acks: ");
		sb.append(max_missed_acks);
		sb.append("\n");
		
		sb.append("metrics-address: \"");
		sb.append(metrics_address);
		sb.append("\"\n");
		
		sb.append("no-autodetect: ");
		sb.append(no_autodetect);
		sb.append("\n");
		
		sb.append("no-proxy-caching: ");
		sb.append(no_proxy_caching);
		sb.append("\n");
		
		sb.append("no-remove-colliding-tunnels: ");
		sb.append(no_remove_colliding_tunnels);
		sb.append("\n");
		
		sb.append("no-ssl-bump-domains: ");
		sb.append(no_ssl_bump_domains);
		sb.append("\n");
		
		sb.append("pac: \"");
		sb.append(pac);
		sb.append("\"\n");
		
		sb.append("pidfile: \"");
		sb.append(pidfile);
		sb.append("\"\n");
		
		sb.append("proxy: \"");
		sb.append(proxy);
		sb.append("\"\n");
		
		sb.append("proxy-tunnel: ");
		sb.append(proxy_tunnel);
		sb.append("\n");
		
//		sb.append("proxy-userpwd: \"");
//		sb.append(proxy_userpwd);
//		sb.append("\"\n");
		
		sb.append("readyfile: \"");
		sb.append(readyfile);
		sb.append("\"\n");
		
		sb.append("reconnect: ");
		sb.append(reconnect);
		sb.append("\n");
		
		sb.append("scproxy-port: ");
		sb.append(scproxy_port);
		sb.append("\n");
		
		sb.append("scproxy-read-limit: ");
		sb.append(scproxy_read_limit);
		sb.append("\n");
		
		sb.append("scproxy-write-limit: ");
		sb.append(scproxy_write_limit);
		sb.append("\n");
		
//		sb.append("se-port: ");
//		sb.append(se_port);
//		sb.append("\n");
		
		sb.append("shared-tunnel: ");
		sb.append(shared_tunnel);
		sb.append("\n");
		
		sb.append("tunnel-domains: ");
		sb.append(tunnel_domains);
		sb.append("\n");
		
		sb.append("verbose: ");
		sb.append(verbose);
		sb.append("\n");
		
		sb.append("vm-version: \"");
		sb.append(vm_version);
		sb.append("\"\n");

		FileWriter myWriter = new FileWriter(file);
		myWriter.write(sb.toString());
		myWriter.close();
		
		SauceLabsLog.info("config.yml created - "+file.getAbsolutePath());
	}

}
