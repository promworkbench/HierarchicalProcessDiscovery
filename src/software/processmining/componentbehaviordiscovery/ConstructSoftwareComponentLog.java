package software.processmining.componentbehaviordiscovery;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import UtilityFunctions.InitializeSoftwareEventLog;
import openXESsoftwareextension.XSoftwareExtension;


public class ConstructSoftwareComponentLog {
 
	/**
	 * this class aims to extracting software event log of each component by identifying component/interface instances. 
	 * it returns a flat event log.
	 * @param com2class
	 * @param com2classList
	 * @param originalLog
	 * @return
	 */
	public static XLog generatingComponentSoftwareEventLog(String component, Set<String> classSet, XLog originalLog, XFactory factory)
	{
		// create log using the factory and component name as input. 
		XLog componentLog =InitializeSoftwareEventLog.initialize(factory, component);
		
		for(XTrace trace: originalLog)
		{
			XTrace tempTrace = factory.createTrace();
			for(XEvent event: trace)
			{
				// filtering the trace according to the component classes
				if(classSet.contains(XSoftwareExtension.instance().extractPackage(event)+"."+XSoftwareExtension.instance().extractClass(event)))
				{
					tempTrace.add(event);
				}
			}
			// identify component instances for the filtered trace
			// create new traces (each corresponds to one component instance)
			HashMap<String, Set<String>> componentInstance2objectset =new HashMap<String, Set<String>>(); 

			/*
			 * the current approach use class object may cause problems for static method whose object is 0. 
			 * It may be better to use the combination of class object and method name. 
			 */
			componentInstance2objectset =InstanceIdentificationFromTrace(tempTrace,classSet);
			
			// create instance trace and add to component Log. 
			for(String comIns:componentInstance2objectset.keySet())
			{
				XTrace insTrace = factory.createTrace();
				for(XEvent e: tempTrace)
				{
					if (componentInstance2objectset.get(comIns).contains(XSoftwareExtension.instance().extractClassObject(e)))
					{
						insTrace.add(e);
					}
				}	
				componentLog.add(insTrace);
			}
		}
		System.out.println("number of traces in the component log after instance identification: "+componentLog.size());
		return componentLog;
	}
	

	//this method reture the component instance cardinality
	public static Integer componentInstanceCardinality(String component, Set<String> classSet, XLog originalLog, XFactory factory)
	{
		int cardinality =1;
		for(XTrace trace: originalLog)
		{
			XTrace tempTrace = factory.createTrace();
			for(XEvent event: trace)
			{
				// filtering the trace according to the component classes
				if(classSet.contains(XSoftwareExtension.instance().extractPackage(event)+"."+XSoftwareExtension.instance().extractClass(event)))
				{
					tempTrace.add(event);
				}
			}
			// identify component instances for the filtered trace
			HashMap<String, Set<String>> componentInstance2objectset =new HashMap<String, Set<String>>(); 
			componentInstance2objectset =InstanceIdentificationFromTrace(tempTrace,classSet);
			
			if(componentInstance2objectset.size()>1)
				cardinality=componentInstance2objectset.size();
		}
		return cardinality;
	}
	
	/**
	 * construct the instance for each trace (component trace or interface trace)
	 * @param trace
	 * @param classSet, the combination of package name and class name
	 * @return
	 */
	public static HashMap<String, Set<String>> InstanceIdentificationFromTrace(XTrace trace, Set<String> classSet)
	{
		// we first conctruct a connected graph
		 DirectedGraph<String, DefaultEdge> directedGraph =
		            new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
		 
		 // traverse through each event in the case
		 for (XEvent event :trace)
		 {
			 directedGraph.addVertex(XSoftwareExtension.instance().extractClassObject(event));
			//if the caller of this recording belongs to the component.
			if (classSet.contains(XSoftwareExtension.instance().extractCallerpackage(event)+"."+XSoftwareExtension.instance().extractCallerclass(event)))
			{
				directedGraph.addVertex(XSoftwareExtension.instance().extractCallerclassobject(event));
				// add an arc from caller to callee
				directedGraph.addEdge(XSoftwareExtension.instance().extractClassObject(event), 
						XSoftwareExtension.instance().extractCallerclassobject(event));
			}
		 }	
		 
		//compute all weakly connected component
        ConnectivityInspector ci = new ConnectivityInspector(directedGraph);
        
        //Returns a list of Set s, where each set contains 
        //all vertices that are in the same maximally connected component.
        java.util.List connected = ci.connectedSets();
        HashMap<String, Set<String>> Instance2Objs = new HashMap<String, Set<String>>();
        
        for (int i=0;i<connected.size();i++)
        {
        	Instance2Objs.put(i+XConceptExtension.instance().extractName(trace), (Set<String>)connected.get(i));
        }
        
        return Instance2Objs;
	}
	
	
	//convert the nested event set to a Hashset<xeventclass, hashmap<Xevent>>
	public static HashMap<XEventClass, HashSet<XEvent>> convertNestedEventSet2XeventClass(HashSet<XEvent> nestedEventSet, XLogInfo Xloginfo)
	{
		//create a hashmap, here the xeventclass should be with package+class+method (the proper classifier should be selected).
		HashMap<XEventClass, HashSet<XEvent>> xeventclass2nestedevents = new HashMap<XEventClass, HashSet<XEvent>>();
		
		//first get the xeventclass set of these nested events.
		HashSet<XEventClass> xeventclassSet = new HashSet<XEventClass>();
		for (XEvent event:nestedEventSet)
		{
			xeventclassSet.add(Xloginfo.getEventClasses().getClassOf(event));
		}
		
		//then go through the eventclass set, 
		for (XEventClass xeventclass: xeventclassSet)
		{
			xeventclass2nestedevents.put(xeventclass, getXevents4EventClass(xeventclass, nestedEventSet, Xloginfo));
		}
		System.out.println("the number of nested event classes: "+xeventclass2nestedevents.size());
		return xeventclass2nestedevents;
	}
	
	//get Set<Xevent> of one xeventclass. The input is the eventclass and the original eventlist
	public static HashSet<XEvent> getXevents4EventClass(XEventClass xeventclass, HashSet<XEvent> nestedEventSet, XLogInfo Xloginfo)
	{
		HashSet<XEvent> tempEventSet = new HashSet<XEvent>();
		for (XEvent event:nestedEventSet)
		{
			if (xeventclass.toString().equals(Xloginfo.getEventClasses().getClassOf(event).toString()))
			{
				tempEventSet.add(event);
			}
				
		}
		return tempEventSet;
	}
		
	
	//get the main log for the top-level,
	public static EventLog2NestedEventSet getStartMain(XLog mainLog, XLog originalLog, XFactory factory, Set<String> classSet)
	{
		System.out.println("in top-level get main... ");
		EventLog2NestedEventSet mainLog2NestedEvents = new EventLog2NestedEventSet();
		
		// to store the nested methods
		HashSet<XEvent> nestedEventSet = new HashSet<XEvent>();
		for (XTrace trace: originalLog)
		{
			XTrace mainTrace = factory.createTrace();			
			for (XEvent event: trace)
			{
				//collect all top-level events, i.e., 
				if (!classSet.contains(XSoftwareExtension.instance().extractCallerpackage(event)+"."+XSoftwareExtension.instance().extractCallerclass(event)))
				{
					String nestedFlag = checkNesting(event, trace);
					//if the current event is nested, then (1) add nested flag; (2) add to nested event list
					if (nestedFlag.equals("1"))
					{
						nestedEventSet.add(event);
						event.getAttributes().put("Nested_Flag", new XAttributeLiteralImpl("Nested_Flag",nestedFlag));
					}
					else
					{
						//if it is not nested, add the nested_flag ==0.
						event.getAttributes().put("Nested_Flag", new XAttributeLiteralImpl("Nested_Flag",nestedFlag));
					}
					mainTrace.add(event);
				}
				
			}
			mainLog.add(mainTrace);
		}
		
		mainLog2NestedEvents.setMainLog(mainLog);
		mainLog2NestedEvents.setNestedEventSet(nestedEventSet);
		System.out.println("the number of traces in the main: " + mainLog.size());
		System.out.println("the number of nested events in the main: "+ nestedEventSet.size());
		return mainLog2NestedEvents;
	}
	
	//check if an event is nested, i.e. there exist at least one event whose caller method and caller class object equals with its method and class object
	public static String checkNesting(XEvent event, XTrace trace)
	{
		String nestedFlag="0";
		//
		for (XEvent e: trace)
		{
			if (XSoftwareExtension.instance().extractClassObject(event).equals(XSoftwareExtension.instance().extractCallerclassobject(e))
					&&XConceptExtension.instance().extractName(event).equals(XSoftwareExtension.instance().extractCallermethod(e)))
			{
				nestedFlag="1";
				return nestedFlag;
			}
		}


		return nestedFlag;
	}
	
	//get the main log for other levels, i.e., filtering the log using the input event list (find those events whose caller is in the list)
	// the number of traces= |eventlist|*|inputlog|
	public static EventLog2NestedEventSet getMainOtherLevels(XLog mainLog, HashSet<XEvent> eventSet, XLog originalLog, XFactory factory, String componentName)
	{
		System.out.println("in other levels get main... ");
		EventLog2NestedEventSet mainLogNestedSet = new EventLog2NestedEventSet();
		// to store the nested methods
		HashSet<XEvent> nestedEventList = new HashSet<XEvent>();
		
		// we need to cope with different caller, i.e., main and others. 
		for(XEvent callerEvent: eventSet)
		{
			// if the eventlist is not main()
			for (XTrace trace:originalLog)
			{
				XTrace tempTrace = factory.createTrace();	
				for (XEvent calleeEvent: trace)
				{
					if(XSoftwareExtension.instance().extractClassObject(callerEvent).equals(XSoftwareExtension.instance().extractCallerclassobject(calleeEvent))
							&&XConceptExtension.instance().extractName(callerEvent).equals(XSoftwareExtension.instance().extractCallermethod(calleeEvent)))
					{
						// if a event (callee class) does not belong to the current component, it cannot be denoted as nesting
						String nestedFlag = checkNesting(calleeEvent, trace);
						//if the current event is nested, then (1) add nested flag; (2) add to nested event list
						if (nestedFlag.equals("1"))
						{
							nestedEventList.add(calleeEvent);
							calleeEvent.getAttributes().put("Nested_Flag", new XAttributeLiteralImpl("Nested_Flag",nestedFlag));
						}
						else
						{
							//if it is not nested, add the nested_flag ==0.
							calleeEvent.getAttributes().put("Nested_Flag", new XAttributeLiteralImpl("Nested_Flag",nestedFlag));
						}
						tempTrace.add(calleeEvent);
					}
				}
				
				if(tempTrace.size()>0)
				{
					mainLog.add(tempTrace);
				}	
			}
		}
		
		mainLogNestedSet.setMainLog(mainLog);
		mainLogNestedSet.setNestedEventSet(nestedEventList);
		return mainLogNestedSet;
	}
		
		
}
