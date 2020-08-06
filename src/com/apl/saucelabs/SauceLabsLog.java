package com.apl.saucelabs;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author Akshay Lakhmani
 *
 */
public class SauceLabsLog {
	
	private static final Logger logger = Logger.getLogger(SauceLabsLog.class.getName());

	public static void error(String msg)
	{
		logger.log(Level.SEVERE, msg);
	}
	
	public static void error(Exception e)
	{
		logger.log(Level.SEVERE, e.getMessage());
	}
	
	public static void error(String msg, Exception e)
	{
		error(msg);
		error(e);
	}
	
	public static void info(String msg)
	{
		logger.log(Level.INFO, msg);
	}
	
	public static void debug(String msg)
	{
		logger.log(Level.FINE, msg);
	}
	
}
