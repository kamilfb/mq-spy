package pl.baczkowicz.msgspy.ui.connections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.layout.AnchorPane;
import pl.baczkowicz.mqttspy.connectivity.MqttConnectionFactory;
import pl.baczkowicz.spy.ui.configuration.ConfiguredConnectionGroupDetails;
import pl.baczkowicz.spy.ui.connections.IConnectionFactory;
import pl.baczkowicz.spy.ui.panes.SpyPerspective;
import pl.baczkowicz.spy.ui.properties.ConnectionListItemProperties;
import pl.baczkowicz.spy.ui.properties.ModifiableConnection;

public class ConnectionFactory implements IConnectionFactory
{
	public static final String AMQP = "AMQP";
	
	public static final String STOMP = "STOMP";
	
	private MqttConnectionFactory mqttConnectionFactory;

	private Collection<AnchorPane> controllers;

	private Collection<String> enabledProtocols; 
	
	public ConnectionFactory(final MqttConnectionFactory mqttConnectionFactory, final Collection<String> enabledProtocols)
	{
		this.mqttConnectionFactory = mqttConnectionFactory;
		this.enabledProtocols = enabledProtocols;
	}
	
	@Override
	public void populateProtocolCell(TableCell<ConnectionListItemProperties, String> cell, String item)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public ConnectionListItemProperties createConnectionListItemProperties(ModifiableConnection connection)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void findConnections(ConfiguredConnectionGroupDetails parentGroup, List<ModifiableConnection> connections)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public Collection<MenuItem> createMenuItems()
	{
		final Collection<MenuItem> items = new ArrayList<MenuItem>();
		
		for (final String protocol : enabledProtocols)
		{
			items.add(mqttConnectionFactory.createMenuItemForProtocol(protocol));
		}
		
		return items;
	}

	public ModifiableConnection newConnection(String protocol)
	{
		if (MQTT.equals(protocol))
		{
			return MqttConnectionFactory.newConnection();
		}
		
		return null;
	}

	@Override
	public ModifiableConnection duplicateConnection(ModifiableConnection copyFrom)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<AnchorPane> loadControllers(final Object parent)
	{
		controllers = new ArrayList<AnchorPane>();
		
		controllers.add(mqttConnectionFactory.loadController(parent));
//		items.add(loadController(AMQP, parent));
//		items.add(loadController(STOMP, parent));
//		items.add(loadController(KINESIS, parent));
		
		return controllers;
	}
	
	private AnchorPane loadController(String protocol, final Object parent)
	{
//		if (MQTT.equals(protocol))
//		{
//			final FXMLLoader loader = FxmlUtils.createFxmlLoaderForProjectFile("EditConnectionPane.fxml");
//			editConnectionPane = FxmlUtils.loadAnchorPane(loader);
//			
//			editConnectionPaneController = ((EditMqttConnectionController) loader.getController());
//
//			editConnectionPaneController.setConfigurationManager(configurationManager);
//			editConnectionPaneController.setEventBus(eventBus);
//			editConnectionPaneController.setConnectionManager(connectionManager);
//			editConnectionPaneController.setEditConnectionsController((EditConnectionsController) parent);
//			editConnectionPaneController.init();
//			
//			return editConnectionPane;
//		}	
		
		return null;
	}

	@Override
	public void editConnection(ModifiableConnection connection)
	{
		mqttConnectionFactory.editConnection(connection);
		// TODO Auto-generated method stub
		
	}

	@Override
	public void openConnection(ModifiableConnection connection)
	{
		mqttConnectionFactory.openConnection(connection);
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setRecordModifications(boolean record)
	{
		mqttConnectionFactory.setRecordModifications(record);
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPerspective(SpyPerspective perspective)
	{
		mqttConnectionFactory.setPerspective(perspective);
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setEmptyConnectionListMode(boolean empty)
	{
		mqttConnectionFactory.setEmptyConnectionListMode(empty);
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setVisible(boolean groupSelected)
	{
		for (final AnchorPane controller : controllers)
		{
			controller.setVisible(!groupSelected);
		}
	}
}
