package pl.baczkowicz.msgspy.daemon.kinesis;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.kinesis.AmazonKinesisClient;

import pl.baczkowicz.msgspy.daemon.generated.configuration.DaemonKinesisConnectionDetails;
import pl.baczkowicz.msgspy.daemon.generated.configuration.ProtocolEnum;
import pl.baczkowicz.spy.connectivity.IConnection;
import pl.baczkowicz.spy.scripts.BaseScriptManager;

public class KinesisConnection implements IConnection
{
	/** AWS Kinesis Client. */
	private AmazonKinesisClient client;
	
	private BaseScriptManager scriptManager;

	public ProtocolEnum getProtocol()
	{
		return ProtocolEnum.KINESIS;
	}

	@Override
	public boolean canPublish()
	{
		return false;
	}

	public void configure(final DaemonKinesisConnectionDetails connectionSettings)
	{
		client = new AmazonKinesisClient(new ProfileCredentialsProvider());
		client.setEndpoint(connectionSettings.getEndpoint(), "kinesis", connectionSettings.getRegionId());
	}
	
	public AmazonKinesisClient getClient()
	{
		return client;
	}

	public void setScriptManager(final BaseScriptManager scriptManager)
	{
		this.scriptManager = scriptManager;	
	}
}
