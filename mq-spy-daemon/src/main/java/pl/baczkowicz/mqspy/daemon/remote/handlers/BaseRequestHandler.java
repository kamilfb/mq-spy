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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import pl.baczkowicz.mqspy.daemon.remote.RemoteController;

public abstract class BaseRequestHandler extends HttpServlet
{
	private static final long serialVersionUID = -9204115887954866526L;
	
	protected RemoteController controller;

	public BaseRequestHandler(final RemoteController controller)
	{
		this.controller = controller;
	}
	
	protected Map<String, Object> getParameterMap(final HttpServletRequest req)
	{
		final Map<String, Object> args = new HashMap<>();
		
		args.putAll(req.getParameterMap());
		
		return args;
	}
}
