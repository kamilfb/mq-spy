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
package pl.baczkowicz.mqspy.connectivity.jms;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import pl.baczkowicz.mqspy.daemon.generated.configuration.DaemonJmsConnectionDetails;
import pl.baczkowicz.mqspy.daemon.generated.configuration.JmsContextFile;
import pl.baczkowicz.mqspy.daemon.jms.JmsMessageDrivenBean;
import pl.baczkowicz.mqspy.daemon.stomp.StompCallback;
import pl.baczkowicz.spy.common.generated.ProtocolEnum;
import pl.baczkowicz.spy.common.generated.ScriptDetails;
import pl.baczkowicz.spy.common.generated.ScriptedSubscriptionDetails;
import pl.baczkowicz.spy.connectivity.BaseSubscription;
import pl.baczkowicz.spy.scripts.BaseScriptManager;
import pl.baczkowicz.spy.scripts.Script;

public class JmsConnection implements IJmsConnection
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(JmsConnection.class);
	
	private Connection connection;
	
	private ConnectionFactory connectionFactory;
	
	private JmsTemplate jmsTemplate;
	
	private Session session;

	private boolean started;

	private BaseScriptManager scriptManager;
	
	public void configure(final DaemonJmsConnectionDetails connectionSettings)
	{
		try
		{
			if (connectionSettings.getConnectionFactory().getContextFile() != null)
			{
				final JmsContextFile contextFile = connectionSettings.getConnectionFactory().getContextFile();
				
				final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(contextFile.getContextFileLocation());
				
				connectionFactory = context.getBean(contextFile.getConnectionFactoryBean(), ConnectionFactory.class);
				
				if (contextFile.getJmsTemplateBean() != null)
				{
					jmsTemplate = context.getBean(contextFile.getJmsTemplateBean(), JmsTemplate.class);
				}
				else
				{
					jmsTemplate = new JmsTemplate(connectionFactory);
				}
				
				context.close();
				
				connection = connectionFactory.createConnection();
				connection.start();
				started = true;
				
				// TODO: optionally create all topics in spring?
			}			
			else if (connectionSettings.getConnectionFactory().getScriptFile() != null)
			{
				// TODO: create a connection factory
				
				// TODO: create connection (optional)
				connection = connectionFactory.createConnection();
				connection.start();
				started = true;
				
				// TODO: create jms template (optional)
				
				// TODO: create a session (optional)?
			}
					
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
		return subscribe(new BaseSubscription(topic));
	}
	
	public boolean subscribe(final BaseSubscription subscription)
	{
		try
		{
			logger.info("Subscribing...");
			
			final Destination destination = session.createTopic(subscription.getTopic());
			final MessageConsumer consumer = session.createConsumer(destination);
			final JmsMessageDrivenBean mdb = new JmsMessageDrivenBean(scriptManager, subscription);
			consumer.setMessageListener(mdb);
			
			//connection.subscribe(subscription.getTopic(), new StompCallback(scriptManager, subscription));
			logger.info("Subscribed to " + subscription.getTopic());
			
			if (subscription.getDetails() != null 
					&& subscription.getDetails().getScriptFile() != null 
					&& !subscription.getDetails().getScriptFile().isEmpty())
			{
				final Script script = scriptManager.addScript(new ScriptDetails(false, false, subscription.getDetails().getScriptFile()));
				subscription.setScript(script);
				scriptManager.runScript(script, false);
				
				if (scriptManager.invokeBefore(script))					
				{
					subscription.setScriptActive(true);
				}
			}
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

	@Override
	public void publish(final String publicationTopic, final String data)
	{
		jmsTemplate.send(publicationTopic, new MessageCreator()
		{			
			@Override
			public Message createMessage(final Session session) throws JMSException
			{
				return session.createTextMessage(data);
			}
		});
	}
	
	public void setScriptManager(final BaseScriptManager scriptManager)
	{
		this.scriptManager = scriptManager;		
	}
}
