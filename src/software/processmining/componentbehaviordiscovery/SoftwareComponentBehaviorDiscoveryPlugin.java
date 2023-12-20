package software.processmining.componentbehaviordiscovery;

import java.util.HashSet;
import java.util.Set;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.plugin.events.Logger.MessageLevel;
import org.processmining.framework.util.ui.widgets.helper.ProMUIHelper;
import org.processmining.framework.util.ui.widgets.helper.UserCancelledException;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.plugins.dialogs.IMMiningDialog;

import UtilityClasses.ClassClass;
import UtilityClasses.ComponentConfig;

/**
 * this plugin aims to discover the behavior model for each software component. 
 * each component model refers to a hierarchical Petri net. 
 * 
 * Input 1: a software event log (original data, each trace refers to one software execution), obtained from "XPort based Software Event Log Pre-processing (V2-parameter values)" plugin 
 * using Maiker's XPort Instrumentation as tool;
 * Input 2: component configuration file indicating the mapping from component to classes, 
 * this file is obtained manually or automatically using class clustering approaches, e.g., "Class Interaction Graph Discovery" plugin.  
 * 
 * parameter: the nesting depth of the hierarchical Petri net model. the default setting is 3.  
 * 
 * Output: a set of component models describing the behavior of each component in term of HPN models.
 * 
 * By combining with the "Class Interaction Graph Discovery" plugin, this plugin has two extreme application: 
 * (1) if we move the slider of "Class Interaction Graph Discovery" plugin to the minimal value, we can get a component that includes all classes, 
 * 		therefore, we can discover the whole software behavior as a component. 
 * 		Note: in this case, the top-level component log is those events whose caller do not belongs to the class set of the component. 
 * 		Therefore, if the class set includes all methods of the software, we cannot get the top-level log.  
 * 		Specially, the caller of main() is null. 
 * (2) if we move the slider of "Class Interaction Graph Discovery" plugin to the maximal value, we can get a set of components that each only includes a single class, 
 * 		therefore, we can discover the so-called "Dynamic Class Object Process Graph" or "Object Process Graph". There should be some further investigation in this direction. 
 * 		[ref1: Dynamic Object Process Graphs, J. Quante, 2008] 
 * 		[ref2: Dynamic protocol recovery, J. Quante, 2007]
 * @author cliu3
 */

@Plugin(
		name = "Software Component Behavior Discovery (All Components New)",// plugin name
		
		returnLabels = {"Software Component Behavior"}, //return labels
		returnTypes = {ComponentModelSet.class},//return class, a set of component to hpns 
		
		//input parameter labels, corresponding with the second parameter of main function
		parameterLabels = {"Software Event Log", "Component Configuration"},
		
		userAccessible = true,
		help = "This plugin aims to discover the component behavior models of software." 
		)
public class SoftwareComponentBehaviorDiscoveryPlugin {
	
	@UITopiaVariant(
	        affiliation = "TU/e", 
	        author = "Cong liu", 
	        email = "c.liu.3@tue.nl OR liucongchina@163.com"
	        )
	@PluginVariant(
			variantLabel = "Software Component Behavior Discovery for each Component, default",
			// the number of required parameters, 0 means the first input parameter, 1 means the second input parameter
			requiredParameterLabels = { 0,1 })
	
	public ComponentModelSet componentBehaviorDiscovery(UIPluginContext context, XLog originalLog, ComponentConfig comConfig) throws ConnectionCannotBeObtained, UserCancelledException 
	{
		//get the log name from the original log. it is shown as the title of returned results. 
		context.getFutureResult(0).setLabel("Component Behavior of: " + XConceptExtension.instance().extractName(originalLog));
		return discovery(context, originalLog, comConfig);
	}
	
	public static int currentNestingDepth =0;//current recursion depth.
	
	public ComponentModelSet discovery(UIPluginContext context, XLog originalLog, ComponentConfig comConfig) throws ConnectionCannotBeObtained, UserCancelledException
	{			
		context.log("Component Behavior Discovery Starts...", MessageLevel.NORMAL);		
		//the component models that we want to discover
		ComponentModelSet componenModels = new ComponentModelSet();
		
		//set the inductive miner parameters, the original log is used to set the classifier
		IMMiningDialog dialog = new IMMiningDialog(originalLog);
		InteractionResult result = context.showWizard("Configure Parameters for Inductive Miner (used for all component models)", true, true, dialog);
		if (result != InteractionResult.FINISHED) {
			return null;
		}
		
		
		// the mining parameters are set here 
		MiningParameters IMparameters = dialog.getMiningParameters(); //IMparameters.getClassifier()
	
		//select the maximal nesting depth for the discovered hierarchical petri net. 
		String [] patterns = new String[6];
		patterns[0]="1";
		patterns[1]="2";
		patterns[2]="3";
		patterns[3]="4";
		patterns[4]="5";
		patterns[5]="6";
		int maximalNestingDepth =Integer.parseInt(ProMUIHelper.queryForObject(context, "Select the nesting depth", patterns));
		System.out.println("Selected nesting depth: "+maximalNestingDepth);
		context.log("Maximal Nesting Depth is: "+maximalNestingDepth, MessageLevel.NORMAL);		
		
		//create factory to create XLog, XTrace and XEvent 
		XFactory factory = new XFactoryNaiveImpl();	
		XLogInfo Xloginfo = XLogInfoFactory.createLogInfo(originalLog, IMparameters.getClassifier());

		//identify component instances, and construct software event log for each component.
		for (String component: comConfig.getAllComponents())
		{
			context.log("Current component name is: "+component, MessageLevel.NORMAL);		
			//obtain the software event log for each component.
			// input:(1) component name, (2) class set of this component; and (2) the original software log
			
			// create class set of the current component
			Set<String> componentClassSet = new HashSet<>();
			for(ClassClass c: comConfig.getClasses(component))
			{
				componentClassSet.add(c.toString());// both the package and class names are used to describe each class
			}
	
			//construct hierarchical software event log. 
			//the construction of N-nesting level hierarchy does not rely on the instance identification. 
			HSoftwareEventLog hseLog = HierarchicalSoftwareEventLogConstruction.constructHierarchicalLog(maximalNestingDepth, factory, componentClassSet, 
			ConstructSoftwareComponentLog.generatingComponentSoftwareEventLog(component, componentClassSet, originalLog, factory), 
			Xloginfo, component);
			
			//software component behavior discovery, HPN
			HierarchicalMultiinstancePetriNet hpn = MineHierarchicalPetriNet.mineHierarchicalPetriNet(context, hseLog, IMparameters);
			
			componenModels.addComponentModel(component, hpn);
						
		}	
		return componenModels;
	}

}
