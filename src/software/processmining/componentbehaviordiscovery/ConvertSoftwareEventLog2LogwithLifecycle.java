package software.processmining.componentbehaviordiscovery;

import org.deckfour.xes.classification.XEventAttributeClassifier;
import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

import UtilityFunctions.OrderingEventsNano;
import openXESsoftwareextension.XSoftwareExtension;

/*
 * this plugin aims to transform a software event log to one with lifecycle
 * information. Note1: adding a classifier: XEventClassifier STANDARD_CLASSIFIER
 * = new XEventAttributeClassifier("MXML Legacy Classifier",
 * XConceptExtension.KEY_NAME, XLifecycleExtension.KEY_TRANSITION); Note2:
 * remove the caller information (these log can be used for hierarchical
 * discovery with lifecycle information).
 */
@Plugin(name = "Convert Software Event Log to Log with Lifecycle Information", // plugin name

		returnLabels = { "Transactional Software Event Log " }, //return labels
		returnTypes = { XLog.class }, //return class

		//input parameter labels, corresponding with the second parameter of main function
		parameterLabels = { "Software Event Log" },

		userAccessible = true, help = "This plugin aims to add lifecycle information to a software event log.")
public class ConvertSoftwareEventLog2LogwithLifecycle {

	@UITopiaVariant(affiliation = "TU/e", author = "Cong liu", email = "c.liu.3@tue.nl OR liucongchina@163.com")
	@PluginVariant(variantLabel = "Convert Software Event Log to Log with Lifecycle Information, default",
			// the number of required parameters, {0} means one input parameter
			requiredParameterLabels = { 0 })
	public XLog addingLifecycleInformation(UIPluginContext context, XLog softwareLog) {
		XFactory factory = new XFactoryNaiveImpl();
		XLog lifecycleLog = initializeSoftwareLifecycleLog(factory,
				XConceptExtension.instance().extractName(softwareLog) + "_Lifecycle");

		//create lifecycle event.
		for (XTrace trace : softwareLog) {
			XTrace lifecycleTrace = factory.createTrace();
			XConceptExtension.instance().assignName(lifecycleTrace, XConceptExtension.instance().extractName(trace));
			for (XEvent e : trace) {
				//create lifecycle events. 
				XEvent startEvent = factory.createEvent();
				XConceptExtension.instance().assignName(startEvent, XConceptExtension.instance().extractName(e));
				XSoftwareExtension.instance().assignClass(startEvent, XSoftwareExtension.instance().extractClass(e));
				XSoftwareExtension.instance().assignPackage(startEvent,
						XSoftwareExtension.instance().extractPackage(e));
				XSoftwareExtension.instance().assignClassObject(startEvent,
						XSoftwareExtension.instance().extractClassObject(e));
				XLifecycleExtension.instance().assignTransition(startEvent, "Start");
				XSoftwareExtension.instance().assignStarttimenano(startEvent,
						XSoftwareExtension.instance().extractStarttimenano(e));

				XEvent completeEvent = factory.createEvent();
				XConceptExtension.instance().assignName(completeEvent, XConceptExtension.instance().extractName(e));
				XSoftwareExtension.instance().assignClass(completeEvent, XSoftwareExtension.instance().extractClass(e));
				XSoftwareExtension.instance().assignPackage(completeEvent,
						XSoftwareExtension.instance().extractPackage(e));
				XSoftwareExtension.instance().assignClassObject(completeEvent,
						XSoftwareExtension.instance().extractClassObject(e));
				XLifecycleExtension.instance().assignTransition(completeEvent, "Complete");
				XSoftwareExtension.instance().assignStarttimenano(completeEvent,
						XSoftwareExtension.instance().extractEndtimenano(e));

				lifecycleTrace.add(startEvent);
				lifecycleTrace.add(completeEvent);

			}

			//order lifecycleTrace by start timestamp
			lifecycleLog.add(OrderingEventsNano.orderEventLogwithTimestamp(lifecycleTrace,
					XSoftwareExtension.KEY_STARTTIMENANO));
		}
		return lifecycleLog;
	}

	// initialize main software event loglog
	public static XLog initializeSoftwareLifecycleLog(XFactory factory, String logName) {
		//add the log name		
		XLog lifecycleLog = factory.createLog();
		XConceptExtension.instance().assignName(lifecycleLog, logName);
		//log.getAttributes().put(XConceptExtension.KEY_NAME, new XAttributeLiteralImpl(XConceptExtension.KEY_NAME, logName));

		//create standard extension
		XExtension conceptExtension = XConceptExtension.instance();
		//XExtension organizationalExtension = XOrganizationalExtension.instance();
		//XExtension timeExtension = XTimeExtension.instance();
		XExtension lifecycleExtension = XLifecycleExtension.instance();
		XExtension softwareExtension = XSoftwareExtension.instance();

		// create extensions
		lifecycleLog.getExtensions().add(conceptExtension);
		lifecycleLog.getExtensions().add(softwareExtension);
		lifecycleLog.getExtensions().add(lifecycleExtension);
		//log.getExtensions().add(timeExtension); 

		// create trace level global attributes
		XAttribute xtrace = new XAttributeLiteralImpl(XConceptExtension.KEY_NAME, "DEFAULT");
		lifecycleLog.getGlobalTraceAttributes().add(xtrace);

		// create event level global attributes		
		lifecycleLog.getGlobalEventAttributes().add(XConceptExtension.ATTR_NAME);
		lifecycleLog.getGlobalEventAttributes().add(XSoftwareExtension.ATTR_CLASS);
		lifecycleLog.getGlobalEventAttributes().add(XSoftwareExtension.ATTR_PACKAGE);
		lifecycleLog.getGlobalEventAttributes().add(XSoftwareExtension.ATTR_CLASSOBJ);
		lifecycleLog.getGlobalEventAttributes().add(XSoftwareExtension.ATTR_STARTTIMENANO);
		lifecycleLog.getGlobalEventAttributes().add(XLifecycleExtension.ATTR_TRANSITION);

		// create classifiers based on global attribute		
		//Note that we have two possible ways to create these classifier..
//		XEventAttributeClassifier methodCallClassifer = new XEventAndClassifier(
//				new XEventAttributeClassifier("Concept name", XConceptExtension.KEY_NAME),
//				new XEventAttributeClassifier("Software class", XSoftwareExtension.KEY_CLASS),
//				new XEventAttributeClassifier("Software package", XSoftwareExtension.KEY_PACKAGE));
//
//		XEventAttributeClassifier methodCallClassiferLifecycle = new XEventAndClassifier(
//				new XEventAttributeClassifier("Concept name", XConceptExtension.KEY_NAME),
//				new XEventAttributeClassifier("Software class", XSoftwareExtension.KEY_CLASS),
//				new XEventAttributeClassifier("Software package", XSoftwareExtension.KEY_PACKAGE), 
//				new XEventAttributeClassifier("Lifecycle", XLifecycleExtension.KEY_TRANSITION));

		XEventAttributeClassifier methodCallClassifer = new XEventAttributeClassifier(
				"Method Call Identifier", XConceptExtension.KEY_NAME, XSoftwareExtension.KEY_CLASS,
				XSoftwareExtension.KEY_PACKAGE);
		
		XEventAttributeClassifier methodCallClassiferLifecycle = new XEventAttributeClassifier(
				"Method Call Identifier Lifecycle", XConceptExtension.KEY_NAME, XSoftwareExtension.KEY_CLASS,
				XSoftwareExtension.KEY_PACKAGE, XLifecycleExtension.KEY_TRANSITION);
//		log.getClassifiers().add(classifierActivityObject);
		lifecycleLog.getClassifiers().add(methodCallClassifer);
		lifecycleLog.getClassifiers().add(methodCallClassiferLifecycle);

		return lifecycleLog;
	}
}
