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

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqspy.daemon.MqSpyDaemon;
import pl.baczkowicz.mqspy.daemon.generated.configuration.MqSpyDaemonConfiguration;
import pl.baczkowicz.mqspy.daemon.remote.handlers.RunScriptFunctionRequestHandler;
import pl.baczkowicz.mqspy.daemon.remote.handlers.RunScriptRequestHandler;
import pl.baczkowicz.mqspy.daemon.remote.handlers.SetScriptsLocationRequestHandler;
import pl.baczkowicz.mqspy.daemon.remote.handlers.StopRequestHandler;

public class HttpListener
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(HttpListener.class);
		
	private Server jettyServer;
	
	public void configureRemoteControl(final MqSpyDaemonConfiguration configuration, final MqSpyDaemon daemon)
	{
		if (configuration.getRemoteControl() != null && configuration.getRemoteControl().getHttpListener() != null)
		{
			try
			{
				final RemoteController controller = new RemoteController(daemon);
				
				jettyServer = new Server(configuration.getRemoteControl().getHttpListener().getPort());

				final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		        context.setContextPath("/");
		        
		        final DefaultServlet defaultServlet = new DefaultServlet();
		        final ServletHolder holderPwd = new ServletHolder("default", defaultServlet);
		        holderPwd.setInitParameter("resourceBase", "./src/main/webapp/");
		        context.addServlet(holderPwd, "/*");
		        
		        context.addServlet(new ServletHolder(new RunScriptRequestHandler(controller)), "/runScript");
		        context.addServlet(new ServletHolder(new RunScriptFunctionRequestHandler(controller)), "/runScriptFunction");
		        context.addServlet(new ServletHolder(new SetScriptsLocationRequestHandler(controller)), "/setScriptsLocation");		        
		        context.addServlet(new ServletHolder(new StopRequestHandler(controller)), "/stop");
		        
		        jettyServer.setHandler(context);
		        jettyServer.start();
			}
			catch (Exception e)
			{
				logger.error("Cannot start the HTTP listener on " + configuration.getRemoteControl().getHttpListener().getPort(), e);
			}			
		}
	}
	
	public void stop()
	{
		if (jettyServer != null)			
		{
			try
			{
				jettyServer.stop();
			}
			catch (Exception e)
			{
				logger.error("Can't stop the HTTP server", e);
			}
		}
	}
}
