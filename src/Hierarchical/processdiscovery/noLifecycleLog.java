package Hierarchical.processdiscovery;

import java.io.IOException;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XLogImpl;
import org.deckfour.xes.model.impl.XTraceImpl;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.simplelogoperations.XLogFunctions;

/**
 * this plugin aims to transform a normal event log (only with complete event log) to a lifecycle event log. 
 * Note that the event name of the normal event log is "AAA_s" or "AAA_c" that indicates the lifecycle information
 * @author cliu3
 *
 */

@Plugin(
		name = "lifecycle log to normal log",// plugin name
		
		returnLabels = {"XES Log"}, //return labels
		returnTypes = {XLog.class},//return class
		
		//input parameter labels, corresponding with the second parameter of main function
		parameterLabels = {"XES Log"},
		
		userAccessible = true,
		help = "" 
		)
public class noLifecycleLog {
	@UITopiaVariant(
	        affiliation = "", 
	        author = "", 
	        email = ""
	        )
	@PluginVariant(
			variantLabel = "lifecycle log to normal log, default",
			// the number of required parameters, {0} means one input parameter
			requiredParameterLabels = {0}
			)
	public XLog lifecycleEventLogGenerator(UIPluginContext context, XLog originalLog) throws IOException 
	{
		XLog Log = (XLog)originalLog.clone();
		XAttributeMap logattlist = XLogFunctions.copyAttMap(originalLog.getAttributes());
		
		XLog newLog = new XLogImpl(logattlist);   //newLog
		for(XTrace trace: Log)
		{
			XTrace newTrace = new XTraceImpl(XLogFunctions.copyAttMap(trace.getAttributes()));
			for (XEvent event: trace)
			{
				if(XConceptExtension.instance().extractName(event).equals("LC"))
					continue;
				newTrace.add(event);
			}
			if(newTrace.size()!=0) {
				newLog.add(newTrace);
			}
			//newLog.add(newTrace);
		}
		return newLog;
	}
}
