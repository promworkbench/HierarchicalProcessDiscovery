package software.processmining.componentbehaviordiscovery;
/*
 * This plugin aims to extract the component log without instance identification, and also adding lifecycle transition. 
 */

import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.ui.widgets.helper.ProMUIHelper;
import org.processmining.framework.util.ui.widgets.helper.UserCancelledException;

import UtilityClasses.ClassClass;
import UtilityClasses.ComponentConfig;
import UtilityFunctions.InitializeSoftwareEventLog;
import openXESsoftwareextension.XSoftwareExtension;

@Plugin(
		name = "Single Component Log Extraction (no Instance Identification)",// plugin name
		
		returnLabels = {"Software Event Log"}, //return labels
		returnTypes = {XLog.class},//return class, a set of component to hpns 
		
		//input parameter labels, corresponding with the second parameter of main function
		parameterLabels = {"Software Event Log", "Component Configuration"},
		
		userAccessible = true,
		help = "This plugin aims  to extract the component log without instance identification." 
		)
public class SingleComponentLogExtractionPlugin_NoInstanceIdentification {
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
				
				return convertLogwithLifecycle.addingLifecycleInformation(context, generatingComponentSoftwareEventLognoInstanceIdentification(component, componentClassSet, originalLog, factory));
			}
		}
		return null;	
	}
	
	/**
	 * this class aims to extracting software event log of each component without identifying component/interface instances. 
	 * it returns a flat event log.
	 * @param com2class
	 * @param com2classList
	 * @param originalLog
	 * @return
	 */
	public static XLog generatingComponentSoftwareEventLognoInstanceIdentification(String component, Set<String> classSet, XLog originalLog, XFactory factory)
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
			
			componentLog.add(tempTrace);
		}
		return componentLog;
	}
	
}
