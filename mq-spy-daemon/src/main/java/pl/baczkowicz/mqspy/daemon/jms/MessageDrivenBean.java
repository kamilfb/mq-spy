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

public class MessageDrivenBean implements MessageListener
{

	private final static Logger logger = LoggerFactory.getLogger(MessageDrivenBean.class);

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
				
				logger.info("Payload: " + new String(data));
			}
			else if (message instanceof TextMessage)
			{
				TextMessage msg = (TextMessage) message;
				logger.info("Consumed text message: " + msg.getText());
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