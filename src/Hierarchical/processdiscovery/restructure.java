package Hierarchical.processdiscovery;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeMapImpl;
import org.deckfour.xes.model.impl.XEventImpl;
import org.deckfour.xes.model.impl.XLogImpl;
import org.processmining.plugins.simplelogoperations.XLogFunctions;

public class restructure {
	public XLog addSubCid(XFactory factory, XLog sublog, String path) throws IOException {//sublog
		
		
		XAttributeMap logattlist = XLogFunctions.copyAttMap(sublog.getAttributes());
		XLog newLog = new XLogImpl(logattlist);   //newLog
		XLog newsublog = new XLogImpl(logattlist);   //newLog
		//∂¡»°–≈œ¢
		FileReader file = new FileReader(path);
		BufferedReader read =new BufferedReader(file); 
		
		String line = null;
		line = read.readLine();
		
		//--------------------------------------------
		String [] line1 = line.split(",");
		
		XTrace newTrace = factory.createTrace();
		
		//.csvcase id°trace
		String caseid =line1[0];
		
		while(read.lines() != null) {
			
			if(!caseid.equals(line1[0])) {
				
				XConceptExtension.instance().assignName(newTrace, caseid); //
				caseid =line1[0];		
				newLog.add(newTrace);
				newTrace = factory.createTrace();
			}
			newTrace.add(addNewEvent(line1[1]));
			line = read.readLine();
			if(line!=null)
			     line1 = line.split(",");
			else 
				break;
			
			
		}
		read.close();
		return newLog;
	}
	 private XEvent addNewEvent(String eventName) {
	    	XAttributeMap attMap = new XAttributeMapImpl();
	    	
	    	XLogFunctions.putLiteral(attMap, "concept:name", eventName);	   
	    	XLogFunctions.putLiteral(attMap, "lifecycle:transition", "complete");
	    	XEvent newEvent = new XEventImpl(attMap);
	    	return newEvent;
	    	}
	 private XEvent addSubCid(XEvent oldEvent, String sub_id) {
	    	XAttributeMap attMap = new XAttributeMapImpl();
	    	
	    	XLogFunctions.putLiteral(attMap, "concept:name", oldEvent.getAttributes().get("concept:name").toString());
	    	//XLogFunctions.putLiteral(attMap, "concept:instance", oldEvent.getAttributes().get("concept:instance").toString());
	    	//System.out.println(" instance :" + oldEvent.getAttributes().get("concept:instance").toString()+ " event " + oldEvent.getAttributes().get("concept:name").toString());
	    	XLogFunctions.putLiteral(attMap, "org:resource", sub_id);
	    	XLogFunctions.putTimestamp(attMap, "time:timestamp", XLogFunctions.getTime(oldEvent));
	    	XLogFunctions.putLiteral(attMap, "lifecycle:transition", "complete");
	    	XEvent newEvent = new XEventImpl(attMap);
	    	return newEvent;
	    	}
}
