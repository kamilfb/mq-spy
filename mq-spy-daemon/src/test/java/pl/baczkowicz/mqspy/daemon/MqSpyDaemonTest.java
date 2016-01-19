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

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.spy.utils.ThreadingUtils;

public class MqSpyDaemonTest
{
	private final static Logger logger = LoggerFactory.getLogger(MqSpyDaemonTest.class);
	
	@Test
	public void testBasicMqttConfiguration()
	{
		final MqSpyDaemon daemon = new MqSpyDaemon();
		
		assertTrue(daemon.start("src/test/resources/test_configurations/basic-mqtt-configuration.xml"));
		
		while (!daemon.canPublish())
		{
			logger.debug("Client not connected yet - can't start test cases... [waiting another 1000ms]");
			ThreadingUtils.sleep(1000);
		}
		
		ThreadingUtils.sleep(60000);
		
		daemon.stop();
	}
	
	@Test
	public void testBasicJmsConfiguration()
	{
		final MqSpyDaemon daemon = new MqSpyDaemon();
		
		assertTrue(daemon.start("src/test/resources/test_configurations/basic-jms-configuration.xml"));
		
//		while (!daemon.canPublish())
//		{
//			logger.debug("Client not connected yet - can't start test cases... [waiting another 1000ms]");
//			ThreadingUtils.sleep(1000);
//		}
		
		ThreadingUtils.sleep(15000);
		
		daemon.stop();
	}
	
	@Test
	public void testNrDataFeedsStompConfiguration()
	{
		final MqSpyDaemon daemon = new MqSpyDaemon();
		
		assertTrue(daemon.start("/home/kamil/Programming/nr-data-feeds-context.xml"));
		
		while (!daemon.canPublish())
		{
			logger.debug("Client not connected yet [waiting another 1000ms]");
			ThreadingUtils.sleep(1000);
		}
		
		ThreadingUtils.sleep(120000);
		
		daemon.stop();
	}
}
