package pl.baczkowicz.msgspy.ui.configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.baczkowicz.spy.common.generated.ConnectionGroup;
import pl.baczkowicz.spy.common.generated.FormatterDetails;
import pl.baczkowicz.spy.configuration.BaseConfigurationUtils;
import pl.baczkowicz.spy.connectivity.BaseSubscription;
import pl.baczkowicz.spy.exceptions.XMLException;
import pl.baczkowicz.spy.ui.configuration.BaseConfigurationManager;
import pl.baczkowicz.spy.ui.configuration.ConfiguredConnectionGroupDetails;
import pl.baczkowicz.spy.ui.panes.SpyPerspective;
import pl.baczkowicz.spy.ui.properties.ModifiableConnection;

public class ConfigurationManager extends BaseConfigurationManager
{
	private final static Logger logger = LoggerFactory.getLogger(ConfigurationManager.class);
	
	public ConfigurationManager() throws XMLException
	{
		loadDefaultPropertyFile();
		loadUiPropertyFile();
	}

	@Override
	public boolean saveConfiguration()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Collection<? extends ModifiableConnection> getConnections()
	{
		// TODO Auto-generated method stub
		return new ArrayList<>();
	}

	@Override
	public Collection<? extends ModifiableConnection> getConnections(ConfiguredConnectionGroupDetails group)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<FormatterDetails> getFormatters()
	{
		// TODO Auto-generated method stub
		return new ArrayList<>();
	}

	@Override
	public boolean removeFormatter(FormatterDetails formatter)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int countFormatter(FormatterDetails formatter)
	{
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public void initialiseConfiguration()
	{
		setRootGroup(new ConfiguredConnectionGroupDetails(new ConnectionGroup(
				BaseConfigurationUtils.DEFAULT_GROUP, "All connections", new ArrayList(), new ArrayList()), false));
	}

	@Override
	public boolean loadConfiguration(File file)
	{
		// TODO Auto-generated method stub
		setRootGroup(new ConfiguredConnectionGroupDetails(new ConnectionGroup(
				BaseConfigurationUtils.DEFAULT_GROUP, "All connections", new ArrayList(), new ArrayList()), false));
		
		return true;
	}

	@Override
	public List<ConfiguredConnectionGroupDetails> getOrderedGroups()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void saveUiProperties(double lastWidth, double lastHeight, boolean maximized, SpyPerspective perspective, boolean selected)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateSubscriptionConfiguration(String id, BaseSubscription subscription)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteSubscriptionConfiguration(String id, BaseSubscription subscription)
	{
		// TODO Auto-generated method stub
		
	}
}
