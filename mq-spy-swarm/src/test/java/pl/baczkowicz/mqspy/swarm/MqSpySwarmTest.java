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

import org.junit.Test;

import pl.baczkowicz.spy.exceptions.SpyException;
import pl.baczkowicz.spy.utils.ThreadingUtils;

public class MqSpySwarmTest
{
	// private final static Logger logger = LoggerFactory.getLogger(MqSpySwarmTest.class);
	
	@Test
	public void testBasicMqttSwarmConfiguration() throws SpyException
	{
		final MqSpySwarm swarm = new MqSpySwarm();
		swarm.initialise();
		swarm.loadAndRun("src/test/resources/test_configurations/basic-mqtt-swarm-configuration.xml");
				
		ThreadingUtils.sleep(10000);		
	}	
}
