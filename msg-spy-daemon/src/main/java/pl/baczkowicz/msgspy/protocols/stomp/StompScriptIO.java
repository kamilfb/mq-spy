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
package pl.baczkowicz.msgspy.protocols.stomp;

import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.spy.eventbus.IKBus;
import pl.baczkowicz.spy.scripts.Script;
import pl.baczkowicz.spy.scripts.ScriptIO;

/**
 * Implementation of the interface between a script and the rest of the application.
 */
public class StompScriptIO extends ScriptIO
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(StompScriptIO.class);
	
	/** Reference to the connection. */
	private final StompConnection connection;
	
	/**
	 * Creates the PublicationScriptIO.
	 * 
	 * @param connection The connection for which the script is executed
	 * @param eventManager The global event manager
	 * @param script The script itself
	 * @param executor Task executor
	 */
	public StompScriptIO(
			final StompConnection connection, final IKBus eventBus, 
			final Script script, final Executor executor)
	{
		super(script, executor);	
		this.connection = connection;
	}	
	
	public void publish(final String publicationTopic, final String data)
	{
		//publish(publicationTopic, data, 0, false);
	}


	public boolean subscribe(final String topic)
	{
		return false;
//		BaseMqttSubscription subscription = connection.getMqttSubscriptionForTopic(topic);
//		
//		if (subscription == null)
//		{
//			subscription = new BaseMqttSubscription(topic, qos, 1, 1000);
//		}
//		
//		return connection.subscribe(subscription);
	}

	public boolean unsubscribe(final String topic)
	{
//		BaseMqttSubscription subscription = connection.getMqttSubscriptionForTopic(topic);
//		
//		if (subscription != null)
//		{
//			connection.removeSubscription(subscription);
//			return connection.unsubscribe(topic);
//		}
		
		return false;		
	}
	
	// TODO: getAllMessages
}
