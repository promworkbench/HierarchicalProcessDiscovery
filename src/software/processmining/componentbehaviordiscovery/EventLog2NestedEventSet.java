package software.processmining.componentbehaviordiscovery;

import java.util.HashSet;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;

//this class is used to store the mainLog and its nested event set
public class EventLog2NestedEventSet {
	
	// the main sfotware event log. 
	private XLog mainLog =null; 
	
	//nested event set, to avoid repetive elements
	private HashSet<XEvent> nestedEventSet = new HashSet<XEvent>();

	public XLog getMainLog() {
		return mainLog;
	}

	public HashSet<XEvent> getNestedEventSet() {
		return nestedEventSet;
	}

	public void setNestedEventSet(HashSet<XEvent> nestedEventSet) {
		this.nestedEventSet = nestedEventSet;
	}

	public void setMainLog(XLog mainLog) {
		this.mainLog = mainLog;
	}	
}
