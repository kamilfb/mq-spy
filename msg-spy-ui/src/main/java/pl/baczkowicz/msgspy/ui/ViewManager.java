/***********************************************************************************
 * 
 * Copyright (c) 2015 Kamil Baczkowicz
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
import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Screen;
import javafx.stage.Stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.Main;
import pl.baczkowicz.mqttspy.ui.MqttViewManager;
import pl.baczkowicz.mqttspy.ui.events.ShowNewMqttSubscriptionWindowEvent;
import pl.baczkowicz.msgspy.ui.controllers.MsgSpyMainController;
import pl.baczkowicz.spy.ui.BaseViewManager;
import pl.baczkowicz.spy.ui.configuration.BaseConfigurationManager;
import pl.baczkowicz.spy.ui.configuration.UiProperties;
import pl.baczkowicz.spy.ui.events.ConfigurationLoadedEvent;
import pl.baczkowicz.spy.ui.events.ConnectionsChangedEvent;
import pl.baczkowicz.spy.ui.events.LoadConfigurationFileEvent;
import pl.baczkowicz.spy.ui.threading.SimpleRunLaterExecutor;
import pl.baczkowicz.spy.ui.utils.FxmlUtils;
import pl.baczkowicz.spy.ui.utils.ImageUtils;

public class ViewManager extends BaseViewManager
{
	private final static Logger logger = LoggerFactory.getLogger(ViewManager.class);
	
	private MsgSpyMainController mainController;

	private ConnectionViewManager connectionViewManager;
	
	//private ConfigurationManager configurationManager;

	public void init()
	{
		super.init();
		
		eventBus.subscribe(this, this::loadConfigurationFile, LoadConfigurationFileEvent.class, new SimpleRunLaterExecutor());
		eventBus.subscribe(this, MqttViewManager::showNewSubscriptionWindow, ShowNewMqttSubscriptionWindowEvent.class);
	}
	
	public Scene createMainWindow(final Stage primaryStage) throws IOException
	{
		final FXMLLoader loader = FxmlUtils.createFxmlLoaderForProjectFile("MsgSpyMainWindow.fxml");
		
		// Get the associated pane
		AnchorPane pane = (AnchorPane) loader.load();
		
		final Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
		
		// Set scene width, height and style
		final double height = Math.min(UiProperties.getApplicationHeight(configurationManager.getUiPropertyFile()), primaryScreenBounds.getHeight());			
		final double width = Math.min(UiProperties.getApplicationWidth(configurationManager.getUiPropertyFile()), primaryScreenBounds.getWidth());
		
		final Scene scene = new Scene(pane, width, height);			
		scene.getStylesheets().add(Main.class.getResource("/ui/css/application.css").toExternalForm());
		// TODO: add any msg-spy specific styles here...
		
		stylesheets = scene.getStylesheets();
		
		// Get the associated controller
		mainController = (MsgSpyMainController) loader.getController();
		
		mainController.setEventBus(eventBus);
		mainController.setConnectionViewManager(connectionViewManager);
		mainController.setStatisticsManager(statisticsManager);
		mainController.setVersionManager(versionManager);
		mainController.setViewManager(this);
		mainController.setConfigurationManager(configurationManager);
		mainController.updateUiProperties(configurationManager.getUiPropertyFile());		
		
		// connectionViewManager.setMainController(mainController);
		
		// Set the stage's properties
		primaryStage.setScene(scene);	
		primaryStage.setMaximized(UiProperties.getApplicationMaximized(configurationManager.getUiPropertyFile()));			
					
		// Initialise resources in the main controller			
		mainController.setStage(primaryStage);
		mainController.setLastHeight(height);
		mainController.setLastWidth(width);
		mainController.init();
		
	    primaryStage.getIcons().add(ImageUtils.createIcon(configurationManager.getDefaultPropertyFile().getApplicationLogo()).getImage());
	    
	    // setUpKeyHandlers(scene);
		
		return scene;
	}
	
	
	public void clear()
	{
		connectionViewManager.disconnectAndCloseAll();
		
		// Only re-initialise if it has been initialised already
		if (editConnectionsController != null)
		{
			initialiseEditConnectionsWindow(mainController.getStage().getScene().getWindow());
		}	
	}		

	public void loadConfigurationFile(final LoadConfigurationFileEvent event)
	{
		logger.info("Loading configuration file from " + event.getFile().getAbsolutePath());
		
		if (configurationManager.loadConfiguration(event.getFile()))
		{
			clear();
			// controlPanelPaneController.refreshConnectionsStatus();
			eventBus.publish(new ConnectionsChangedEvent());
			
			// Process the connection settings		
			connectionViewManager.autoOpenConnections();					
		}
		
		eventBus.publish(new ConfigurationLoadedEvent());	
	}	
	
	public void loadDefaultConfigurationFile()
	{		
		final File defaultConfigurationFile = BaseConfigurationManager.getDefaultConfigurationFileObject();
		
		logger.info("Default configuration file present (" + defaultConfigurationFile.getAbsolutePath() + ") = " + defaultConfigurationFile.exists());
		
		if (defaultConfigurationFile.exists())
		{
			eventBus.publish(new LoadConfigurationFileEvent(defaultConfigurationFile));
		}
		else
		{
			configurationManager.initialiseConfiguration();
		}
	}
	
	// ************

	/**
	 * Sets the configuration manager.
	 * 
	 * @param configurationManager the configurationManager to set
	 */
//	public void setConfigurationManager(final ConfigurationManager configurationManager)
//	{
//		this.configurationManager = configurationManager;
//	}
	
	public void setConnectionViewManager(final ConnectionViewManager connectionViewManager)
	{
		this.connectionViewManager = connectionViewManager;		
	}

//	public void setStatisticsManager(final StatisticsManager statisticsManager)
//	{
//		this.statisticsManager = statisticsManager;		
//	}
	
//	public MsgSpyMainController getMainController()	
//	{
//		return mainController;
//	}
}
