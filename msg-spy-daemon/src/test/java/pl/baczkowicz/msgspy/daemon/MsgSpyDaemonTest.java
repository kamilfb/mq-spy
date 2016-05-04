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

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.spy.utils.ThreadingUtils;

public class MsgSpyDaemonTest
{
	private final static Logger logger = LoggerFactory.getLogger(MsgSpyDaemonTest.class);
	
	@Test
	public void testBasicMqttConfiguration()
	{
		final MessageSpyDaemon daemon = new MessageSpyDaemon();
		
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
		final MessageSpyDaemon daemon = new MessageSpyDaemon();
		
		assertTrue(daemon.start("src/test/resources/test_configurations/basic-jms-configuration.xml"));
		
		ThreadingUtils.sleep(15000);
		
		daemon.stop();
	}
}
