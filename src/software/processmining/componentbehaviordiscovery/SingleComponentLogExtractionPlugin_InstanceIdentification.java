package software.processmining.componentbehaviordiscovery;

import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.ui.widgets.helper.ProMUIHelper;
import org.processmining.framework.util.ui.widgets.helper.UserCancelledException;

import UtilityClasses.ClassClass;
import UtilityClasses.ComponentConfig;

/*
 * This plugin aims to extract the component log after instance identification, and also adding lifecycle transition. 
 */

@Plugin(
		name = "Single Component Log Extraction (with Instance Identification)",// plugin name
		
		returnLabels = {"Software Event Log"}, //return labels
		returnTypes = {XLog.class},//return class, a set of component to hpns 
		
		//input parameter labels, corresponding with the second parameter of main function
		parameterLabels = {"Software Event Log", "Component Configuration"},
		
		userAccessible = true,
		help = "This plugin aims  to extract the component log after instance identification." 
		)
public class SingleComponentLogExtractionPlugin_InstanceIdentification {
	@UITopiaVariant(
	        affiliation = "TU/e", 
	        author = "Cong liu", 
	        email = "c.liu.3@tue.nl OR liucongchina@gmail.com"
	        )
	@PluginVariant(
			variantLabel = "Component Log Extraction Behavior Model, default",
			// the number of required parameters, 0 means the first input parameter, 1 means the second input parameter
			requiredParameterLabels = { 0,1 })
	public XLog componentLogExtraction(UIPluginContext context, XLog originalLog, ComponentConfig comConfig) throws UserCancelledException
	{			
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
//		XLogInfo Xloginfo = XLogInfoFactory.createLogInfo(originalLog, IMparameters.getClassifier());
				
		
		//add lifecycle information to component log. 
		ConvertSoftwareEventLog2LogwithLifecycle convertLogwithLifecycle = new ConvertSoftwareEventLog2LogwithLifecycle();
				
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
				
				return convertLogwithLifecycle.addingLifecycleInformation(context, ConstructSoftwareComponentLog.generatingComponentSoftwareEventLog(component, componentClassSet, originalLog, factory));
			}
		}
		
		return null;
				
	}
}
