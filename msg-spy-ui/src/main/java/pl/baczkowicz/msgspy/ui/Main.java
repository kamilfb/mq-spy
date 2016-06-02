/***********************************************************************************
 * 
 * Copyright (c) 2014 Kamil Baczkowicz
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 *
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 *    
 * The Eclipse Distribution License is available at
 *   http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 * 
 *    Kamil Baczkowicz - initial API and implementation and/or initial documentation
 *    
 */
package pl.baczkowicz.msgspy.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import javafx.application.Application;
import javafx.stage.Stage;

import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.connectivity.MqttConnectionFactory;
import pl.baczkowicz.mqttspy.scripts.MqttScriptManager;
import pl.baczkowicz.mqttspy.ui.MqttConnectionViewManager;
import pl.baczkowicz.mqttspy.ui.MqttViewManager;
import pl.baczkowicz.mqttspy.ui.stats.MqttStatsFileIO;
import pl.baczkowicz.msgspy.ui.configuration.ConfigurationManager;
import pl.baczkowicz.msgspy.ui.connections.ConnectionFactory;
import pl.baczkowicz.spy.eventbus.IKBus;
import pl.baczkowicz.spy.eventbus.KBus;
import pl.baczkowicz.spy.ui.configuration.BaseConfigurationManager;
import pl.baczkowicz.spy.ui.configuration.IConfigurationManager;
import pl.baczkowicz.spy.ui.events.LoadConfigurationFileEvent;
import pl.baczkowicz.spy.ui.stats.StatisticsManager;
import pl.baczkowicz.spy.ui.utils.FxmlUtils;
import pl.baczkowicz.spy.ui.versions.VersionManager;

/** 
 * The main class, loading the app.
 */
public class Main extends Application
{	
	/** Name of the parameter supplied on the command line to indicate where to find the configuration file - optional. */
	private final static String CONFIGURATION_PARAMETER_NAME = "configuration";
	
	/** Name of the parameter supplied on the command line to indicate no configuration wanted - optional. */
	private final static String NO_CONFIGURATION_PARAMETER_NAME = "no-configuration";

	private MqttConnectionFactory setUpMqttSpyResources(IKBus eventBus, StatisticsManager statisticsManager, IConfigurationManager configurationManager, Stage primaryStage)
	{
		final MqttViewManager mqttViewManager = new MqttViewManager();
		final MqttConnectionViewManager mqttConnectionViewManager = new MqttConnectionViewManager(eventBus, statisticsManager, configurationManager);
		mqttConnectionViewManager.setParentStage(primaryStage);
		mqttConnectionViewManager.setViewManager(mqttViewManager);
		mqttViewManager.setConnectionManager(mqttConnectionViewManager);
		mqttViewManager.setConfigurationManager(configurationManager);
		mqttViewManager.setEventBus(eventBus);
		mqttViewManager.setStatisticsManager(statisticsManager);
		
		final MqttConnectionFactory mqttConnectionFactory = new MqttConnectionFactory();
		mqttConnectionFactory.setEventBus(eventBus);
		mqttConnectionFactory.setConfigurationManager(configurationManager);
		mqttConnectionFactory.setConnectionManager(mqttConnectionViewManager);
		
		return mqttConnectionFactory;
	}
	
	/**
	 * Starts the application.
	 */
	@Override
	public void start(final Stage primaryStage)
	{			
		BaseConfigurationManager.APPLICATION_NAME = "msg-spy";
		
		final IKBus eventBus = new KBus();
		
		try
		{
			final ConfigurationManager configurationManager = new ConfigurationManager();			
			
			FxmlUtils.setParentClass(getClass());									
			
			// TODO: read version info from the server
			// TODO: read stats from the server (once logged in)
			final StatisticsManager statisticsManager = new StatisticsManager(new MqttStatsFileIO());
			final VersionManager versionManager = new VersionManager(configurationManager.getDefaultPropertyFile());
			
			final ViewManager viewManager = new ViewManager();
									
			final ConnectionViewManager connectionManager = new ConnectionViewManager();										
			// connectionManager.setViewManager(viewManager);			
			
			final MqttConnectionFactory mqttConnectionFactory = setUpMqttSpyResources(eventBus, statisticsManager, configurationManager, primaryStage);
			
			final Collection<String> protocols = new ArrayList<>();
			protocols.add(ConnectionFactory.MQTT);
			protocols.add(ConnectionFactory.AMQP);
			protocols.add(ConnectionFactory.STOMP);
			protocols.add("Kinesis");
			
			final ConnectionFactory connectionFactory = new ConnectionFactory(mqttConnectionFactory, protocols);
//			connectionFactory.setConfigurationManager(configurationManager);
//			connectionFactory.setConnectionManager(connectionManager);
//			connectionFactory.setEventBus(eventBus);

			viewManager.setConnectionFactory(connectionFactory);
			viewManager.setGenericScriptManager(new MqttScriptManager(null, null, null));
			viewManager.setEventBus(eventBus);
			viewManager.setConfigurationManager(configurationManager);
			viewManager.setConnectionViewManager(connectionManager);
			viewManager.setStatisticsManager(statisticsManager);
			viewManager.setVersionManager(versionManager);
			viewManager.setApplication(this);
			viewManager.init();
			
			viewManager.createMainWindow(primaryStage);
			
			// Show the main window
			primaryStage.show();
			
			// Load the config file if specified
			final String noConfig = this.getParameters().getNamed().get(NO_CONFIGURATION_PARAMETER_NAME); 
			final String configurationFileLocation = this.getParameters().getNamed().get(CONFIGURATION_PARAMETER_NAME);
			
			if (noConfig != null)
			{
				// Do nothing - no config wanted
			}
			else if (configurationFileLocation != null)
			{
				eventBus.publish(new LoadConfigurationFileEvent(new File(configurationFileLocation)));				
			}
			else
			{
				// If no configuration parameter is specified, use the user's home directory and the default configuration file name
				viewManager.loadDefaultConfigurationFile();						
			}
		}
		catch (Exception e)
		{
			LoggerFactory.getLogger(Main.class).error("Error while loading the main window", e);
		}
	}

//	private Scene createMainWindow(Stage primaryStage, IKBus eventBus) throws IOException
//	{
//		final FXMLLoader loader = FxmlUtils.createFxmlLoader(FxmlUtils.getParentClass(), "fxml/EmbeddedLogInPane.fxml");
//		
//		// Get the associated pane
//		AnchorPane pane = (AnchorPane) loader.load();
//		
//		final EmbeddedLogInPaneController controller = (EmbeddedLogInPaneController) loader.getController();
//		controller.setEventBus(eventBus);
//		
//		final Scene scene = new Scene(pane);			
//
//		// Set the stage's properties
//		primaryStage.setScene(scene);	
//		
//	    // primaryStage.getIcons().add(ImageUtils.createIcon("msg-spy-logo").getImage());
//	    		
//		return scene;		
//	}
	
//	public void showExternalWebPage(final ShowExternalWebPageEvent event)
//	{
//		this.getHostServices().showDocument(event.getWebpage());		
//	}

	public static void main(String[] args)
	{
		launch(args);
	}
}
