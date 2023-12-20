package Hierarchical.processdiscovery;
/*
 * this class aims to do the activity relation detection, directly follow, contains, overlaps
 */

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.deckfour.xes.model.impl.XEventImpl;
import org.deckfour.xes.model.impl.XLogImpl;
import org.deckfour.xes.model.impl.XTraceImpl;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.simplelogoperations.XLogFunctions;

@Plugin(
		name = "real event log to log with lifecycle event log",// plugin name
		
		returnLabels = {"XES Log"}, //return labels
		returnTypes = {XLog.class},//return class
		
	
		parameterLabels = {"XES Log"},
		
		userAccessible = true,
		help = "" 
		)
public class realEventLog2LifecycleEventLog {

	@UITopiaVariant(
	        affiliation = "TU/e", 
	        author = "", 
	        email = ""
	        )
	@PluginVariant(
			variantLabel = "real event log to log with lifecycle event log, default",
			
			requiredParameterLabels = {0}
			)
	
	public XLog lifecycleEventLogGenerator(UIPluginContext context, XLog originalLog) {
		
		XLog lifecycleLog = (XLog)originalLog.clone();
		XAttributeMap logattlist = XLogFunctions.copyAttMap(originalLog.getAttributes());
		XLog newLog = new XLogImpl(logattlist);   //newLog是更新后的日志
		
	
		
		for(XTrace trace: lifecycleLog)
		{
			XTrace newTrace = new XTraceImpl(XLogFunctions.copyAttMap(trace.getAttributes()));
			XTrace newTrace1 = new XTraceImpl(XLogFunctions.copyAttMap(trace.getAttributes()));
			int[] intstart = new int[trace.size()];
			for(int k = 0; k<trace.size();k++) {
				intstart[k]=0;   //start
			}
			
			//event
			for(int i=trace.size()-1;i>=0;i--)
			{
				if(XLifecycleExtension.instance().extractTransition(trace.get(i)).equals("complete")) {
					
					newTrace.add(newComplete(trace.get(i)));
					int flag = 0;
					for(int j = i-1; j>=0;j--) {
						if(XLifecycleExtension.instance().extractTransition(trace.get(j)).equals("start")&&
								XConceptExtension.instance().extractName(trace.get(i)) == XConceptExtension.instance().extractName(trace.get(j))&&
								intstart[j]==0) {
							intstart[j]=1;
							flag = 1;
							break;
						}
					}
					if(flag==0) {//start
						
						//XConceptExtension.instance().extractName(trace)
						
						newTrace.add(newStart(trace.get(i)));
					}
				}
				else if(XLifecycleExtension.instance().extractTransition(trace.get(i)).equals("start") && intstart[i]==1) {
					newTrace.add(newStart(trace.get(i)));
				}	
			}
			
			for(int l = newTrace.size()-1;l>=0;l--) {
				newTrace1.add(newTrace.get(l));
			}
			newLog.add(newTrace1);
		}			
		return newLog;	
	}	
	 private XEvent newComplete(XEvent oldEvent) {
	    	XAttributeMap attMap = new XAttributeMapImpl();
	    	XLogFunctions.putLiteral(attMap, "concept:name", oldEvent.getAttributes().get("concept:name").toString());
	    	XLogFunctions.putLiteral(attMap, "org:resource", oldEvent.getAttributes().get("org:resource").toString());
	    	XLogFunctions.putTimestamp(attMap, "time:timestamp", XLogFunctions.getTime(oldEvent));
	    	XLogFunctions.putLiteral(attMap, "lifecycle:transition", "complete");
	    	XEvent newEvent = new XEventImpl(attMap);
	    	return newEvent;
	    	}
	 private XEvent newStart(XEvent event) {
	    	XAttributeMap attMap = new XAttributeMapImpl();
	    	XLogFunctions.putLiteral(attMap, "concept:name", event.getAttributes().get("concept:name").toString());
	    	XLogFunctions.putLiteral(attMap, "org:resource", event.getAttributes().get("org:resource").toString());
	    	XLogFunctions.putTimestamp(attMap, "time:timestamp", XLogFunctions.getTime(event));
	    	XLogFunctions.putLiteral(attMap, "lifecycle:transition", "start");
	    	XEvent newEvent = new XEventImpl(attMap);
	    	return newEvent;
	    	}



}
