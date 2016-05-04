package pl.baczkowicz.msgspy.protocols.kinesis.handlers;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent.KinesisEventRecord;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent.Record;

import pl.baczkowicz.msgspy.protocols.kinesis.IKinesisOnMessageHandler;
import pl.baczkowicz.msgspy.protocols.kinesis.KinesisHandlerOnMessageContext;

public class KinesisToLambdaMapper<Response> implements IKinesisOnMessageHandler
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(KinesisToLambdaMapper.class);
	
	private RequestHandler<KinesisEvent, Response> lambda;

	public KinesisToLambdaMapper(final RequestHandler<KinesisEvent, Response> lambda)
	{
		this.lambda = lambda;
	}
	
	@Override
	public void onMessage(final KinesisHandlerOnMessageContext context)
	{
		final KinesisEvent kinesisEvent = new KinesisEvent();
		kinesisEvent.setRecords(new ArrayList<>());
		int recordsAdded = 0;
		
		for (final String key : context.getResults().keySet())
		{
			for (final com.amazonaws.services.kinesis.model.Record record : context.getResults().get(key).getRecords())
			{
				final KinesisEventRecord kinesisEventRecord = new KinesisEventRecord();
				final Record kinesisEventRecordContent = new Record();
				
				kinesisEventRecordContent.setData(record.getData());
				kinesisEventRecordContent.setApproximateArrivalTimestamp(record.getApproximateArrivalTimestamp());
				kinesisEventRecordContent.setSequenceNumber(record.getSequenceNumber());
				kinesisEventRecordContent.setPartitionKey(record.getPartitionKey());
				
				kinesisEventRecord.setKinesis(kinesisEventRecordContent);
				
				kinesisEvent.getRecords().add(kinesisEventRecord);
				recordsAdded++;
			}
		}
		
		if (recordsAdded > 0)
		{
			final Response response = lambda.handleRequest(kinesisEvent, new DummyLambdaKinesisContext());
			logger.info("Lambda response = {}", response);
		}
	}
}
