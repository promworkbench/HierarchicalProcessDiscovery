package Hierarchical.processdiscovery;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
/**
 * this plugin aims to discover a hierarchical process model from a lifecycle event log;
 * Input 1: an event log
 * Input 2: a nesting threshold
 * 
 * Output: hierarchical Petri nets
 */
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.ui.widgets.helper.ProMUIHelper;
import org.processmining.framework.util.ui.widgets.helper.UserCancelledException;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.plugins.dialogs.IMMiningDialog;

import software.processmining.componentbehaviordiscovery.HierarchicalMultiinstancePetriNet;

@Plugin(
		name = "Hierarchical Multi-instance Business Process Model Discovery",// plugin name
		
		returnLabels = {"Hierarchical Multi-instance Business Process Model"}, //return labels
		returnTypes = {HierarchicalMultiinstancePetriNet.class},//return class, a cross-organization process model
		
	      parameterLabels = {"Lifecycle Event Log"},
		
		userAccessible = true,
		help = "This plugin aims to discovery hierarchical multi-instance process business models from lifecycle event logs." 
		)
public class HierarchicalBusinessProcessDiscoveryPlugin {
	@UITopiaVariant(
	        affiliation = "TU/e", 
	        author = "Cong liu", 
	        email = "c.liu.3@tue.nl;liucongchina@gmail.com"
	        )
	@PluginVariant(
			variantLabel = "Hierarchical Multi-instance Business Process Model Discovery, default",
			// the number of required parameters, 0 means the first input parameter 
			requiredParameterLabels = {0})

	public HierarchicalMultiinstancePetriNet HierarchicalBehaviorDiscovery(UIPluginContext context, XLog lifecycleLog) throws ConnectionCannotBeObtained, UserCancelledException, IOException 
	{
		// the input nesting threshold
		double nestingRationThreshold = ProMUIHelper.queryForDouble(context, "Select Nesting Threshold", 0, 1,0.85);	
		//double nestingRationThreshold =0.85;
		
		//set the inductive miner parameters, the original log is used to set the classifier
		IMMiningDialog dialog = new IMMiningDialog(lifecycleLog);
		InteractionResult result = context.showWizard("Configure Parameters for Inductive Miner (used for all sub-processes)", true, true, dialog);
		if (result != InteractionResult.FINISHED) {
			return null;
		}
		// the mining parameters are set here 
		MiningParameters IMparameters = dialog.getMiningParameters(); //IMparameters.getClassifier()
		//create factory to create Xlog, Xtrace and Xevent.
		XFactory factory = new XFactoryNaiveImpl();
		XLogInfo Xloginfo = XLogInfoFactory.createLogInfo(lifecycleLog, IMparameters.getClassifier());
		
		//get the activity set of an event log;
		long startTime_total=System.currentTimeMillis();

		HashSet<String> activitySet =ActivityRelationDetection.getActivitySet(lifecycleLog);
		System.out.println("Activity Set: "+activitySet);
		
		//get all possible activity pairs
		HashSet<ActivityPair> activityPariSet = ActivityRelationDetection.getAllActivityPairs(activitySet);
		System.out.println("Activity Pair Set: "+activityPariSet);
		ActivityRelationDetection.number(lifecycleLog,activitySet);
		//get the frequency of directly follow relations 
		HashMap<ActivityPair, Integer> directlyFollowFrequency =ActivityRelationDetection.getFrequencyofDirectlyFollowRelation(lifecycleLog, activityPariSet);
		System.out.println("Directly Follow Frequency: "+directlyFollowFrequency);
		
		//get the frequency of overlap relations
		HashMap<ActivityPair, Integer> overlapFrequency =ActivityRelationDetection.getFrequencyofOverlapRelation(lifecycleLog, activityPariSet);
		System.out.println("Overlap Frequency: "+overlapFrequency);

		//get the frequency of contain relations
		HashMap<ActivityPair, Integer> containFrequency =ActivityRelationDetection.getFrequencyofContainRelation(lifecycleLog, activityPariSet);
		System.out.println("Contain Frequency: "+containFrequency);

		//get the set of nesting activity pairs that meet the input nesting ratio
		HashSet<ActivityPair> nestingActivityPariSet = new HashSet<>();
		for(ActivityPair ap : containFrequency.keySet())
		{
//			//computing the nesting ratio
			double apNestingRatio =ActivityRelationDetection.nestingFrequencyRatio(lifecycleLog,ap, containFrequency, directlyFollowFrequency, overlapFrequency);
			
			if( ap.getSourceActivity().equals("PO"))
				System.out.println(ap+" "+apNestingRatio);
			if(apNestingRatio>=nestingRationThreshold )
			{
				
				nestingActivityPariSet.add(ap);
			}
		}
		
		//System.out.println("Nesting Activity Pairs Meeting Input Nesting Ratio: "+nestingActivityPariSet); 

		//get the nesting activity pairs and remove transitive reduction. 
		ActivityNestingGraph ang =TransitiveNestingRelationReduction.ActivityPrecedencyGraphConstruction(nestingActivityPariSet);
		

//		for(String node: ang.getAllVertexes())
//		{
//			System.out.println("Nested Activities of "+node+" are: "+TransitiveNestingRelationReduction.getNestedActivitiesOfAnActivity(ang, node)); 
//		}
				
		//Hierarchical Log construction
		HEventLog hlog =HEventLogConstruction.constructHierarchicalLog(ang, activitySet, lifecycleLog, factory, Xloginfo);

		//Hierarchical pn discovery 
		HierarchicalMultiinstancePetriNet hpn  = DiscoverHierarchicalPetriNet.mineHierarchicalPetriNet(context, hlog, IMparameters);
		
		  //long startTime_total=System.currentTimeMillis();
		 // Object[] IMP=IMPetriNet.minePetriNet(context, lifecycleLog, dialog.getMiningParameters());
//		  long endTime_total=System.currentTimeMillis();
//		  System.out.println("***************");
//		  System.out.println("HPM2 Time:"+(endTime_total-startTime_total));
//		  System.out.println("***************");
		return hpn;
	}
	
}

