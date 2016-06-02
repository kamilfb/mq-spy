package pl.baczkowicz.msgspy.ui;

import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.scene.control.Tab;
import pl.baczkowicz.mqttspy.configuration.ConfiguredMqttConnectionDetails;
import pl.baczkowicz.spy.exceptions.ConfigurationException;
import pl.baczkowicz.spy.ui.IConnectionViewManager;
import pl.baczkowicz.spy.ui.controllers.IConnectionController;

public class ConnectionViewManager implements IConnectionViewManager
{
	private final static Logger logger = LoggerFactory.getLogger(ConnectionViewManager.class);
	
	public IConnectionController getControllerForTab(final Tab selectedTab)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void disconnectAll()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public Collection<? extends IConnectionController> getConnectionControllers()
	{
		// TODO Auto-generated method stub
		return new ArrayList<>();
	}

	@Override
	public void disconnectAndCloseAll()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void autoOpenConnections()
	{
//		for (final ConfiguredMqttConnectionDetails connection : configurationManager.getConnections())
//		{
//			if (connection.isAutoOpen() != null && connection.isAutoOpen())
//			{					
//				try
//				{
//					openConnection(connection);
//				}
//				catch (ConfigurationException e)
//				{
//					// TODO: show warning dialog for invalid
//					logger.error("Cannot open conection {}", connection.getName(), e);
//				}
//			}
//		}		
	}

}
