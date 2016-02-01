package pl.baczkowicz.mqspy.connectivity.stomp;

import java.util.Map;

import net.ser1.stomp.Client;
import net.ser1.stomp.Listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqspy.daemon.generated.configuration.DaemonStompConnectionDetails;
import pl.baczkowicz.mqspy.daemon.stomp.StompCallback;
import pl.baczkowicz.spy.common.generated.ProtocolEnum;
import pl.baczkowicz.spy.common.generated.ScriptDetails;
import pl.baczkowicz.spy.common.generated.ScriptedSubscriptionDetails;
import pl.baczkowicz.spy.connectivity.BaseSubscription;
import pl.baczkowicz.spy.scripts.BaseScriptManager;
import pl.baczkowicz.spy.scripts.Script;

public class StompConnection implements IStompConnection, Listener
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(StompConnection.class);

	private Client connection;

	private BaseScriptManager scriptManager;

	private DaemonStompConnectionDetails connectionSettings;
	
	public void configure(final DaemonStompConnectionDetails connectionSettings)
	{
		this.connectionSettings = connectionSettings;
	}
	
	public void connect()
	{
		try
		{
			connection = new Client(connectionSettings.getServerURI(),
									connectionSettings.getPort(), 
									connectionSettings.getUserCredentials().getUsername(),
									connectionSettings.getUserCredentials().getPassword());

			connection.addErrorListener(this);
			
			if (connection.isConnected())
			{
				logger.info("Connected to "
						+ connectionSettings.getServerURI() + ":"
						+ connectionSettings.getPort());
			}
			else
			{
				logger.error("Could not connect");
				return;
			}		
			
			for (final ScriptedSubscriptionDetails subscriptionDetails : connectionSettings.getSubscription())
			{
				final BaseSubscription subscription = new BaseSubscription(subscriptionDetails.getTopic());
				subscription.setDetails(subscriptionDetails);
				subscribe(subscription);
			}
		}
		catch (Exception e)
		{
			logger.error("STOMP error", e);
		}		
	}

	public void stopStomp()
	{
		connection.disconnect();
	}

	@Override
	public ProtocolEnum getProtocol()
	{
		return ProtocolEnum.STOMP;
	}

	@Override
	public boolean canPublish()
	{
		if (connection == null)
		{
			return false;
		}
		
		return connection.isConnected();
	}

	@Override
	public boolean subscribe(String topic)
	{
		return subscribe(new BaseSubscription(topic));
	}
	
	public boolean subscribe(final BaseSubscription subscription)
	{
		logger.info("Subscribing...");
		connection.subscribe(subscription.getTopic(), new StompCallback(scriptManager, subscription));
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

	public boolean unsubscribe(final String topic)
	{
		logger.info("Unsubscribing...");
		connection.unsubscribe(topic);
		logger.info("Unsubscribed from " + topic);
		
		return true;
	}

	public void setScriptManager(final BaseScriptManager scriptManager)
	{
		this.scriptManager = scriptManager;		
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void message(final Map header, final String body)
	{
		// This method is for error handling only - see addErrorListener
		logger.error("Got header: " + header);
		logger.error("Got body: " + body);		
	}

	@Override
	public void publish(final String publicationTopic, final String data)
	{
		logger.info("Publishing to {}: {}", publicationTopic, data);
		connection.send(publicationTopic, data);		
	}
}
