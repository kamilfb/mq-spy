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
package pl.baczkowicz.msgspy.protocols.stomp;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import org.slf4j.LoggerFactory;

import pl.baczkowicz.mqttspy.logger.IMqttMessageLogIO;
import pl.baczkowicz.mqttspy.logger.MqttMessageLogIO;
import pl.baczkowicz.spy.eventbus.IKBus;
import pl.baczkowicz.spy.scripts.BaseScriptManager;
import pl.baczkowicz.spy.scripts.Script;
import pl.baczkowicz.spy.scripts.ScriptRunner;

/**
 * This class manages script creation and execution.
 */
public class StompScriptManager extends BaseScriptManager
{
	/** Connection for which the script will be run. */
	private StompConnection connection;
	
	/**
	 * Creates the script manager.
	 * 
	 * @param eventManager The event manager to be used
	 * @param executor The executor to be used
	 * @param connection The connection for which to run the scripts
	 */
	public StompScriptManager(final IKBus eventBus, final Executor executor, final StompConnection connection)
	{
		super(eventBus, executor);
		this.setConnection(connection);
	}
	
	public void populateEngineVariables(final Script script)
	{
		final StompScriptIO scriptIO = new StompScriptIO(connection, eventBus, script, executor); 
		//script.setScriptIO(scriptIO);
		
		final Map<String, Object> scriptVariables = new HashMap<String, Object>();
		
		// This should be used for general script-related actions
		scriptVariables.put("spy", scriptIO);
		
		scriptVariables.put("logger", LoggerFactory.getLogger(ScriptRunner.class));
		
		// TODO: replace with auditReplay
		final IMqttMessageLogIO mqttMessageLog = new MqttMessageLogIO();
		// Add it to the script IO so that it gets stopped when requested
		script.addTask(mqttMessageLog);			
		scriptVariables.put("messageLog", mqttMessageLog);
		
		putJavaVariablesIntoEngine(script.getScriptEngine(), scriptVariables);
	}

	/**
	 * @return the connection
	 */
	public StompConnection getConnection()
	{
		return connection;
	}

	/**
	 * @param connection the connection to set
	 */
	public void setConnection(StompConnection connection)
	{
		this.connection = connection;
	}
}
