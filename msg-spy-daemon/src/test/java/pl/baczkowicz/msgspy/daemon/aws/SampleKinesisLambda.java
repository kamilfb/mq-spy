package pl.baczkowicz.msgspy.daemon.aws;

import java.io.IOException;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import com.amazonaws.services.lambda.runtime.events.KinesisEvent;
import com.amazonaws.services.lambda.runtime.events.KinesisEvent.KinesisEventRecord;

public class SampleKinesisLambda implements RequestHandler<KinesisEvent, Object>
{
	@Override
	public Object handleRequest(KinesisEvent input, Context context)
	{
		try
		{
			recordHandler(input);
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		return "All ok :)";
	}

	public void recordHandler(KinesisEvent event) throws IOException
	{
		for (KinesisEventRecord rec : event.getRecords())
		{
			System.out.println(new String(rec.getKinesis().getData().array()));
		}
	}
}
