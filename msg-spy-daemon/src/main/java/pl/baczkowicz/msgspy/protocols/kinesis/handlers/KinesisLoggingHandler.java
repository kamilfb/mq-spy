package pl.baczkowicz.msgspy.protocols.kinesis.handlers;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.kinesis.model.GetRecordsResult;
import com.amazonaws.services.kinesis.model.Record;

import pl.baczkowicz.msgspy.protocols.kinesis.IKinesisOnMessageHandler;
import pl.baczkowicz.msgspy.protocols.kinesis.KinesisHandlerOnMessageContext;

public class KinesisLoggingHandler implements IKinesisOnMessageHandler
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(KinesisLoggingHandler.class);
	
	@Override
	public void onMessage(final KinesisHandlerOnMessageContext context)
	{
		logger.debug("onMessage()");
		
		Map<String, GetRecordsResult> results = context.getResults();
		
		for (final String shardId : results.keySet())
		{
//			if (results.get(shardId).getRecords().isEmpty())
//			{
//				continue;
//			}
			
			logger.debug("Shard ID = {}, behind by = {}ms", shardId, results.get(shardId).getMillisBehindLatest());
			
			for (final Record record : results.get(shardId).getRecords())
			{
				logger.info("Partition key = {}, timestamp = {}, seq number = {}", 
						record.getPartitionKey(), record.getApproximateArrivalTimestamp(), record.getSequenceNumber());
				
				logger.info("Data = {}", new String(record.getData().array()));
			}
		}
	}

}
