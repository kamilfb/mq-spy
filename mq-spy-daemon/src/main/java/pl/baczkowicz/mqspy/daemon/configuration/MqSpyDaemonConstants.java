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

package pl.baczkowicz.mqspy.daemon.configuration;

/**
 * Constants for loading the configuration.
 */
public final class MqSpyDaemonConstants
{
	/** Configuration package. */
	public static final String PACKAGE = "pl.baczkowicz.mqspy.daemon.generated.configuration";
	
	/** Configuration schema. */
	public static final String SCHEMA = "/mq-spy-daemon-configuration.xsd";
	
	/** Location of the properties file. */
	public static final String DEFAULT_PROPERTIES_FILE_NAME = "/mq-spy-daemon.properties";
}
