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
package pl.baczkowicz.mqspy.daemon.remote.handlers;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;

import pl.baczkowicz.mqspy.daemon.remote.RemoteController;

public class RunScriptRequestHandler extends BaseRequestHandler
{
	private static final long serialVersionUID = 1031333668786590446L;

	public RunScriptRequestHandler(final RemoteController controller)
	{
		super(controller);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		setVerbose(true);
		resp.setStatus(HttpStatus.OK_200);		
		logMessage(resp, "Received: " + req.getParameterMap());
		
		final String scriptName = req.getParameter("name");
		final String async = req.getParameter("async");
		
		if (scriptName != null)
		{			
			final String scriptLocation = controller.getScriptLocation(scriptName);
			
			if (scriptLocation == null)
			{
				logMessage(resp, "Invalid request: file " + scriptName + " does not exist");
				return;
			}
			
			if (async != null)
			{
				logMessage(resp, "Attemping to run script with parameters: " + scriptLocation);
				controller.getDaemon().runScript(scriptLocation, Boolean.valueOf(async), getParameterMap(req));
			}
			else
			{
				logMessage(resp, "Attemping to run script: " + scriptLocation);
				controller.getDaemon().runScript(scriptLocation);				
			}
		}
		else
		{		
			logMessage(resp, "Invalid request: missing 'name' parameter");
		}
	}
}
