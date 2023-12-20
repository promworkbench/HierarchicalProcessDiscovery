package Hierarchical.processdiscovery;
/*
 * this class aims to do the activity relation detection, directly follow, contains, overlaps
 */

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

public class ActivityRelationDetection {

	
	//top level activities= all activities - nested activities + root activities
	public static HashSet<String> getTopLevelActivitySet(HashSet<String> activitySet, HashSet<String> allNestedActivities,HashSet<String> rootActivities)
	{
		HashSet<String> topLevelActivities = new HashSet<>();
		topLevelActivities.addAll(activitySet);
		topLevelActivities.removeAll(allNestedActivities);
		topLevelActivities.addAll(rootActivities);
		
		return topLevelActivities;
	}
	
	
	//get the activity set of an input log
	public static HashSet<String> getActivitySet (XLog log)
	{
		HashSet<String> activitySet =new HashSet<String>();
		for(XTrace trace: log)
		{
			for (XEvent event: trace)
			{
				activitySet.add(XConceptExtension.instance().extractName(event));
			}
		}
		return activitySet;
	}
	
	//get all possible activity pairs, not we do not care about recursion, i.e., (a, a) is not considered. 
	public static HashSet<ActivityPair> getAllActivityPairs(HashSet<String> activitySet)
	{
		HashSet<ActivityPair> activityPair = new HashSet<>();
		for(String source: activitySet)
		{
			for(String target: activitySet)
			{
				if(!source.equals(target))
				{
					ActivityPair ap = new ActivityPair(source, target);
					activityPair.add(ap);
				}
			}
		}
		return activityPair;
	}
	
public static void number(XLog lifecycleLog,HashSet<String> activitySet) {
		
		Iterator<String> iterator = activitySet.iterator();
		while(iterator.hasNext()) {
			String name = iterator.next();
			int f = 0;
			for(XTrace trace: lifecycleLog)
			{
				int []flag = new int[trace.size()];
				
				for(int i = 0;i<trace.size()-1;i++) {
					flag[i]=0;
				}
				for(XEvent event: trace)
				{
					if(XLifecycleExtension.instance().extractTransition(event).equals("start")&& 
							XConceptExtension.instance().extractName(event).equals(name)){
						for(int k = trace.indexOf(event);k<trace.size();k++) {
							if(XLifecycleExtension.instance().extractTransition(trace.get(k)).equals("complete")&& 
							XConceptExtension.instance().extractName(trace.get(k)).equals(name)&&
							flag[k]==0) {
								f++;
								flag[k]=1;
								break;
							}
						}
					}
				}
			}
			System.out.println(name + " "+f);
		}	
	}
	
	//get the frequency of directly follow relations 
	public static HashMap<ActivityPair, Integer> getFrequencyofDirectlyFollowRelation(XLog lifecycleLog, HashSet<ActivityPair> activityPariSet)
	{
		HashMap<ActivityPair, Integer> directlyFollowFrequency = new HashMap<>();
		
		int sourceStart =0;
		int sourceComplete =0;
		int targetStart =0;
		int targetComplete =0;
		
		for(ActivityPair ap : activityPariSet)
		{
			int frequency = 0;
			for(XTrace trace: lifecycleLog)
			{
				for(XEvent event: trace)
				{
					if(XConceptExtension.instance().extractName(event).equals(ap.getSourceActivity())&&
							XLifecycleExtension.instance().extractTransition(event).equals("start"))//find the start event of source
					{
						sourceStart =trace.indexOf(event);
						sourceComplete =0;
						targetStart =0;
						targetComplete =0;
						for(int i=sourceStart;i<trace.size();i++)
						{
							if(XConceptExtension.instance().extractName(trace.get(i)).equals(ap.getSourceActivity())&&
									XLifecycleExtension.instance().extractTransition(trace.get(i)).equals("complete"))//find the start event of source
							{
								sourceComplete=i;
								break;
							}	
						}
						for(int j =sourceComplete;j<trace.size();j++)
						{
							if(XConceptExtension.instance().extractName(trace.get(j)).equals(ap.getTargetActivity())&&
									XLifecycleExtension.instance().extractTransition(trace.get(j)).equals("start"))//find the start event of source
							{
								targetStart=j;
								break;
							}
						}
						for(int k=targetStart; k<trace.size();k++)
						{

							if(XConceptExtension.instance().extractName(trace.get(k)).equals(ap.getTargetActivity())&&
									XLifecycleExtension.instance().extractTransition(trace.get(k)).equals("complete"))//find the start event of source
							{
								targetComplete=k;
								break;
							}
						}
						
						if(sourceStart<sourceComplete && sourceComplete<targetStart && targetStart<targetComplete)
						{
							frequency++;
						}
						
					}
				}
				
			}
			directlyFollowFrequency.put(ap, frequency);
		}
		
		return directlyFollowFrequency;
	}
	
	//get the frequency of overlap relations
	public static HashMap<ActivityPair, Integer> getFrequencyofOverlapRelation(XLog lifecycleLog, HashSet<ActivityPair> activityPariSet)
	{
		HashMap<ActivityPair, Integer> overlapFrequency = new HashMap<>();
		
		int sourceStart =0;
		int targetStart =0;
		int sourceComplete =0;
		int targetComplete =0;
		
		for(ActivityPair ap : activityPariSet)
		{
			int frequency = 0;
			for(XTrace trace: lifecycleLog)
			{
				for(XEvent event: trace)
				{
					if(XConceptExtension.instance().extractName(event).equals(ap.getSourceActivity())&&
							XLifecycleExtension.instance().extractTransition(event).equals("start"))//find the start event of source
					{
						sourceStart =trace.indexOf(event);
						targetStart =0;
						sourceComplete =0;
						targetComplete =0;
						for(int i=sourceStart;i<trace.size();i++)
						{
							if(XConceptExtension.instance().extractName(trace.get(i)).equals(ap.getTargetActivity())&&
									XLifecycleExtension.instance().extractTransition(trace.get(i)).equals("start"))//find the start event of source
							{
								targetStart=i;
								break;
							}	
						}
						for(int j =targetStart;j<trace.size();j++)
						{
							if(XConceptExtension.instance().extractName(trace.get(j)).equals(ap.getSourceActivity())&&
									XLifecycleExtension.instance().extractTransition(trace.get(j)).equals("complete"))//find the start event of source
							{
								sourceComplete=j;
								break;
							}
						}
						for(int k=sourceComplete; k<trace.size();k++)
						{

							if(XConceptExtension.instance().extractName(trace.get(k)).equals(ap.getTargetActivity())&&
									XLifecycleExtension.instance().extractTransition(trace.get(k)).equals("complete"))//find the start event of source
							{
								targetComplete=k;
								break;
							}
						}
						if(sourceStart<targetStart && targetStart<sourceComplete && sourceComplete<targetComplete)
						{
							frequency++;
						}
					}
				}
			}
			overlapFrequency.put(ap, frequency);
		}
		
		return overlapFrequency;
	}
	
	
	//get the frequency of contain relations
	public static HashMap<ActivityPair, Integer> getFrequencyofContainRelation(XLog lifecycleLog, HashSet<ActivityPair> activityPariSet)
	{
		HashMap<ActivityPair, Integer> containFrequency = new HashMap<>();
		
		int sourceStart =0;
		int targetStart =0;
		int targetComplete =0;
		int sourceComplete =0;

		for(ActivityPair ap : activityPariSet)
		{
			int frequency = 0;
			for(XTrace trace: lifecycleLog)
			{
				for(XEvent event: trace)
				{
					if(XConceptExtension.instance().extractName(event).equals(ap.getSourceActivity())&&
							XLifecycleExtension.instance().extractTransition(event).equals("start"))//find the start event of source
					{
						sourceStart =trace.indexOf(event);
						targetStart =0;
						targetComplete =0;
						sourceComplete =0;
						
						for(int i=sourceStart;i<trace.size();i++)
						{
							if(XConceptExtension.instance().extractName(trace.get(i)).equals(ap.getTargetActivity())&&
									XLifecycleExtension.instance().extractTransition(trace.get(i)).equals("start"))//find the start event of source
							{
								targetStart=i;
								break;
							}	
						}
						for(int j =targetStart;j<trace.size();j++)
						{
							if(XConceptExtension.instance().extractName(trace.get(j)).equals(ap.getTargetActivity())&&
									XLifecycleExtension.instance().extractTransition(trace.get(j)).equals("complete"))//find the start event of source
							{
								targetComplete=j;
								break;
							}
						}
						for(int k=targetComplete; k<trace.size();k++)
						{

							if(XConceptExtension.instance().extractName(trace.get(k)).equals(ap.getSourceActivity())&&
									XLifecycleExtension.instance().extractTransition(trace.get(k)).equals("complete"))//find the start event of source
							{
								sourceComplete=k;
								break;
							}
						}
						if(sourceStart<targetStart && targetStart<targetComplete && targetComplete<sourceComplete)
						{
							frequency++;
						}
					}
				}
			}
			
			containFrequency.put(ap, frequency);
		}
		
		return containFrequency;
	}
	
	public static boolean activityPayFrequency(XLog lifecycleLog,ActivityPair ap) {
		int f = 0;
		int m = 0;
		for(XTrace trace:lifecycleLog) {
			for(XEvent event:trace) {
				if(XConceptExtension.instance().extractName(event).equals(ap.getSourceActivity())) {
					f++;
					break;
				}
				else if(XConceptExtension.instance().extractName(event).equals(ap.getTargetActivity())){
					m++;
					break;
				}
			}
		}
		if(ap.getSourceActivity().equals("C")||ap.getSourceActivity().equals("F"))
			System.out.println(ap+" : "+f+" "+m);
		if(f>m)
		{
			return true;
		}
			
		return false;
	}
	
	//compute the nesting frequency
	public static double nestingFrequencyRatio(XLog lifecycleLog,ActivityPair ap, HashMap<ActivityPair, Integer> containRelation, HashMap<ActivityPair, Integer> directlyFollowRelation, HashMap<ActivityPair, Integer> overlapRelation)
	{
		//get the nesting frequency of ap
		int apNesting =containRelation.get(ap);
		
		//get the contain relation of ap
		int apDirectlyFollow = directlyFollowRelation.get(ap);
		
		//get the overlap relation of ap
		int apOverlap = overlapRelation.get(ap);
				
		//get the nesting frequency of ap reverse
		ActivityPair apReverse = new ActivityPair(ap.getTargetActivity(), ap.getSourceActivity());
		
		//get the nesting frequency of ap
		int apReverseNesting =containRelation.get(apReverse);
		
		//get the contain relation of ap
		int apReverseDirectlyFollow = directlyFollowRelation.get(apReverse);
		
		//get the overlap relation of ap
		int apReverseOverlap = overlapRelation.get(apReverse);
			
		
		double nestingFrequencyRatio=0;
		int sum = 0;
		sum = apNesting+apDirectlyFollow+apOverlap+apReverseNesting+apReverseDirectlyFollow+apReverseOverlap;
//		if(activityPayFrequency(lifecycleLog, ap)&&apNesting>apDirectlyFollow&&apNesting>apOverlap
//				&&apNesting>apReverseDirectlyFollow&&apNesting>apReverseOverlap&&apNesting>apReverseNesting){
//			sum = apNesting+apDirectlyFollow+apOverlap;	
//		}
//		else {
//			sum = apNesting+apDirectlyFollow+apOverlap+apReverseNesting+apReverseDirectlyFollow+apReverseOverlap;
//		}
		if(sum>0) {
			nestingFrequencyRatio =(double)(apNesting)/sum;
		}
		else
		{
			nestingFrequencyRatio =(double)(apNesting)/(sum+1);
		}
		
		
		System.out.println(ap+ "->"+nestingFrequencyRatio);
		return nestingFrequencyRatio;
	}



}
