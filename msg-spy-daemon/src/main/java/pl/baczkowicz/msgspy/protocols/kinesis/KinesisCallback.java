package pl.baczkowicz.msgspy.protocols.kinesis;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.kinesis.model.GetRecordsResult;
import com.amazonaws.services.kinesis.model.Record;

import pl.baczkowicz.spy.connectivity.BaseSubscription;
import pl.baczkowicz.spy.messages.BaseMessage;
import pl.baczkowicz.spy.scripts.BaseScriptManager;

public class KinesisCallback implements IKinesisOnMessageHandler
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(KinesisCallback.class);
	
	private BaseSubscription subscription;

	private BaseScriptManager scriptManager;

	public KinesisCallback(final BaseScriptManager scriptManager, final BaseSubscription subscription)
	{
		this.subscription = subscription;
		this.scriptManager = scriptManager;
	}
	
	@Override
	public void onMessage(final KinesisHandlerOnMessageContext context)
	{
		logger.debug("onMessage()");
		
		Map<String, GetRecordsResult> results = context.getResults();
		
		for (final String shardId : results.keySet())
		{			
			logger.debug("Shard ID = {}, behind by = {}ms", shardId, results.get(shardId).getMillisBehindLatest());
			
			for (final Record record : results.get(shardId).getRecords())
			{
				logger.info("Partition key = {}, timestamp = {}, seq number = {}", 
						record.getPartitionKey(), record.getApproximateArrivalTimestamp(), record.getSequenceNumber());
				
				final String data = new String(record.getData().array());
				logger.info("Data = {}", data);
				
				final BaseMessage message = new BaseMessage(subscription.getTopic(), data);
				
				if (subscription.isScriptActive())
				{
					scriptManager.runScriptWithReceivedMessage(subscription.getScript(), message);
				}				
			}
		}
	}
}
