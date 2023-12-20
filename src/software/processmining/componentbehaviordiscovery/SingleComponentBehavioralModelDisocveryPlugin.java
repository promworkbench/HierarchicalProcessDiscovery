package software.processmining.componentbehaviordiscovery;

import java.util.HashSet;
import java.util.Set;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
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

@Plugin(
		name = "Single Component Behavior Discovery",// plugin name
		
		returnLabels = {"Software Component Behavior"}, //return labels
		returnTypes = {HierarchicalMultiinstancePetriNet.class},//return class, a set of component to hpns 
		
		//input parameter labels, corresponding with the second parameter of main function
		parameterLabels = {"Software Event Log", "Component Configuration"},
		
		userAccessible = true,
		help = "This plugin aims to discover the component behavior models of software." 
		)
public class SingleComponentBehavioralModelDisocveryPlugin {
	@UITopiaVariant(
	        affiliation = "TU/e", 
	        author = "Cong liu", 
	        email = "c.liu.3@tue.nl OR liucongchina@163.com"
	        )
	@PluginVariant(
			variantLabel = "Single Component Behavior Discovery for one Component, default",
			// the number of required parameters, 0 means the first input parameter, 1 means the second input parameter
			requiredParameterLabels = { 0,1 })
	public HierarchicalMultiinstancePetriNet componentBehaviorDiscovery(UIPluginContext context, XLog originalLog, ComponentConfig comConfig) throws ConnectionCannotBeObtained, UserCancelledException 
	{
		HierarchicalMultiinstancePetriNet hpn = new HierarchicalMultiinstancePetriNet();
		//set the inductive miner parameters, the original log is used to set the classifier
		IMMiningDialog dialog = new IMMiningDialog(originalLog);
		InteractionResult result = context.showWizard("Configure Parameters for Inductive Miner (used for all interface models)", true, true, dialog);
		if (result != InteractionResult.FINISHED) {
			return null;
		}
		// the mining parameters are set here 
		MiningParameters IMparameters = dialog.getMiningParameters(); //IMparameters.getClassifier()
		
		//select the maximal nesting depth for the discovered hierarchical petri net. 
		String [] depth = new String[20];
		depth[0]="1";
		depth[1]="2";
		depth[2]="3";
		depth[3]="4";
		depth[4]="5";
		depth[5]="6";
		depth[6]="7";
		depth[7]="8";
		depth[8]="9";
		depth[9]="10";
		depth[10]="11";
		depth[11]="12";
		depth[12]="13";
		depth[13]="14";
		depth[14]="15";
		depth[15]="16";
		depth[16]="17";
		depth[17]="18";
		depth[18]="19";
		depth[19]="20";
		int maximalNestingDepth =Integer.parseInt(ProMUIHelper.queryForObject(context, "Select the maximal nesting depth", depth));
		context.log("Maximal Interface Behaioval Model Depth is: "+maximalNestingDepth, MessageLevel.NORMAL);	
		System.out.println("Selected nesting depth: "+maximalNestingDepth);
			
		//select the component to discover its behavioral model. 
		String [] components = new String[comConfig.getAllComponents().size()];
		int i =0;
		for(String  com: comConfig.getAllComponents())
		{
			components[i]=com;
			i++;
		}
		String selectedComponents = ProMUIHelper.queryForString(context, "Select the components", components);
		
		//create factory to create Xlog, Xtrace and Xevent.
		XFactory factory = new XFactoryNaiveImpl();
		XLogInfo Xloginfo = XLogInfoFactory.createLogInfo(originalLog, IMparameters.getClassifier());
		
		//identify component instances, and construct software event log for each component.
		for (String component: comConfig.getAllComponents())
		{
			if(selectedComponents.equals(component))
			{ 
				// create class set of the current component
				Set<String> componentClassSet = new HashSet<>();
				for(ClassClass c: comConfig.getClasses(component))
				{
					componentClassSet.add(c.toString());// both the package and class names are used to describe each class
				}
				
				HSoftwareEventLog hseLog = HierarchicalSoftwareEventLogConstruction.constructHierarchicalLog(maximalNestingDepth, factory, componentClassSet, 
						ConstructSoftwareComponentLog.generatingComponentSoftwareEventLog(component, componentClassSet, originalLog, factory), 
						Xloginfo, component);
						
				//convert the hierarchical event log to flat log, with start and complete. pay attention to the classifier
				//context.getProvidedObjectManager().createProvidedObject("Flat Event Log", res, XLog.class, context);

				//software component behavior discovery, HPN
				hpn = MineHierarchicalPetriNet.mineHierarchicalPetriNet(context, hseLog, IMparameters);
				break;
			}
		}
		
		return hpn;
	}
}
