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
package pl.baczkowicz.msgspy.daemon.jms;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jms.core.JmsTemplate;

import pl.baczkowicz.msgspy.daemon.generated.configuration.DaemonJmsConnectionDetails;
import pl.baczkowicz.spy.common.generated.ProtocolEnum;
import pl.baczkowicz.spy.common.generated.ScriptedSubscriptionDetails;

public class JmsConnection implements IJmsConnection
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(JmsConnection.class);
	
	private Connection connection;
	
	private ConnectionFactory connectionFactory;
	
	private JmsTemplate jmsTemplate;
	
	private Session session;

	private boolean started;
	
	public void configure(final DaemonJmsConnectionDetails connectionSettings)
	{
		if (connectionSettings.getConnectionFactory().getContextFile() != null)
		{
			final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
					connectionSettings.getConnectionFactory().getContextFile().getContextFileLocation());
			
			connectionFactory = context.getBean(
					connectionSettings.getConnectionFactory().getContextFile().getConnectionFactoryBean(), ConnectionFactory.class);
			
			jmsTemplate = context.getBean(
					connectionSettings.getConnectionFactory().getContextFile().getJmsTemplateBean(), JmsTemplate.class);
			
			context.close();
		}				
		
		try
		{
			connection = connectionFactory.createConnection();
			// TODO: set client ID?
			connection.start();
			started = true;
					
			// TODO: parametrise that
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			
			for (final ScriptedSubscriptionDetails subscription : connectionSettings.getSubscription())
			{
				subscribe(subscription.getTopic());
			}
		}
		catch (JMSException e)
		{
			logger.error("JMS error", e);
		}
		
		// TODO: username and password
	}
	
	public void stopJms()
	{
		if (connection != null)
		{
			try
			{
				session.close();
				connection.stop();				
			}
			catch (JMSException e)
			{
				logger.error("JMS error", e);
			}
			// TODO
		}
	}

	@Override
	public ProtocolEnum getProtocol()
	{
		return ProtocolEnum.JMS;
	}

	@Override
	public boolean subscribe(final String topic)
	{
		try
		{
			final Destination destination = session.createTopic(topic);
			final MessageConsumer consumer = session.createConsumer(destination);
			final MessageDrivenBean mdb = new MessageDrivenBean();
			consumer.setMessageListener(mdb);
			return true;
		}
		catch (Exception e)
		{
			logger.error("JMS error", e);
		}
		
		return false;
	}

	@Override
	public boolean unsubscribe(final String topic)
	{
		try
		{
			session.unsubscribe(topic);
			return true;
		}
		catch (Exception e)
		{
			logger.error("JMS error", e);
		}
		
		return false;
	}

	@Override
	public boolean canPublish()
	{
		return started;
	}
}
