package software.processmining.componentbehaviordiscovery;

/**
 * this class defines the hierarchical Petri net class.
 */
import java.util.HashMap;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetImpl;

public class HierarchicalMultiinstancePetriNet {
	// mapping from XEventClass to hierarchical petri net
	// XEventClass relies on the classifier
	private HashMap<XEventClass, HierarchicalMultiinstancePetriNet> XEventClass2hpn;
	private Petrinet pn;
	
	//constructor
	public HierarchicalMultiinstancePetriNet()
	{
		this.XEventClass2hpn = new HashMap<XEventClass, HierarchicalMultiinstancePetriNet>();
		this.pn = new PetrinetImpl("");
	}
	
	public HashMap<XEventClass, HierarchicalMultiinstancePetriNet> getXEventClass2hpn() {
		return XEventClass2hpn;
	}

	public void setXEventClass2hpn(HashMap<XEventClass, HierarchicalMultiinstancePetriNet> xEventClass2hpn) {
		XEventClass2hpn = xEventClass2hpn;
	}

	public Petrinet getPn() {
		return pn;
	}

	public void setPn(Petrinet pn) {
		this.pn = pn;
	}
}
