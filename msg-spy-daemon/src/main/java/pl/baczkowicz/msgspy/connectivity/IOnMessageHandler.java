package pl.baczkowicz.msgspy.connectivity;

public interface IOnMessageHandler<T>
{
	void onMessage(final T context);
}
