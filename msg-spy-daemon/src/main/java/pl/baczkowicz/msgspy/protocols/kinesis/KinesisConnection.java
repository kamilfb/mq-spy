package pl.baczkowicz.msgspy.protocols.kinesis;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.amazonaws.services.kinesis.model.DescribeStreamResult;
import com.amazonaws.services.kinesis.model.GetRecordsRequest;
import com.amazonaws.services.kinesis.model.GetRecordsResult;
import com.amazonaws.services.kinesis.model.GetShardIteratorRequest;
import com.amazonaws.services.kinesis.model.GetShardIteratorResult;
import com.amazonaws.services.kinesis.model.PutRecordRequest;
import com.amazonaws.services.kinesis.model.PutRecordResult;
import com.amazonaws.services.kinesis.model.PutRecordsRequest;
import com.amazonaws.services.kinesis.model.PutRecordsRequestEntry;
import com.amazonaws.services.kinesis.model.PutRecordsResult;
import com.amazonaws.services.kinesis.model.Shard;

import pl.baczkowicz.msgspy.daemon.generated.configuration.DaemonKinesisConnectionDetails;
import pl.baczkowicz.msgspy.daemon.generated.configuration.ProtocolEnum;
import pl.baczkowicz.spy.connectivity.IConnection;

public class KinesisConnection implements IConnection, Runnable
{
	/** AWS Kinesis Client. */
	private AmazonKinesisClient client;
	
	// private BaseScriptManager scriptManager;

	// TODO: make it a synchronized/blocking map
	private Map<String, IKinesisOnMessageHandler> handlers = new HashMap<>();

	private Map<String, Map<String, String>> streamIterators = new HashMap<>();

	private boolean initialised;

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

	public void initialise(final boolean autoStart)
	{
		if (!initialised)
		{
			if (autoStart)
			{
				// TODO: make it a monitored thread
				new Thread(this).start();
			}
			
			initialised = true;
		}
	}

	private void initialiseStream(final String streamName, final String iteratorType)
	{
		final DescribeStreamResult result = client.describeStream(streamName);
		
		streamIterators.put(streamName, new HashMap<String, String>());
				
		for (final Shard shard : result.getStreamDescription().getShards())
		{
			final String shardIterator = initialiseShard(streamName, shard, iteratorType);
			
			streamIterators.get(streamName).put(shard.getShardId(), shardIterator);
		}
	}
	
	private String initialiseShard(final String streamName, final Shard shard, final String iteratorType)
	{
		final GetShardIteratorRequest getShardIteratorRequest = new GetShardIteratorRequest();
		getShardIteratorRequest.setStreamName(streamName);
		getShardIteratorRequest.setShardId(shard.getShardId());
		// e.g. "TRIM_HORIZON"
		getShardIteratorRequest.setShardIteratorType(iteratorType);

		final GetShardIteratorResult getShardIteratorResult = client.getShardIterator(getShardIteratorRequest);
		return getShardIteratorResult.getShardIterator();
	}
	
	public AmazonKinesisClient getClient()
	{
		return client;
	}
//
//	public void setScriptManager(final BaseScriptManager scriptManager)
//	{
//		this.scriptManager = scriptManager;	
//	}
	
	public void subscribe(final String streamName, final String iteratorType, final IKinesisOnMessageHandler handler, boolean autoStart)
	{
		initialiseStream(streamName, iteratorType);
		addStreamHandler(streamName, handler);
		
		initialise(autoStart);
	}
	
	private void addStreamHandler(final String streamName, final IKinesisOnMessageHandler handler)
	{
		handlers.put(streamName, handler);
	}

	private Map<String, GetRecordsResult> getRecords(final String streamName, final int limit)
	{
		final Map<String, GetRecordsResult> results = new HashMap<>();
		
		for (final String shardId : streamIterators.get(streamName).keySet())
		{
			results.put(shardId, getRecords(streamName, shardId, streamIterators.get(streamName).get(shardId), limit));			
		}
		
		return results;
	}
		
	private GetRecordsResult getRecords(final String streamName, final String shardId, final String shardIterator, final int limit)
	{
		final GetRecordsRequest getRecordsRequest = new GetRecordsRequest();
		getRecordsRequest.setShardIterator(shardIterator);
		getRecordsRequest.setLimit(limit);
		
		final GetRecordsResult result = getClient().getRecords(getRecordsRequest);
		
		final String newShardIterator = result.getNextShardIterator();
		streamIterators.get(streamName).put(shardId, newShardIterator);
		
		return result;
	}
	
	public PutRecordResult publish(final String streamName, final String partitionKey, final String prevSequenceNumber, final ByteBuffer payload)
	{
		final PutRecordRequest putRecordRequest = new PutRecordRequest();
		putRecordRequest.setStreamName(streamName);
		putRecordRequest.setData(payload);
		putRecordRequest.setPartitionKey(partitionKey);
		
		if (prevSequenceNumber != null)
		{
			putRecordRequest.setSequenceNumberForOrdering(prevSequenceNumber);
		}
		
		try
		{
			final PutRecordResult putRecordResult = getClient().putRecord(putRecordRequest);
			return putRecordResult;
		}
		catch (Exception e)
		{
			// TODO: log
			return null;
		}
	}
	
	public PutRecordsResult publish(final String streamName, final List<Entry<String, ByteBuffer>> records)
	{
		final PutRecordsRequest putRecordRequests = new PutRecordsRequest();
		putRecordRequests.setStreamName(streamName);
		final Collection<PutRecordsRequestEntry> putRecords = new ArrayList<>();
		
		for (final Entry<String, ByteBuffer> record : records)
		{
			final PutRecordsRequestEntry entry = new PutRecordsRequestEntry();
			entry.setData(record.getValue());
			entry.setPartitionKey(record.getKey());		
			putRecords.add(entry);
		}
		
		putRecordRequests.setRecords(putRecords);
				
		final PutRecordsResult putRecordResult = getClient().putRecords(putRecordRequests);
		
		return putRecordResult;
	}

	@Override
	public void run()
	{
		while (true)
		{
			try
			{
				doGetCycle();
				
				Thread.sleep(500);
			} 
			catch (InterruptedException e)
			{
				break;
			}
		}
	}
	
	public void doGetCycle()
	{
		for (final String streamName : handlers.keySet())
		{
			doGetCycle(streamName, 100);
		}
	}

	public void doGetCycle(final String streamName, final int limit)
	{
		final KinesisHandlerOnMessageContext context = new KinesisHandlerOnMessageContext(getRecords(streamName, limit));
		handlers.get(streamName).onMessage(context);
	}
}
