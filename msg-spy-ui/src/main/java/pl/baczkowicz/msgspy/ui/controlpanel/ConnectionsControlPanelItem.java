package pl.baczkowicz.msgspy.ui.controlpanel;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import pl.baczkowicz.spy.eventbus.IKBus;
import pl.baczkowicz.spy.ui.controllers.ControlPanelItemController;
import pl.baczkowicz.spy.ui.controlpanel.IControlPanelItem;
import pl.baczkowicz.spy.ui.controlpanel.ItemStatus;
import pl.baczkowicz.spy.ui.events.ShowEditConnectionsWindowEvent;

public class ConnectionsControlPanelItem implements IControlPanelItem
{
	private static final double MAX_CONNECTIONS_HEIGHT = 350;
	
	private IKBus eventBus;

	public ConnectionsControlPanelItem(final IKBus eventBus)
	{
		this.eventBus = eventBus;
	}
	
	@Override
	public void update(ControlPanelItemController controller, Button button)
	{
		button.setMaxHeight(MAX_CONNECTIONS_HEIGHT);
		
		// Clear any previously displayed connections
		while (controller.getCustomItems().getChildren().size() > 2) { controller.getCustomItems().getChildren().remove(2); }
		
		controller.setTitle("You haven't got any connections configured.");
		controller.setDetails("Click here to create a new connection...");
		controller.setStatus(ItemStatus.INFO);
		
		button.setOnAction(new EventHandler<ActionEvent>()
		{			
			@Override
			public void handle(ActionEvent event)
			{
				eventBus.publish(new ShowEditConnectionsWindowEvent(button.getScene().getWindow(), true, null));
			}
		});

		controller.refresh();
		
	}
}
