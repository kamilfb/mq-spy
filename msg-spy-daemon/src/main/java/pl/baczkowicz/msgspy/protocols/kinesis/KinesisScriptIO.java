package pl.baczkowicz.msgspy.protocols.kinesis;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Executor;

import com.amazonaws.services.kinesis.model.PutRecordResult;
import com.amazonaws.services.kinesis.model.PutRecordsResult;

import pl.baczkowicz.spy.scripts.Script;
import pl.baczkowicz.spy.scripts.ScriptIO;

public class KinesisScriptIO extends ScriptIO implements IKinesisScriptIO
{
	private final KinesisConnection connection;
	
	public KinesisScriptIO(final KinesisConnection connection, final Script script, final Executor executor)
	{
		super(script, executor);
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
		return connection.publish(streamName, partitionKey, prevSequenceNumber, payload);
	}
	
	public PutRecordsResult publish(final String streamName, final List<Entry<String, ByteBuffer>> records)
	{
		return connection.publish(streamName, records);
	}
	
	@Override
	public void subscribe(final String streamName, final String iteratorType, final IKinesisOnMessageHandler handler, final boolean autoStart)
	{
		connection.subscribe(streamName, iteratorType, handler, autoStart);
	}

	@Override
	public void readStreams()
	{
		connection.doGetCycle();
	}
}
