package UtilityFunctions;

import org.deckfour.xes.classification.XEventAttributeClassifier;
import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;

import openXESsoftwareextension.XSoftwareExtension;

public class InitializeSoftwareEventLog {
	// initialize main software event loglog
	public static XLog initialize(XFactory factory, String logName)
	{
		//add the log name		
		XLog log = factory.createLog();
		log.getAttributes().put(XConceptExtension.KEY_NAME, new XAttributeLiteralImpl(XConceptExtension.KEY_NAME, logName));
		
		//create standard extension
		XExtension conceptExtension = XConceptExtension.instance();
		//XExtension organizationalExtension = XOrganizationalExtension.instance();
		XExtension timeExtension = XTimeExtension.instance();
		XExtension lifecycleExtension=XLifecycleExtension.instance();
		XExtension softwareExtension=XSoftwareExtension.instance();
		
		// create extensions
		log.getExtensions().add(conceptExtension);
		log.getExtensions().add(softwareExtension);
		log.getExtensions().add(lifecycleExtension);
		log.getExtensions().add(timeExtension);
		
		// create trace level global attributes
		XAttribute xtrace = new XAttributeLiteralImpl(XConceptExtension.KEY_NAME, "DEFAULT"); 
		log.getGlobalTraceAttributes().add(xtrace);

		// create event level global attributes		

		log.getGlobalEventAttributes().add(XConceptExtension.ATTR_NAME);
		log.getGlobalEventAttributes().add(XSoftwareExtension.ATTR_CLASS);
		log.getGlobalEventAttributes().add(XSoftwareExtension.ATTR_PACKAGE);
		log.getGlobalEventAttributes().add(XSoftwareExtension.ATTR_CLASSOBJ);
		log.getGlobalEventAttributes().add(XSoftwareExtension.ATTR_COMPONENT);
		log.getGlobalEventAttributes().add(XSoftwareExtension.ATTR_CALLERMETHOD);
		log.getGlobalEventAttributes().add(XSoftwareExtension.ATTR_CALLERCLASS);
		log.getGlobalEventAttributes().add(XSoftwareExtension.ATTR_CALLERPACKAGE);
		log.getGlobalEventAttributes().add(XSoftwareExtension.ATTR_CALLERCLASSOBJ);
		log.getGlobalEventAttributes().add(XSoftwareExtension.ATTR_CALLERCOMPONENT);
		log.getGlobalEventAttributes().add(XSoftwareExtension.ATTR_STARTTIMENANO);
		log.getGlobalEventAttributes().add(XSoftwareExtension.ATTR_ENDTIMENANO);
		log.getGlobalEventAttributes().add(XLifecycleExtension.ATTR_TRANSITION);
		
		// create classifiers based on global attribute		

		XEventAttributeClassifier classifierActivity = new XEventAttributeClassifier("Method Call Identifier", 
				 XConceptExtension.KEY_NAME, XSoftwareExtension.KEY_CLASS, XSoftwareExtension.KEY_PACKAGE);
		//log.getClassifiers().add(classifierActivityObject);
		log.getClassifiers().add(classifierActivity);
		
		return log;
	}
}
