package pl.baczkowicz.msgspy.daemon.kinesis;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Executor;

import com.amazonaws.services.kinesis.model.PutRecordRequest;
import com.amazonaws.services.kinesis.model.PutRecordResult;
import com.amazonaws.services.kinesis.model.PutRecordsRequest;
import com.amazonaws.services.kinesis.model.PutRecordsRequestEntry;
import com.amazonaws.services.kinesis.model.PutRecordsResult;

import pl.baczkowicz.spy.eventbus.IKBus;
import pl.baczkowicz.spy.scripts.Script;
import pl.baczkowicz.spy.scripts.ScriptIO;

public class KinesisScriptIO extends ScriptIO implements IKinesisScriptIO
{
	private final KinesisConnection connection;
	
	private final IKBus eventBus;
	
	public KinesisScriptIO(final KinesisConnection connection, final IKBus eventBus, final Script script, final Executor executor)
	{
		super(script, executor);
		this.eventBus = eventBus;
		this.connection = connection;
	}
	
	public PutRecordResult publish(final String streamName, final String partitionKey, final byte[] payload)
	{
		return publish(streamName, partitionKey, null, payload);
	}
	
	public PutRecordResult publish(final String streamName, final String partitionKey, final ByteBuffer payload)
	{
		return publish(streamName, partitionKey, null, payload);
	}

	public PutRecordResult publish(final String streamName, final String partitionKey, final String prevSequenceNumber, final byte[] payload)
	{
		return publish(streamName, partitionKey, prevSequenceNumber, ByteBuffer.wrap(payload));
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
			final PutRecordResult putRecordResult = connection.getClient().putRecord(putRecordRequest);
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
				
		final PutRecordsResult putRecordResult = connection.getClient().putRecords(putRecordRequests);
		
		return putRecordResult;
	}
}
