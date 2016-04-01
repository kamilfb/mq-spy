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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;

import pl.baczkowicz.msgspy.daemon.remote.RemoteController;

public class StopScriptRequestHandler extends BaseRequestHandler
{
	private static final long serialVersionUID = 1031333668786590446L;

	public StopScriptRequestHandler(final RemoteController controller)
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
		
		if (scriptName != null)
		{			
			final String scriptLocation = controller.getScriptLocation(scriptName);
			
			if (scriptLocation == null)
			{
				logMessage(resp, "Invalid request: file " + scriptName + " does not exist");
				return;
			}
			
			controller.getDaemon().stopScript(scriptLocation);
		}
		else
		{		
			logMessage(resp, "Invalid request: missing 'name' parameter");
		}
	}
}
