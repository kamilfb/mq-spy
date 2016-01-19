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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqspy.daemon.configuration.ConfigurationLoader;
import pl.baczkowicz.mqspy.daemon.generated.configuration.DaemonJmsConnectionDetails;
import pl.baczkowicz.mqspy.daemon.generated.configuration.DaemonStompConnectionDetails;
import pl.baczkowicz.mqspy.daemon.generated.configuration.MqSpyDaemonConfiguration;
import pl.baczkowicz.mqspy.daemon.jms.JmsConnection;
import pl.baczkowicz.mqspy.daemon.remote.HttpListener;
import pl.baczkowicz.mqspy.daemon.stomp.StompConnection;
import pl.baczkowicz.mqspy.daemon.stomp.StompScriptManager;
import pl.baczkowicz.mqspy.scripts.JmsScriptManager;
import pl.baczkowicz.mqttspy.daemon.MqttSpyDaemon;
import pl.baczkowicz.mqttspy.daemon.configuration.generated.DaemonMqttConnectionDetails;
import pl.baczkowicz.spy.common.generated.ProtocolEnum;
import pl.baczkowicz.spy.configuration.PropertyFileLoader;
import pl.baczkowicz.spy.eventbus.IKBus;
import pl.baczkowicz.spy.eventbus.KBus;
import pl.baczkowicz.spy.exceptions.SpyException;
import pl.baczkowicz.spy.exceptions.XMLException;
import pl.baczkowicz.spy.testcases.TestCaseManager;

/**
 * The main class of the daemon.
 */
public class MqSpyDaemon extends MqttSpyDaemon
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(MqSpyDaemon.class);
	
	private ConfigurationLoader loader;

	private HttpListener httpListener;
	// private MqttScriptIO scriptIO;
	
	private JmsConnection jmsConnection;

	private StompConnection stompConnection;
	
	private IKBus eventBus = new KBus();
	
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
		else if (ProtocolEnum.JMS.equals(loader.getProtocol()))
		{
			// Retrieve connection details
			final DaemonJmsConnectionDetails connectionSettings = configuration.getConnectivity().getJmsConnection();	
			
			configureJms(connectionSettings);
			runScripts(connectionSettings.getBackgroundScript(), connectionSettings.getTestCases(), connectionSettings.getRunningMode());
		}
		else if (ProtocolEnum.STOMP.equals(loader.getProtocol()))			
		{
			// Retrieve connection details
			final DaemonStompConnectionDetails connectionSettings = configuration.getConnectivity().getStompConnection();	
			
			configureStomp(connectionSettings);
			runScripts(connectionSettings.getBackgroundScript(), connectionSettings.getTestCases(), connectionSettings.getRunningMode());
		}
	
		// Set up Remote Control
		httpListener = new HttpListener();
		httpListener.configureRemoteControl(configuration, this);
	}
	
	private void configureJms(final DaemonJmsConnectionDetails connectionSettings)
	{	
		jmsConnection = new JmsConnection(); 
		jmsConnection.configure(connectionSettings);
		
		// TODO: pass in a connection
		scriptManager = new JmsScriptManager(eventBus, null, jmsConnection);
		testCaseManager = new TestCaseManager(scriptManager);
	}
	
	private void configureStomp(final DaemonStompConnectionDetails connectionSettings)
	{	
		stompConnection = new StompConnection(); 
		stompConnection.configure(connectionSettings);
		
		scriptManager = new StompScriptManager(eventBus, null, stompConnection);
		stompConnection.setScriptManager(scriptManager);
		
		testCaseManager = new TestCaseManager(scriptManager);
		
		stompConnection.connect();
	}

	public boolean canPublish()
	{
		if (ProtocolEnum.MQTT.equals(loader.getProtocol()))
		{
			return super.mqttConnection.canPublish();
		}
		else if (ProtocolEnum.JMS.equals(loader.getProtocol()))
		{
			// TODO
		}
		else if (ProtocolEnum.STOMP.equals(loader.getProtocol()))			
		{
			return stompConnection.canPublish();
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
		else if (ProtocolEnum.JMS.equals(loader.getProtocol()))
		{
			jmsConnection.stopJms();
		}
		else if (ProtocolEnum.STOMP.equals(loader.getProtocol()))
		{
			stompConnection.stopStomp();
		}
		
		httpListener.stop();		
		
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
