package pl.baczkowicz.mqspy.daemon.remote;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;

import pl.baczkowicz.mqspy.daemon.MqSpyDaemon;

public class RunScriptRequestHandler extends HttpServlet
{
	private static final long serialVersionUID = -9204115887954866526L;
	
	private MqSpyDaemon daemon;

	public RunScriptRequestHandler(final MqSpyDaemon mqSpyDaemon)
	{
		this.daemon = mqSpyDaemon;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		resp.setStatus(HttpStatus.OK_200);
		
		if (req.getParameterMap().containsKey("name"))
		{
			final String scriptName = req.getParameter("name");
			if (req.getParameterMap().containsKey("async"))
			{
				final Map<String, Object> args = new HashMap<>();
				args.putAll(req.getParameterMap());
				
				resp.getWriter().println("Attemping to run script with parameters: " + scriptName);
				daemon.runScript(scriptName, Boolean.valueOf(req.getParameter("async")), args);
			}
			else
			{
				resp.getWriter().println("Attemping to run script: " + scriptName);
				daemon.runScript(scriptName);				
			}
		}
		else
		{		
			resp.getWriter().println("Invalid request: missing 'name' parameter");
		}
		
		resp.getWriter().println("Received: " + req.getParameterMap());
	}
}
