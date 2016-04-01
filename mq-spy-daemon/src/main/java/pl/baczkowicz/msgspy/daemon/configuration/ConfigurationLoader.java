/***********************************************************************************
 * 
 * Copyright (c) 2014 Kamil Baczkowicz
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
package pl.baczkowicz.msgspy.daemon.configuration;

import java.io.File;
import java.io.FileNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.daemon.configuration.MqttSpyDaemonConfigLoader;
import pl.baczkowicz.mqttspy.daemon.configuration.MqttSpyDaemonConstants;
import pl.baczkowicz.mqttspy.daemon.configuration.generated.DaemonMqttConnectionDetails;
import pl.baczkowicz.mqttspy.utils.MqttConfigurationUtils;
import pl.baczkowicz.msgspy.daemon.generated.configuration.DaemonJmsConnectionDetails;
import pl.baczkowicz.msgspy.daemon.generated.configuration.DaemonKinesisConnectionDetails;
import pl.baczkowicz.msgspy.daemon.generated.configuration.DaemonStompConnectionDetails;
import pl.baczkowicz.msgspy.daemon.generated.configuration.MsgSpyDaemonConfiguration;
import pl.baczkowicz.spy.common.generated.ProtocolEnum;
import pl.baczkowicz.spy.configuration.PropertyFileLoader;
import pl.baczkowicz.spy.exceptions.XMLException;
import pl.baczkowicz.spy.xml.XMLParser;

/**
 * Helper class for loading the daemon's configuration.
 */
public class ConfigurationLoader extends PropertyFileLoader
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(ConfigurationLoader.class);
	
	/** XML config parser. */
	private final XMLParser parser;

	/** Daemon's configuration (once parsed). */
	private MsgSpyDaemonConfiguration configuration;
	
	private ProtocolEnum protocol;
	
	/**
	 * Creates the loader. 
	 * 
	 * @throws XMLException Thrown if cannot read the properties file or instantiate the config parser
	 */
	public ConfigurationLoader() throws XMLException
	{
		super();
		readFromClassPath(MsgSpyDaemonConstants.DEFAULT_PROPERTIES_FILE_NAME);
		
		this.parser = new XMLParser(MsgSpyDaemonConstants.PACKAGE, 
				new String[] {	MqttConfigurationUtils.SPY_COMMON_SCHEMA, 
								MqttConfigurationUtils.MQTT_COMMON_SCHEMA,
								MqttSpyDaemonConstants.SCHEMA,
								MsgSpyDaemonConstants.SCHEMA});					
	}
	
	/**
	 * Loads configuration from the given file.
	 * 
	 * @param file The file to load from
	 * 
	 * @return True if all OK
	 */
	public boolean loadConfiguration(final File file)
	{
		try
		{
			configuration = (MsgSpyDaemonConfiguration) parser.loadFromFile(file);	
			populateDefaults();
			return true;
		}
		catch (XMLException e)
		{							
			logger.error("Cannot process the configuration file at " + file.getAbsolutePath(), e);
		}
		catch (FileNotFoundException e)
		{
			logger.error("Cannot read the configuration file from " + file.getAbsolutePath(), e);
		}
		
		return false;
	}

	/**
	 * Populates the connection configuration with default values.
	 */
	private void populateDefaults()
	{
		// Check if MQTT has been configured
		final DaemonMqttConnectionDetails mqttConnection = configuration.getConnectivity().getMqttConnection();
		
		// Check if JMS has been configured
		final DaemonJmsConnectionDetails jmsConnection = configuration.getConnectivity().getJmsConnection();
		
		// Check if STOMP has been configured
		final DaemonStompConnectionDetails stompConnection = configuration.getConnectivity().getStompConnection();
		
		// Check if KINESIS has been configured
		final DaemonKinesisConnectionDetails kinesisConnection = configuration.getConnectivity().getKinesisConnection();
				
		if (mqttConnection != null)		
		{
			protocol = ProtocolEnum.MQTT;
			MqttConfigurationUtils.populateMessageLogDefaults(mqttConnection.getMessageLog());
			MqttSpyDaemonConfigLoader.populateDaemonDefaults(mqttConnection.getBackgroundScript());
			MqttSpyDaemonConfigLoader.generateClientIdIfMissing(mqttConnection);
		}				
		else if (jmsConnection != null)
		{
			protocol = ProtocolEnum.JMS;
		}
		else if (stompConnection != null)
		{
			protocol = ProtocolEnum.STOMP;
		}
		else if (kinesisConnection != null)
		{
			protocol = ProtocolEnum.KINESIS;
		}
	}

	/**
	 * Gets the configuration value.
	 * 
	 * @return The daemon's configuration
	 */
	public MsgSpyDaemonConfiguration getConfiguration()
	{
		return configuration;
	}
	
	public ProtocolEnum getProtocol()
	{
		return protocol;
	}
}
