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
package pl.baczkowicz.mqspy.swarm;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqspy.daemon.MqSpyDaemon;
import pl.baczkowicz.mqspy.swarm.configuration.ConfigurationLoader;
import pl.baczkowicz.mqspy.swarm.generated.configuration.MqSpySwarmConfiguration;
import pl.baczkowicz.mqspy.swarm.generated.configuration.SwarmGroup;
import pl.baczkowicz.spy.configuration.PropertyFileLoader;
import pl.baczkowicz.spy.exceptions.SpyException;
import pl.baczkowicz.spy.exceptions.XMLException;

/**
 * The main class of the daemon.
 */
public class MqSpySwarm
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(MqSpySwarm.class);
	
	private ConfigurationLoader loader;
	
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
		logger.info("### Starting mq-spy-swarm v{}", loader.getFullVersionName());
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
	protected void loadAndRun(final MqSpySwarmConfiguration configuration) throws SpyException
	{			
		for (final SwarmGroup group : configuration.getSwarmGroup())
		{			
			for (int i = 1; i <= group.getDaemonInstances(); i++)
			{
				final int instance = i;
				
				new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						final MqSpyDaemon daemon = new MqSpyDaemon();
						
						try
						{
							daemon.initialise(false);
							daemon.loadAndRun(group.getTemplateConfiguration(), group.getGroupId(), instance);			
						}
						catch (SpyException e)
						{
							logger.error("Cannot load daemon group/instance {}:{}", group.getGroupId(), instance);
						}
									
					}					
				}).start();				
			}
		}
	}
}
