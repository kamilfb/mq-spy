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
package pl.baczkowicz.msgspy.daemon;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.daemon.MqttSpyDaemon;
import pl.baczkowicz.mqttspy.daemon.configuration.generated.DaemonMqttConnectionDetails;
import pl.baczkowicz.mqttspy.scripts.MqttScriptIO;
import pl.baczkowicz.msgspy.daemon.configuration.ConfigurationLoader;
import pl.baczkowicz.msgspy.daemon.generated.configuration.DaemonJmsConnectionDetails;
import pl.baczkowicz.msgspy.daemon.generated.configuration.DaemonKinesisConnectionDetails;
import pl.baczkowicz.msgspy.daemon.generated.configuration.DaemonStompConnectionDetails;
import pl.baczkowicz.msgspy.daemon.generated.configuration.MsgSpyDaemonConfiguration;
import pl.baczkowicz.msgspy.daemon.generated.configuration.ProtocolEnum;
import pl.baczkowicz.msgspy.daemon.jms.JmsConnection;
import pl.baczkowicz.msgspy.daemon.remote.HttpListener;
import pl.baczkowicz.msgspy.daemon.stomp.StompConnection;
import pl.baczkowicz.msgspy.daemon.stomp.StompScriptIO;
import pl.baczkowicz.msgspy.daemon.stomp.StompScriptManager;
import pl.baczkowicz.msgspy.protocols.kinesis.KinesisConnection;
import pl.baczkowicz.msgspy.protocols.kinesis.KinesisScriptIO;
import pl.baczkowicz.msgspy.scripts.JmsScriptIO;
import pl.baczkowicz.msgspy.scripts.JmsScriptManager;
import pl.baczkowicz.spy.configuration.BasePropertyNames;
import pl.baczkowicz.spy.eventbus.IKBus;
import pl.baczkowicz.spy.eventbus.KBus;
import pl.baczkowicz.spy.exceptions.SpyException;
import pl.baczkowicz.spy.exceptions.XMLException;
import pl.baczkowicz.spy.testcases.TestCaseManager;

/**
 * The main class of the daemon.
 */
public class MessageSpyDaemon extends MqttSpyDaemon
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(MessageSpyDaemon.class);
	
	private ConfigurationLoader loader;

	private HttpListener httpListener;
	// private MqttScriptIO scriptIO;
	
	private JmsConnection jmsConnection;

	private StompConnection stompConnection;
	
	private KinesisConnection kinesisConnection;
	
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
		logger.info("### Starting msg-spy-daemon v{}", loader.getFullVersionName());
		logger.info("### If you find it useful, see how you can help at {}", loader.getProperty(BasePropertyNames.DOWNLOAD_URL));
		logger.info("### To get release updates follow @msg_spy on Twitter ");
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
	protected void loadAndRun(final MsgSpyDaemonConfiguration configuration) throws SpyException
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
		else if (ProtocolEnum.KINESIS.equals(loader.getProtocol()))			
		{
			// Retrieve connection details
			final DaemonKinesisConnectionDetails connectionSettings = configuration.getConnectivity().getKinesisConnection();	
			
			configureKinesis(connectionSettings);
			// TODO:
			//runScripts(connectionSettings.getBackgroundScript(), connectionSettings.getTestCases(), connectionSettings.getRunningMode());
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
	
	private void configureKinesis(final DaemonKinesisConnectionDetails connectionSettings)
	{	
		kinesisConnection = new KinesisConnection(); 
		kinesisConnection.configure(connectionSettings);
		
		// scriptManager = new StompScriptManager(eventBus, null, kinesisConnection);
		// kinesisConnection.setScriptManager(scriptManager);
		// testCaseManager = new TestCaseManager(scriptManager);
		
		// kinesisConnection.initialise(true);
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

	public JmsScriptIO jms()
	{
		return new JmsScriptIO(jmsConnection, eventBus, null, null);
	}

	public StompScriptIO stomp()
	{
		return new StompScriptIO(stompConnection, eventBus, null, null);
	}

	public KinesisScriptIO kinesis()
	{
		return new KinesisScriptIO(kinesisConnection, null, null);
	}
	
	public MqttScriptIO mqtt()
	{
		return super.more();
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
