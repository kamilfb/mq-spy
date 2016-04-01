/***********************************************************************************
 * 
 * Copyright (c) 2015 Kamil Baczkowicz
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 *    
 * The Eclipse Distribution License is available at
 *   http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 * 
 *    Kamil Baczkowicz - initial API and implementation and/or initial documentation
 *    
 */
package pl.baczkowicz.msgspy.daemon.remote.handlers;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.msgspy.daemon.remote.RemoteController;

public abstract class BaseRequestHandler extends HttpServlet
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(BaseRequestHandler.class);
	
	private static final long serialVersionUID = -9204115887954866526L;
	
	protected RemoteController controller;
	
	protected boolean isVerbose; 

	public BaseRequestHandler(final RemoteController controller)
	{
		this.controller = controller;
	}
	
	public Map<String, Object> getParameterMap(final HttpServletRequest req)
	{
		final Map<String, Object> args = new HashMap<>();
		
		args.putAll(req.getParameterMap());
		
		return args;
	}
	
	public static boolean isVerbose(final HttpServletRequest req)	
	{
		final String verbose = req.getParameter("verbose");
		
		final boolean isVerbose = Boolean.valueOf(verbose);
		
		return isVerbose;
	}
	
	public void checkOfIsVerbose(final HttpServletRequest req)	
	{
		isVerbose = isVerbose(req);
	}
	
	public void logMessage(final HttpServletResponse resp, final String message)
	{
		if (isVerbose)
		{
			try
			{
				resp.getWriter().println(message);
			}
			catch (Exception e)
			{
				logger.error("Cannot append to response", e);
			}
		}
		
		logger.info(message);
	}
	
	public void setVerbose(final boolean value)
	{
		this.isVerbose = value;
	}
}
