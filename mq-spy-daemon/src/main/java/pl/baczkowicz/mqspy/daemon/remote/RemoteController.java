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
package pl.baczkowicz.mqspy.daemon.remote;

import java.io.File;

import pl.baczkowicz.spy.daemon.IDaemon;

public class RemoteController
{
	private IDaemon daemon;
	
	private String scriptsLocation;

	public RemoteController(final IDaemon daemon)
	{
		this.daemon = daemon;
	}

	public IDaemon getDaemon()
	{
		return daemon;
	}
	
	public String getScriptLocation(final String name)
	{
		if (scriptsLocation != null)
		{
			String scriptName = name;
			if (name.startsWith(System.getProperty("file.separator")))
			{
				scriptName = name.substring(1);
			}
			final String path = scriptsLocation + scriptName;
			
			if (new File(path).exists())
			{
				return path;
			}
			else if (new File(name).exists())
			{
				return name;
			}
			else
			{
				return null;
			}
		}
		else
		{
			if (new File(name).exists())
			{
				return name;
			}
			else
			{
				return null;
			}
		}
	}

	/**
	 * @return the scriptsLocation
	 */
	public String getScriptsLocation()
	{
		return scriptsLocation;
	}

	/**
	 * @param scriptsLocation the scriptsLocation to set
	 */
	public void setScriptsLocation(final String scriptsLocation)
	{
		this.scriptsLocation = scriptsLocation;
		
		if (!scriptsLocation.endsWith(System.getProperty("file.separator")))
		{
			this.scriptsLocation = scriptsLocation + System.getProperty("file.separator");
		}
	}
}
