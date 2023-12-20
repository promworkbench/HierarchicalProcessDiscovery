package Hierarchical.processdiscovery;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.deckfour.xes.classification.XEventAttributeClassifier;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.deckfour.xes.model.impl.XAttributeMapImpl;

public class HEventLogConstruction {
	/*
	 * construct the hierarchical event log recursively. 
	 */
	public static HEventLog constructHierarchicalLog(ActivityNestingGraph ang, HashSet<String> activitySet , XLog lifecycleLog, XFactory factory, XLogInfo Xloginfo) throws IOException
	{
		//get all nested activities
		HashSet<String> allNestedActivities = new HashSet<>();
		for(String n: TransitiveNestingRelationReduction.getAllNestedActivities(ang))
		{
			allNestedActivities.add(n);
		}
		System.out.println("All Nested Activities: "+allNestedActivities); 

		//get all root nesting activities
		HashSet<String> rootActivities  =TransitiveNestingRelationReduction.getAllRootActivities(ang);
		System.out.println("Root Activities: "+rootActivities); 
		
		//get all root nesting activities
		HashSet<String> topLevelActivities  =ActivityRelationDetection.getTopLevelActivitySet(activitySet, allNestedActivities, rootActivities);
		System.out.println("Top Level Activities: "+topLevelActivities); 
		
		
		//the hierarchical event log contains two parts, main log, and mapping <XEventClass, HLog>.
		HEventLog hEventLog = new HEventLog();
		
		//convert top-level activities to XEventClass set
		HashSet<XEventClass> XeventClassSetofTopLevelActivities =getEventClassSet(topLevelActivities, lifecycleLog, Xloginfo);
		System.out.println("Top Level XeventClasses: "+XeventClassSetofTopLevelActivities); 


		//the main part
		XLog mainLog =getMainLog(factory, "Top-level", XeventClassSetofTopLevelActivities, lifecycleLog, Xloginfo);//set the log name
				
		hEventLog.setMainLog(mainLog);
		//the mapping from nested eventclass (activities) to its corresponding sub-log. 
		HashMap<XEventClass, HEventLog> subLogMapping =new HashMap<XEventClass,HEventLog>();
		HashMap<XEventClass, HEventLog> newsubLogMapping =new HashMap<XEventClass, HEventLog>();

		for(String rootNestedActivity: rootActivities)
		{
			//get the xeventclass of a root activity
			XEventClass eventClassActivity =getEventClassOfActivity(rootNestedActivity, lifecycleLog, Xloginfo);
			subLogMapping.put(eventClassActivity,
					ConstructHierarchicalEventLogRecusively(factory, convertSet2Hashset(TransitiveNestingRelationReduction.getNestedActivitiesOfAnActivity(ang, rootNestedActivity)), lifecycleLog, Xloginfo, ang));
		}
		newsubLogMapping = getsubsequenceRecusively(factory, subLogMapping);
		
		hEventLog.setSubLogMapping(newsubLogMapping);
		//hEventLog.setSubLogMapping(subLogMapping);
		return hEventLog;
	}
	
	public static HashMap<XEventClass, HEventLog> getsubsequenceRecusively(XFactory factory, HashMap<XEventClass, HEventLog> subLogMapping) throws IOException {
			
		    HashMap<XEventClass, HEventLog> newsubLogMapping = new HashMap<XEventClass, HEventLog>();
		    Iterator<Entry<XEventClass, HEventLog>> iterator = subLogMapping.entrySet().iterator();
		    
		    while(iterator.hasNext()){
		    	Entry<XEventClass, HEventLog> next1 = iterator.next();
		    	XEventClass key = next1.getKey();
		    	HEventLog value = next1.getValue();
		    	XEventClass class1;
		    	//sublog
		    	XLog sublog = value.getMainLog();
		    	if(boolMultiInstance(sublog)==2) {
		    		
					String path = "E:\\wyFile\\";
					String name = key.getId();
					//path1
					String path1 = path + name+ ".csv";
					FileWriter file1 = new FileWriter(path1);
					BufferedWriter writer = new BufferedWriter(file1);
					
					for(XTrace trace: sublog)
					{
						String caseid = XConceptExtension.instance().extractName(trace);
					    String  caseid1 = caseid.substring(caseid.indexOf('_')+1);
						
						for (XEvent event:trace)
						{
							//String event_name = event.getClass().getName();
						    String eventName = XConceptExtension.instance().extractName(event);
						    
						    if(eventName!=null) {
						    	 
						    	//读取caseid+eventName,case id 用数字表示
							   writer.write(caseid1 + ','+eventName);
							   //writer.write(eventName);
							    writer.write("\r\n");
						    }
						}
						
					}
					writer.close();
					//path2 Python
					String path2 = "E:\\wyFile\\sub_case_iden2.py";
//					String path2 = "E:\\wyFile\\sub_case_iden3.py";
					System.out.println("test 调用.py文件.......");
					
					//path3 
					String path3 = path + name + "1.csv";
					addPython(path2,path1,path3);
					
					//sub_case_id
					System.out.println("进行子日志更新.......");
					restructure re= new restructure();
					
					XLog xLog = re.addSubCid(factory, sublog, path3);
					
					System.out.println("更新完成....");
					
					value.setMainLog(xLog);
					String name1 = key.toString()+ "_2";
					class1 = new XEventClass(name1, key.getIndex());
					System.out.println("key label:" + key.getId()+ "index: "+key.getIndex());
		    	}
		    	else {  
		    		value.setMainLog(sublog);
		    		String name1 = key.toString();
					 class1 = new XEventClass(name1, key.getIndex());
		    	}
		    	
				HashMap<XEventClass, HEventLog> sub_sub= new HashMap<XEventClass, HEventLog>();
		    	if(value.getSubLogMapping()!=null){
		    		sub_sub = getsubsequenceRecusively(factory, value.getSubLogMapping());
		    	}
		    	value.setSubLogMapping(sub_sub);
		    	//newsubLogMapping.put(key, value);
		    	newsubLogMapping.put(class1, value);
		}
		    return newsubLogMapping;
	}
	
	public static int boolMultiInstance(XLog sublog) {
		
		HashSet<String> activitySet =ActivityRelationDetection.getActivitySet(sublog);
		Iterator <String> it = activitySet.iterator();
		int actLen = 0;
		int multi = 0;
		int dan = 0;
		while(it.hasNext()) {
			String name = it.next();
			actLen++;
			int frequence = 0;
			int eventInTraceNum = 0;
			
			for(XTrace trace: sublog) {
				boolean isIN = false;
				for(XEvent event: trace) {
					if(XConceptExtension.instance().extractName(event).toString().equals(name)&&
							XLifecycleExtension.instance().extractTransition(event).toString().equals("complete")) {
						frequence++;
						isIN = true;
					}//if								
				}// for
				if(isIN) {
					eventInTraceNum++;
				}
			}// for
			
			if(frequence == eventInTraceNum) {  
				dan++;
			}
			else if(frequence > eventInTraceNum) {
				multi++;
			}
		}
		
		if((1.0*dan)/actLen > 0.75) {
			return 1;
		}
		else if((1.0 * multi) / actLen >0.75) {
			return 2;
		}
		else 
			return 2;
		
		
	}
	
	//Python
	public static void addPython(String path,String path1,String path2) {

        try {
       	String[] args1 = new String[] {"python", path,path1,path2};//
        	
            Process proc = Runtime.getRuntime().exec(args1);
         
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line;
            //System.out.println(in.readLine());
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
            in.close();
            proc.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } 
	}

	/*
	 * constructive hierarchical log recursively
	 */
	public static HEventLog ConstructHierarchicalEventLogRecusively(XFactory factory, 
			HashSet<String> nestedActivitySet, XLog lifecycleLog, XLogInfo Xloginfo, ActivityNestingGraph ang)
	{
		//the hierarchical event log contains two parts, main log, and mapping.
		HEventLog hEventLog = new HEventLog();
				
		//for each top-level activity, we construct its HEventLog
		HashSet<XEventClass> XeventClassSetofNestedActivities =getEventClassSet(nestedActivitySet, lifecycleLog, Xloginfo);
				
		///the main part
		XLog mainLog =getMainLog(factory, "sub-level", XeventClassSetofNestedActivities, lifecycleLog, Xloginfo);//set the log name
				
		hEventLog.setMainLog(mainLog);
		
		//the mapping from nested eventclass (activities) to its corresponding sub-log. 
		HashMap<XEventClass, HEventLog> subLogMapping =new HashMap<XEventClass, HEventLog>();
		
		for(String node: nestedActivitySet)
		{
			HashSet<String> nestedActivities =convertSet2Hashset(TransitiveNestingRelationReduction.getNestedActivitiesOfAnActivity(ang, node));
			if(nestedActivities.size()!=0)//recursive calling
			{
				//get the xeventclass of a root activity
				XEventClass xeventClassActivity =getEventClassOfActivity(node, lifecycleLog, Xloginfo);
				subLogMapping.put(xeventClassActivity, ConstructHierarchicalEventLogRecusively(factory, 
								nestedActivities,lifecycleLog, Xloginfo, ang));
			}
		}
		
		hEventLog.setSubLogMapping(subLogMapping);
		
		return hEventLog;
		
	}
	
	public static HashSet<String> convertSet2Hashset (Set<String> set)
	{
		HashSet<String> hashs = new HashSet<>();
		for(String s: set)
		{
			hashs.add(s);
		}
		
		return hashs;
	}
	//construct the main log, we only keep the complete event 
	public static XLog getMainLog(XFactory factory, String logName, HashSet<XEventClass> XeventClassSetofTopLevelActivities, XLog lifecycleLog, XLogInfo Xloginfo)
	{
		XLog mainLog =initializeEventLog(factory, "Top-level");//set the log name
		XAttributeMap attMap = new XAttributeMapImpl();
		for(XTrace trace: lifecycleLog)
		{
			XTrace newTrace = factory.createTrace();
			for(XEvent event: trace)
			{
				if (XeventClassSetofTopLevelActivities.contains(Xloginfo.getEventClasses().getClassOf(event))&&
						XLifecycleExtension.instance().extractTransition(event).toLowerCase().equals("complete"))
				{
					newTrace.add(event);
				}
			}
			if(newTrace.size()>0)
			{
				XConceptExtension.instance().assignName(newTrace, XConceptExtension.instance().extractName(trace));
				mainLog.add(newTrace);
			}
		}
		
		return mainLog;
		
	}
	
	
	//get the xeventclass set of a set of nested activities
	public static HashSet<XEventClass> getEventClassSet(HashSet<String> nestedActivities, XLog lifecycleLog,  XLogInfo Xloginfo)
	{
		HashSet<XEventClass> XeventClassSetofNestedActivities = new HashSet<XEventClass>();
		for(String nestA: nestedActivities)
		{
			int flag =0;
			for(XTrace trace: lifecycleLog)
			{
				for (XEvent event:trace)
				{
					if(XConceptExtension.instance().extractName(event).equals(nestA))
					{
						XeventClassSetofNestedActivities.add(Xloginfo.getEventClasses().getClassOf(event));
						//
						flag =1;
						break;
					}
					
				}
				if(flag ==1)// the current event class is found. 
				{
					break;
				}
			}
		}
		return XeventClassSetofNestedActivities;
	}
	
	
	//get the xeventclass set of a set of nested activities
	public static XEventClass getEventClassOfActivity(String nestedActivity, XLog lifecycleLog, XLogInfo Xloginfo)
	{

		for(XTrace trace: lifecycleLog)
		{
			for (XEvent event:trace)
			{
				if(XConceptExtension.instance().extractName(event).equals(nestedActivity))
				{
					return Xloginfo.getEventClasses().getClassOf(event);
				}
			}
		}
		
		return null;
		
	}
	
	// initialize main software event log
	public static XLog initializeEventLog(XFactory factory, String logName)
	{
		//add the log name		
		XLog lifecycleLog = factory.createLog();
		XConceptExtension.instance().assignName(lifecycleLog, logName);
		
		//create standard extension
		XExtension conceptExtension = XConceptExtension.instance();
		XExtension timeExtension = XTimeExtension.instance();
		XExtension lifecycleExtension=XLifecycleExtension.instance();
		
		// create extensions
		lifecycleLog.getExtensions().add(conceptExtension);
		lifecycleLog.getExtensions().add(lifecycleExtension);
		lifecycleLog.getExtensions().add(timeExtension); 
		
		// create trace level global attributes
		XAttribute xtrace = new XAttributeLiteralImpl(XConceptExtension.KEY_NAME, "DEFAULT"); 
		lifecycleLog.getGlobalTraceAttributes().add(xtrace);

		// create event level global attributes		
		lifecycleLog.getGlobalEventAttributes().add(XConceptExtension.ATTR_NAME);
		lifecycleLog.getGlobalEventAttributes().add(XLifecycleExtension.ATTR_TRANSITION);
		
		
		// create classifiers based on global attribute		
		XEventAttributeClassifier activityClassifer = new XEventAttributeClassifier("Activity Name", 
				 XConceptExtension.KEY_NAME);
		lifecycleLog.getClassifiers().add(activityClassifer);
		
		return lifecycleLog;
	}
}
