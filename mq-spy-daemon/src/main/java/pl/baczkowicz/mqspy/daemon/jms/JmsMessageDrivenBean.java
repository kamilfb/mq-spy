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
package pl.baczkowicz.mqspy.daemon.jms;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.spy.connectivity.BaseSubscription;
import pl.baczkowicz.spy.messages.BaseMessage;
import pl.baczkowicz.spy.scripts.BaseScriptManager;

public class JmsMessageDrivenBean implements MessageListener
{
	private final static Logger logger = LoggerFactory.getLogger(JmsMessageDrivenBean.class);
	
	private BaseSubscription subscription;
	
	private BaseScriptManager scriptManager;

	public JmsMessageDrivenBean(final BaseScriptManager scriptManager, final BaseSubscription subscription)
	{
		this.subscription = subscription;
		this.scriptManager = scriptManager;
	}

	public void onMessage(final Message message)
	{
		try
		{
			if (message instanceof BytesMessage)
			{
				BytesMessage msg = (BytesMessage) message;
				logger.info("Consumed bytes message: " + msg);
				
				byte data[] = new byte[(int) msg.getBodyLength()];
				msg.readBytes(data);
				
				final String payload = new String(data);
				logger.info("Payload: " + payload);
				
				final BaseMessage baseMessage = new BaseMessage(subscription.getTopic(), payload);
				
				if (subscription.isScriptActive())
				{
					scriptManager.runScriptWithReceivedMessage(subscription.getScript(), baseMessage);
				}
			}
			else if (message instanceof TextMessage)
			{
				TextMessage msg = (TextMessage) message;
				logger.info("Consumed text message: " + msg.getText());
				
				final String payload = msg.getText();
				final BaseMessage baseMessage = new BaseMessage(subscription.getTopic(), payload);
				
				if (subscription.isScriptActive())
				{
					scriptManager.runScriptWithReceivedMessage(subscription.getScript(), baseMessage);
				}
			}
			else				
			{
				logger.info("Consumed unknown message type [{}]: {}", message.getClass().toString(), message);
			}
			
		}
		catch (JMSException e)
		{
			logger.error("JMS error", e);
		}
		catch (Exception e)
		{
			logger.error("JMS error", e);
		}
	}

}