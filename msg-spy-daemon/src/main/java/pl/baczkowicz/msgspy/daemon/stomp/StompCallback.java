package pl.baczkowicz.msgspy.daemon.stomp;

import java.util.Map;

import net.ser1.stomp.Listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.spy.connectivity.BaseSubscription;
import pl.baczkowicz.spy.messages.BaseMessage;
import pl.baczkowicz.spy.scripts.BaseScriptManager;

public class StompCallback implements Listener
{
	/** Diagnostic logger. */
	private final static Logger logger = LoggerFactory.getLogger(StompCallback.class);
	
	private BaseSubscription subscription;

	private BaseScriptManager scriptManager;

	public StompCallback(final BaseScriptManager scriptManager, final BaseSubscription subscription)
	{
		this.subscription = subscription;
		this.scriptManager = scriptManager;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public void message(final Map header, final String body)
	{
		logger.debug("STOMP message received; header = {}, body = {}", header, body);
		
		final BaseMessage message = new BaseMessage(subscription.getTopic(), body);
		
		if (subscription.isScriptActive())
		{
			scriptManager.runScriptWithReceivedMessage(subscription.getScript(), message);
		}
	}
}
