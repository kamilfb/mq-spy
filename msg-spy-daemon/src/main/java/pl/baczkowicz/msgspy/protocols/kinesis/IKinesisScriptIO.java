package pl.baczkowicz.msgspy.protocols.kinesis;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map.Entry;

import com.amazonaws.services.kinesis.model.PutRecordResult;
import com.amazonaws.services.kinesis.model.PutRecordsResult;

import pl.baczkowicz.spy.scripts.IScriptIO;

public interface IKinesisScriptIO extends IScriptIO
{
	PutRecordResult publish(final String streamName, final String partitionKey, final byte[] payload);
	
	PutRecordResult publish(final String streamName, final String partitionKey, final ByteBuffer payload);

	PutRecordResult publish(final String streamName, final String partitionKey, final String prevSequenceNumber, final byte[] payload);
	
	PutRecordResult publish(final String streamName, final String partitionKey, final String prevSequenceNumber, final ByteBuffer payload);

	PutRecordsResult publish(final String streamName, final List<Entry<String, ByteBuffer>> records);
	
	void subscribe(final String streamName, final String iteratorType, final IKinesisOnMessageHandler handler, final boolean autoStart);
	
	void readStreams();
}
