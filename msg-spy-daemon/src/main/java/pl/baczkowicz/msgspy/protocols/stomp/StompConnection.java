package pl.baczkowicz.msgspy.protocols.stomp;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.ser1.stomp.Client;
import net.ser1.stomp.Listener;
import pl.baczkowicz.msgspy.daemon.generated.configuration.DaemonStompConnectionDetails;
import pl.baczkowicz.msgspy.daemon.generated.configuration.ProtocolEnum;
import pl.baczkowicz.spy.common.generated.ReconnectionSettings;
import pl.baczkowicz.spy.common.generated.ScriptDetails;
import pl.baczkowicz.spy.common.generated.ScriptedSubscriptionDetails;
import pl.baczkowicz.spy.connectivity.BaseSubscription;
import pl.baczkowicz.spy.connectivity.ConnectionStatus;
import pl.baczkowicz.spy.connectivity.IConnectionWithReconnect;
import pl.baczkowicz.spy.scripts.BaseScriptManager;
import pl.baczkowicz.spy.scripts.Script;
import pl.baczkowicz.spy.utils.TimeUtils;

public class StompConnection implements Listener, IConnectionWithReconnect, Runnable
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(StompConnection.class);

	private Client connection;

	private BaseScriptManager scriptManager;

	private DaemonStompConnectionDetails connectionSettings;
	
	private boolean connecting = false;

	private long lastConnectionAttemptTimestamp;

	private ConnectionStatus lastConnectionStatus = ConnectionStatus.NOT_CONNECTED;
	
	public void configure(final DaemonStompConnectionDetails connectionSettings)
	{
		this.connectionSettings = connectionSettings;
	}
	
	public void connect()
	{
		if (!connecting)
		{
			connecting = true;
			
			try
			{
				lastConnectionAttemptTimestamp = TimeUtils.getMonotonicTime();
				
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
			
			connecting = false;
		}
	}

	public void stopStomp()
	{
		if (connection != null)
		{
			connection.disconnect();
		}
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

	@SuppressWarnings("rawtypes")
	@Override
	public void message(final Map header, final String body)
	{
		logger.error("Got header: " + header);
		logger.error("Got body: " + body);		
		
		if (!connection.isConnected() || connection.isClosed())
		{
			logger.error("Connection in not active");
		}
	}


	@Override
	public ReconnectionSettings getReconnectionSettings()
	{
		return connectionSettings.getReconnectionSettings();
	}


	@Override
	public ConnectionStatus getConnectionStatus()
	{
		ConnectionStatus connectionStatus = ConnectionStatus.CONNECTED;
		
		if (lastConnectionAttemptTimestamp == 0)
		{
			connectionStatus =  ConnectionStatus.NOT_CONNECTED;
		}
		else if (connecting)
		{
			connectionStatus =  ConnectionStatus.CONNECTING;
		}
		else if (connection == null || !connection.isConnected() || connection.isClosed())
		{
			connectionStatus =  ConnectionStatus.DISCONNECTED;
		}
		
		if (lastConnectionStatus != connectionStatus)
		{
			logger.info("Connection status changed from {} to {}", lastConnectionStatus, connectionStatus);
		}
		
		lastConnectionStatus = connectionStatus;
		
		return connectionStatus;
	}

	@Override
	public long getLastConnectionAttemptTimestamp()
	{
		return lastConnectionAttemptTimestamp;
	}


	@Override
	public String getName()
	{
		return connectionSettings.getServerURI();
	}

	/**
	 * Connector runnable.
	 */
	@Override
	public void run()
	{
		// Disconnect if reconnecting (e.g. because a connection is hung)
		stopStomp();
		
		connect();
	}
}
