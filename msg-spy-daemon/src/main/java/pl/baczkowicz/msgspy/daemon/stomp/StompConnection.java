package pl.baczkowicz.msgspy.daemon.stomp;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.ser1.stomp.Client;
import net.ser1.stomp.Listener;
import pl.baczkowicz.msgspy.daemon.generated.configuration.DaemonStompConnectionDetails;
import pl.baczkowicz.msgspy.daemon.generated.configuration.ProtocolEnum;
import pl.baczkowicz.spy.common.generated.ScriptDetails;
import pl.baczkowicz.spy.common.generated.ScriptedSubscriptionDetails;
import pl.baczkowicz.spy.connectivity.BaseSubscription;
import pl.baczkowicz.spy.connectivity.IConnection;
import pl.baczkowicz.spy.scripts.BaseScriptManager;
import pl.baczkowicz.spy.scripts.Script;

public class StompConnection implements IConnection, Listener
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

	public boolean unsubscribe(String topic)
	{
		// TODO Auto-generated method stub
		return false;
	}

	public void setScriptManager(final BaseScriptManager scriptManager)
	{
		this.scriptManager = scriptManager;		
	}


	@Override
	public void message(final Map header, final String body)
	{
		logger.error("Got header: " + header);
		logger.error("Got body: " + body);		
	}
}
