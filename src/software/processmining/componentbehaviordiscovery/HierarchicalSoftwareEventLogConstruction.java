package software.processmining.componentbehaviordiscovery;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;

import UtilityFunctions.InitializeSoftwareEventLog;

/*
 * this class main implements the hierarchical software event log construction. 
 */
public class HierarchicalSoftwareEventLogConstruction {
	
	/*
	 * construct the hierarchical software event log. 
	 */
	public static HSoftwareEventLog constructHierarchicalLog(int maximalNestingDepth, XFactory factory, Set<String> classSet, XLog originalLog, 
			XLogInfo Xloginfo, String Component)
	{
		//the hierarchical event log contains two parts, main log, and mapping <XEventClass, HLog>.
		HSoftwareEventLog hsoftwareEventLog = new HSoftwareEventLog();
		
		//HashMap<Integer, V>
		//the main part
		XLog mainLog;
		// a structure to store main log and its corresponding nested xevent set
		EventLog2NestedEventSet mainLog2NestedSet;
		
		//the mapping from nested eventclass (events) to its corresponding sub-log. 
		HashMap<XEventClass, HSoftwareEventLog> subLogMapping =new HashMap<XEventClass, HSoftwareEventLog>();
		
		//for top-level, we need to add all events whose caller class does not belongs to the current component.
		// for the nestingDepth=0, we get the main (or top-level) log.
	
		mainLog =InitializeSoftwareEventLog.initialize(factory, "Top-level");//set the log name
		
		//add traces to the main log, and add nesting attribute to each nested method, return (1) mainlog, (2) a list of nested events.
		//the getStartMain is only used to get the main log of the top-level log.
		mainLog2NestedSet = ConstructSoftwareComponentLog.getStartMain(mainLog, originalLog, factory, classSet);

		//set the main log part
		hsoftwareEventLog.setMainLog(mainLog2NestedSet.getMainLog());
		
		//convert the nested eventset to a hashmap <xeventclass, Hashset<Xevent>>
		HashMap<XEventClass, HashSet<XEvent>> xeventclass2Eventlist =ConstructSoftwareComponentLog.convertNestedEventSet2XeventClass(mainLog2NestedSet.getNestedEventSet(), Xloginfo);
		
		//construct the mapping from nested eventclass to sub-log
		if (xeventclass2Eventlist.size()>0 && maximalNestingDepth>1)// there exists nested events for the top-level log
		{
			// we construct a sub-log for each eventclass rather an event
			for(XEventClass xeventc:xeventclass2Eventlist.keySet())
			{
				subLogMapping.put(xeventc, 
						ConstructHierarchicalLogRecusively(maximalNestingDepth, 2, factory, classSet, xeventc, xeventclass2Eventlist.get(xeventc), 
						originalLog, Xloginfo, Component));
			}
		}
		
		//set the mapping part
		hsoftwareEventLog.setSubLogMapping(subLogMapping);
		return hsoftwareEventLog;
	}
	
	/**
	 * 
	 * @param maximalNestingDepth means user selected maximal nesting depth of the discovered hierarchical petri net
	 * @param currentNestingDepth means the current nesting depth
	 * @param factory
	 * @param classSet
	 * @param xeventclass
	 * @param eventSet
	 * @param originalLog
	 * @param currentClassifier
	 * @param Component
	 * @return
	 */
	public static HSoftwareEventLog ConstructHierarchicalLogRecusively(int maximalNestingDepth, int currentNestingDepth, 
			XFactory factory, Set<String> classSet, XEventClass xeventclass, 
			HashSet<XEvent> eventSet, XLog originalLog, XLogInfo Xloginfo, String Component)
	{
		//the hierarchical event log contains two parts, main log, and mapping.
		HSoftwareEventLog hsoftwareEventLog = new HSoftwareEventLog();
				
		//the main part
		XLog mainLog;
		// a structure to store main log and its corresponding nested event list
		EventLog2NestedEventSet mainLog2NestedSet;
		
		//the mapping from nested eventclass (events) to its corresponding sub-log. 
		HashMap<XEventClass, HSoftwareEventLog> subLogMapping =new HashMap<XEventClass, HSoftwareEventLog>();
		
		
		//the main part, each main log has a name
		mainLog =InitializeSoftwareEventLog.initialize(factory, xeventclass.toString());//set the log name use the xeventclass
		
		//add traces to the main log, and add nesting attribute to each nested method, return (1) mainlog, (2) a list of nested events.
		//the getMainOtherLevels method is used to get the main log of other levels, using eventList of this.
		mainLog2NestedSet = ConstructSoftwareComponentLog.getMainOtherLevels(mainLog, eventSet, originalLog, factory, Component);

		//set the mainlog part
		hsoftwareEventLog.setMainLog(mainLog2NestedSet.getMainLog());
		
		//convert the nested event set to a hashmap<xeventclass, hashset<Xevent>>
		HashMap<XEventClass, HashSet<XEvent>> xeventclass2Eventlist =ConstructSoftwareComponentLog.convertNestedEventSet2XeventClass(mainLog2NestedSet.getNestedEventSet(), Xloginfo);
		
		//construct the mapping from nested events to sub-log
		if (xeventclass2Eventlist.size()>0 && maximalNestingDepth>currentNestingDepth)// there exist nested events
		{
			//here need a construct to mapping eventclass to events. we construct a sub-log for each eventclass rather an event
			for(XEventClass xeventc:xeventclass2Eventlist.keySet())
			{
				subLogMapping.put(xeventc, ConstructHierarchicalLogRecusively(maximalNestingDepth, currentNestingDepth+1, 
						factory,classSet, xeventc, xeventclass2Eventlist.get(xeventc), 
						originalLog, Xloginfo, Component));
			}
		}

		hsoftwareEventLog.setSubLogMapping(subLogMapping);
		
		return hsoftwareEventLog;
		
	}
	
	
}
