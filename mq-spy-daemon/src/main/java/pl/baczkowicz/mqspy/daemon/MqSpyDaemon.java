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
package pl.baczkowicz.mqspy.daemon;

import java.io.File;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqspy.daemon.configuration.ConfigurationLoader;
import pl.baczkowicz.mqspy.daemon.generated.configuration.MqSpyDaemonConfiguration;
import pl.baczkowicz.mqspy.daemon.remote.RunScriptRequestHandler;
import pl.baczkowicz.mqttspy.daemon.MqttSpyDaemon;
import pl.baczkowicz.mqttspy.daemon.configuration.generated.DaemonMqttConnectionDetails;
import pl.baczkowicz.spy.common.generated.ProtocolEnum;
import pl.baczkowicz.spy.configuration.PropertyFileLoader;
import pl.baczkowicz.spy.exceptions.SpyException;
import pl.baczkowicz.spy.exceptions.XMLException;

/**
 * The main class of the daemon.
 */
public class MqSpyDaemon extends MqttSpyDaemon
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(MqSpyDaemon.class);
	
	private ConfigurationLoader loader;

	private Server jettyServer;

	// private MqttScriptIO scriptIO;
	
	/**
	 * This is an internal method - initialises the daemon class.
	 * 
	 * @throws XMLException Thrown if cannot instantiate itself
	 */
	public void initialise() throws XMLException
	{
		// TODO:
		loader = new ConfigurationLoader();
		showInfo();
	}
	
	protected void showInfo()
	{
		logger.info("#######################################################");
		logger.info("### Starting mq-spy-daemon v{}", loader.getFullVersionName());
		logger.info("### If you find it useful, see how you can help at {}", loader.getProperty(PropertyFileLoader.DOWNLOAD_URL));
		logger.info("### To get release updates follow @mqtt_spy on Twitter ");
		logger.info("#######################################################");
	}
	
	/**
	 * This is an internal method - requires "initialise" to be called first.
	 * 
	 * @param configurationFile Location of the configuration file
	 * @throws SpyException Thrown if cannot initialise
	 */
	public void loadAndRun(final String configurationFile) throws SpyException
	{
		// Load the configuration
		loader.loadConfiguration(new File(configurationFile));
		
		loadAndRun(loader.getConfiguration());
	}
	
	/**
	 * This is an internal method - requires "initialise" to be called first.
	 * 
	 * @param configuration Configuration object
	 * @throws SpyException Thrown if cannot initialise
	 */
	protected void loadAndRun(final MqSpyDaemonConfiguration configuration) throws SpyException
	{			
		// Set up MQTT
		if (ProtocolEnum.MQTT.equals(loader.getProtocol()))
		{
			// Retrieve connection details
			final DaemonMqttConnectionDetails connectionSettings = configuration.getConnectivity().getMqttConnection();

			configureMqtt(connectionSettings);
			runScripts(connectionSettings.getBackgroundScript(), connectionSettings.getTestCases(), connectionSettings.getRunningMode());
		}
		
		// Set up Remote Control
		configureRemoteControl(configuration);
	}
	
	private void configureRemoteControl(final MqSpyDaemonConfiguration configuration)
	{
		if (configuration.getRemoteControl() != null && configuration.getRemoteControl().getHttpListener() != null)
		{
			try
			{
				jettyServer = new Server(configuration.getRemoteControl().getHttpListener().getPort());

				final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		        context.setContextPath("/");
		        
		        final DefaultServlet defaultServlet = new DefaultServlet();
		        final ServletHolder holderPwd = new ServletHolder("default", defaultServlet);
		        holderPwd.setInitParameter("resourceBase", "./src/main/webapp/");
		        context.addServlet(holderPwd, "/*");
		        
		        context.addServlet(new ServletHolder(new RunScriptRequestHandler(this)), "/runScript");
		        
		        jettyServer.setHandler(context);
		        jettyServer.start();
			}
			catch (Exception e)
			{
				logger.error("Cannot start the HTTP listener on " + configuration.getRemoteControl().getHttpListener().getPort(), e);
			}			
		}
	}
	
	protected boolean canPublish()
	{
		if (ProtocolEnum.MQTT.equals(loader.getProtocol()))
		{
			return super.mqttConnection.canPublish();
		}

		return false;
	}
	
	/**
	 *  Tries to stop all running threads (apart from scripts) and close the connection.
	 */
	protected void waitAndStop()
	{
		waitForScripts();
		
		if (ProtocolEnum.MQTT.equals(loader.getProtocol()))
		{
			super.stopMqtt();
		}
		
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
		
		displayGoodbyeMessage();
	}	
	
	// TODO
	/**
	 * This exposes additional methods, e.g. publish, subscribe, unsubscribe.
	 *  
	 * @return The Script IO with the extra methods
	 */
//	public MqttScriptIO more()
//	{
//		return scriptIO;
//	}
}
