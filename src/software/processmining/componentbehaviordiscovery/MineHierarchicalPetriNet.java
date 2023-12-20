package software.processmining.componentbehaviordiscovery;

import java.util.HashMap;
import java.util.List;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.packages.PackageManager.Canceller;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.plugins.IMPetriNet;

import UtilityFunctions.OrderingEventsNano;
import openXESsoftwareextension.XSoftwareExtension;

/**
 * this class aims to discover a hierarchical petri net from a hierarchical software event log. 
 * it is implemented in a recursive manner following the hierarchical structure of the software event log. 
 * @author cliu3
 *
 */
public class MineHierarchicalPetriNet {
	
	// the input is a hierarchical event log
	public static HierarchicalMultiinstancePetriNet mineHierarchicalPetriNet(PluginContext context, HSoftwareEventLog hseLog, MiningParameters parameters) throws ConnectionCannotBeObtained
	{
		HierarchicalMultiinstancePetriNet hpn = new HierarchicalMultiinstancePetriNet();
		
		//call the ordering log before mining and use the inductive miner for discovering. 
		Object[] objs =IMPetriNet.minePetriNet(OrderingEventsNano.ordering(hseLog.getMainLog(), XSoftwareExtension.KEY_STARTTIMENANO), 
				parameters, new Canceller() {
			public boolean isCancelled() {
				return false;
			}
		});
		
//		// use Petri net reduction rules, based on Murata rules, i.e., Reduce Silent Transitions, Preserve Behavior
//		Murata  murata = new Murata ();
//		MurataParameters paras = new MurataParameters();
//		paras.setAllowFPTSacredNode(false);
//		Petrinet pn =(Petrinet) murata.run(context, (Petrinet)objs[0], (Marking)objs[1], paras)[0];
//		hpn.setPn(pn);
		
//		//make sure the source place do not have input arcs, e.g., single entry!
		hpn.setPn(addingArtifitialSoucePlace((Petrinet)objs[0],(Marking)objs[1]));

		
		//to deal with its sub-mapping from eventclass to hierarchical petri net
		HashMap<XEventClass, HierarchicalMultiinstancePetriNet> XEventClass2hpn =new HashMap<XEventClass, HierarchicalMultiinstancePetriNet>();
		
		if (hseLog.getSubLogMapping().keySet().size()>0)
		{
			for(XEventClass key:hseLog.getSubLogMapping().keySet())
			{
				XEventClass2hpn.put(key, mineHierarchicalPetriNet(context,hseLog.getSubLogMapping().get(key), parameters));
			}
		}
		
		hpn.setXEventClass2hpn(XEventClass2hpn);
				
		return hpn;
	}
	
	public static Petrinet addingArtifitialSoucePlace(final Petrinet pn, Marking initialM)
	{
		//get all places in the initial marking. 
		List<Place> places = initialM.toList();
//		HashSet<String> placeNames = new HashSet<>();
//		for(Place p: places)
//		{
//			placeNames.add(p.getLabel());
//		}
//		System.out.println(placeNames);

		
		int sourceFlag =1;
		for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : pn.getEdges())
		{
			System.out.println(edge.getTarget().getLabel());
			// if there exist an edge with a place that included in the marking, then we need to add artificial source place and transition.  
			//if(placeNames.contains(edge.getTarget().getLabel()))
			if(places.contains(edge.getTarget()))
			{
				sourceFlag=0;
				break;
			}
		}
		
		if(sourceFlag==0)//
		{
			Place sourceP= pn.addPlace("lc source");
			Transition sourceT = pn.addTransition("lc transition");
			sourceT.setInvisible(true);
			
			pn.addArc(sourceP, sourceT);
			pn.addArc(sourceT, places.get(0));
		}
		
		return pn;
	}
}
