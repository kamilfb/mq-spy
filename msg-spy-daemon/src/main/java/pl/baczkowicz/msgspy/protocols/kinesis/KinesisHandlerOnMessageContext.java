package pl.baczkowicz.msgspy.protocols.kinesis;

import java.util.Map;

import com.amazonaws.services.kinesis.model.GetRecordsResult;

public class KinesisHandlerOnMessageContext
{
	private final Map<String, GetRecordsResult> result;

	public KinesisHandlerOnMessageContext(final Map<String, GetRecordsResult> results)
	{
		this.result = results;
	}

	public Map<String, GetRecordsResult> getResults()
	{
		return result;
	}
}
