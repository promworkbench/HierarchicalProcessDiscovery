package software.processmining.componentbehaviordiscovery;

import java.util.HashMap;
import java.util.HashSet;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.DirectedGraphElement;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetImpl;

/*
 * this plugin aims to transform a hierarchical petri net to a flat one in a recursive way. 
 */

@Plugin(
		name = "Convert a Hierarchical Multi-instanxe Petri Net to a Flat Petri Net",// plugin name
		
		returnLabels = {"Flat Petri Net"}, //return labels
		returnTypes = {Petrinet.class},//return class
		
		//input parameter labels, corresponding with the second parameter of main function
		parameterLabels = {"Petri Net"},
		
		userAccessible = true,
		help = "This plugin aims to convert a hierarchical petri net to a flat one." 
		)
public class ConvertHierarchicalPN2FlatPN {
	@UITopiaVariant(
	        affiliation = "TU/e", 
	        author = "Ying Wang,Cong liu", 
	        email = "c.liu.3@tue.nl;liucongchina@163.com"
	        )
	@PluginVariant(
			variantLabel = "Convert Hierarchical Petri Net to Flat Petri Net, default",
			// the number of required parameters, {0} means one input parameter
			requiredParameterLabels = {0}
			)
	public Petrinet convertHPNtoPN(UIPluginContext context, HierarchicalMultiinstancePetriNet hpn) 
	{
		//define the converted flat petri net as a global variable
		final Petrinet pn =new PetrinetImpl("Cloned flat PN");
		
		//clone the top-level pn to the new flat pn. this function also returns a mapping from the old pn tp the cloned pn. 
		ConvertPetriNet2PNwithLifeCycle.clonePetriNet(hpn.getPn(), pn);

		//HashMap<XEventClass, HierarchicalPetriNet> XEventClass2hpn =hpn.getXEventClass2hpn();
		
		//嵌套变迁名和类的集合
		HashMap<String, XEventClass> eventClassName2EventClass = new HashMap<>();
		
		//get all nested transition in the current level. 
		//获得所有的嵌套变迁
		HashSet<String> nestedTransitionLabels = new HashSet<>();
		for(XEventClass xe: hpn.getXEventClass2hpn().keySet())
		{
			nestedTransitionLabels.add(xe.toString());
			eventClassName2EventClass.put(xe.toString(), xe);
		}
		
		//get all transition that are not invisible. 
		//invisible:
		HashSet<Transition> transitionSet = new HashSet<>();
		for(Transition t :pn.getTransitions())
		{
			
			if(!t.isInvisible()) // not invisible transition
			{
				transitionSet.add(t);
			}
		}

		for(Transition t: transitionSet)
		{
			//for normal transitions that are not nested transitions. 
			
			if(nestedTransitionLabels.contains(t.getLabel()+"_2")) {
				convertHPNtoPNRecursively(pn, t,nestedTransitionLabels,eventClassName2EventClass ,hpn.getXEventClass2hpn().get(eventClassName2EventClass.get(t.toString()+"_2")), "2");
			}
			
			else if(nestedTransitionLabels.contains(t.getLabel())) {
				convertHPNtoPNRecursively(pn, t, nestedTransitionLabels,eventClassName2EventClass, hpn.getXEventClass2hpn().get(eventClassName2EventClass.get(t.toString())), "1");
			}
			else {
				convertNormalTransition(pn, t,nestedTransitionLabels);
			}
		}
		
		return pn;
	}
		
	/*Input: 
	 * pn: is the flat Petri net 
	 * nestedTransition: is the nested transition in the flat pn. 
	 * hpn: is the hpn that refered to nestedTransition
	 * 
	 * construct pn from hpn recursively
	 * !!!Requirement: all sub-pn in hpn are with sink and source places. 
	 */
	
	//Flat Petri net 
	public static void convertHPNtoPNRecursively(final Petrinet pn, Transition nestedTransition,HashSet<String> nestedTransitionLabels,HashMap<String, XEventClass> eventClassName2EventClass,  HierarchicalMultiinstancePetriNet hpn,String nestFlag)
	{
		
		//transform the top-level pn in hpn to a pn with lifecycle transitions. 
		
		//define the converted flat petri net as a global variable
		Petrinet toplevel_pn =new PetrinetImpl("top-level pn");
		
		//clone the top-level pn to the new flat pn. 
		 
		ConvertPetriNet2PNwithLifeCycle.clonePetriNet(hpn.getPn(), toplevel_pn);
		
		//get all transition that are not invisible. 
		HashSet<Transition> transitionSet = new HashSet<>();
		for(Transition t :toplevel_pn.getTransitions())
		{
			if(!t.isInvisible()) // not invisible transition
			{
				transitionSet.add(t);
			}
		}
		
		for(Transition t: transitionSet)
		{
			//for normal transitions that are not nested transitions. 

			if(!nestedTransitionLabels.contains(t.getLabel()))
			{
				convertNormalTransition(toplevel_pn, t,nestedTransitionLabels);
			}
			else//for nested transitions
			{
				//recursively add sub-nested pn

				convertHPNtoPNRecursively(toplevel_pn, t,nestedTransitionLabels, eventClassName2EventClass,hpn.getXEventClass2hpn().get(eventClassName2EventClass.get(t.toString())), nestFlag);
			}
			
		}
				
		//connect the input pn with the top-level lifecycle pn of hpn
		connectTopLevelPN(pn, nestedTransition, toplevel_pn, nestFlag);
	}
	
	// the toplevel_pn should have single source and single sink place
	//连接顶层Petri net
	public static void connectTopLevelPN(final Petrinet pn, Transition nestedTransition, Petrinet toplevel_pn, String nestFlag)
	{
		
		String nestedName = nestedTransition.getLabel();

		//get the pre place set of nestedTransition
		HashSet<Place> prePlaceSet = new HashSet<>();
		for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge :pn.getInEdges(nestedTransition))
		{
			prePlaceSet.add((Place)edge.getSource());
			//remove the current edge
			pn.removeArc((Place)edge.getSource(), nestedTransition);
		}
		
		//get the post place set of t
		HashSet<Place> postPlaceSet = new HashSet<>();
		for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge :pn.getOutEdges(nestedTransition))
		{
			postPlaceSet.add((Place)edge.getTarget());
			//remove the current edge
			pn.removeArc(nestedTransition, (Place)edge.getTarget());
		}
		
				
		//create start and end nested transition
		//
		Transition startNestedTransition = pn.addTransition(nestedName+"+Start");
		Transition endNestedTransition= pn.addTransition(nestedName+"+Complete");
		
		//remove t, 
		pn.removeTransition(nestedTransition);
		
		//add arcs from startTransition to each pre place
		for(Place preP: prePlaceSet)
		{
			pn.addArc(preP, startNestedTransition);
		}
		
		//add arcs from endTransition to each post place
		for(Place postP: postPlaceSet)
		{
			pn.addArc(endNestedTransition, postP);
		}
		
				
		/*
		 * add toplevel_pn to flat pn
		 */
		//add all elements in toplevel_pn to pn
		//keep the mapping from toplevel_pn elements to pn. 
		HashMap<DirectedGraphElement, DirectedGraphElement> mapping = new HashMap<DirectedGraphElement, DirectedGraphElement>();

		//copy transitions. 
		for (Transition t : toplevel_pn.getTransitions()) {
			//------------------------------------------------------
			if(t.getLabel().contains("_")) {
				String name1 = t.getLabel().substring(0, t.getLabel().indexOf('_'));
				Transition copy = pn.addTransition(name1, null);
			}
			Transition copy = pn.addTransition(t.getLabel(), null);
			copy.setInvisible(t.isInvisible());
			mapping.put(t, copy);
		}
		
		//copy places. 
		for (Place p : toplevel_pn.getPlaces()) {
			Place copy = pn.addPlace(p.getLabel(), null);
			mapping.put(p, copy);
		}
		
		
		
		//add arcs
		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> a : toplevel_pn.getEdges()) {
			if(a.getSource() instanceof Transition)
			{
				org.processmining.models.graphbased.directed.petrinet.elements.Arc clonedArc = pn.addArc((Transition) mapping.get(a.getSource()), (Place) mapping.get(a.getTarget()));
				mapping.put(a, clonedArc);
			}
			else{
				org.processmining.models.graphbased.directed.petrinet.elements.Arc clonedArc = pn.addArc((Place) mapping.get(a.getSource()), (Transition) mapping.get(a.getTarget()));
				mapping.put(a, clonedArc);
			}
		}
		
		//add edge from startNestedTransition of pn to the source place of toplevel_pn
		pn.addArc(startNestedTransition, (Place) mapping.get(getSourcePlace(toplevel_pn)));
		//add edge from the sink place of toplevel_pn to endNestedTransition of pn
		pn.addArc((Place) mapping.get(getSinkPlace(toplevel_pn)), endNestedTransition);
		
		if(nestFlag.equals("2")) {
			//
			Transition invision1 = pn.addTransition("T1");
			Transition invision2= pn.addTransition("T2");
			invision1.setInvisible(true);
			invision2.setInvisible(true);
			
			//
			//T1->Px   Px->T2
			//
			Place Px = pn.addPlace("Px");

			Place Pe = pn.addPlace("Pe");
			
			pn.addArc(invision1, Px);
			pn.addArc(Px, invision2);
			
			pn.addArc(invision1, (Place) mapping.get(getSourcePlace(toplevel_pn)));
			pn.addArc((Place) mapping.get(getSinkPlace(toplevel_pn)), invision2);
			//
			pn.addArc(invision1, Pe);
			pn.addArc(Pe,invision1);
			pn.addArc(Pe,invision2);
			pn.addArc(invision2, Pe);
			//

			pn.addArc(startNestedTransition, Pe);
			pn.addArc(Pe, endNestedTransition);

			
		}

	}
	
	//
	public static void convertNormalTransition(final Petrinet pn, Transition t,HashSet<String> nestedTransitionLabels)
	{
		//get the pre place set of t
		HashSet<Place> prePlaceSet = new HashSet<>();
		for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge :pn.getInEdges(t))
		{
			prePlaceSet.add((Place)edge.getSource());
			//remove the current edge
			pn.removeArc((Place)edge.getSource(), t);
		}
		
		//get the post place set of t
		HashSet<Place> postPlaceSet = new HashSet<>();
		for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge :pn.getOutEdges(t))
		{
			postPlaceSet.add((Place)edge.getTarget());
			//remove the current edge
			pn.removeArc(t, (Place)edge.getTarget());
		}
		
		//create start and end transition
		Transition startTransition = pn.addTransition(t.getLabel()+"+Start");
		Transition endTransition= pn.addTransition(t.getLabel()+"+Complete");
		
		//remove t, 
		pn.removeTransition(t);
		if(!nestedTransitionLabels.contains(t.getLabel())) {
			//add arcs from startTransition to each pre place
			for(Place preP: prePlaceSet)
			{
				pn.addArc(preP, startTransition);
			}
			
			//add arcs from endTransition to each post place
			for(Place postP: postPlaceSet)
			{
				pn.addArc(endTransition, postP);
			}
			
			//create the subset, i.e., ts->p->tc
			//add a place to connect startTransition and endTransition
			Place connectionP = pn.addPlace("connect");
			pn.addArc(startTransition, connectionP);
			pn.addArc(connectionP, endTransition);
		}
		
	}
	
	//SourcePlace
	public static Place getSourcePlace(Petrinet pn)
	{
		for (final Place p : pn.getPlaces()) 
		{
			int sourceFlag=0;
			//check if the p is the source place 
			for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : pn.getEdges())
			{
				//if there is no incoming arc, this place is a start place
				if(edge.getTarget().getId().equals(p.getId()))
				{
					sourceFlag=1;//the current place is not source
					break;
				}
			}
			if(sourceFlag==0)
			return p;
		}	
		return null;
	}
	
	//SinkPlace
	public static Place getSinkPlace(Petrinet pn)
	{
		for (final Place p : pn.getPlaces()) 
		{
			int sinkFlag=0;
			//check if the p is the source place 
			for(PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : pn.getEdges())
			{
				//if there is no out-going arc, this place is a end place
				if(edge.getSource().getId().equals(p.getId()))
				{
					sinkFlag=1;//the current place is not sink
					break;
				}
			}
			if(sinkFlag==0)
			return p;
		}	
		return null;
	}
	
//	/* 
//	 * get the key according to value. 
//	 */
//	 public static DirectedGraphElement getKeyFromValue(HashMap<DirectedGraphElement, DirectedGraphElement> mapping, DirectedGraphElement value) 
//	 {
//	    for (DirectedGraphElement e : mapping.keySet()) {
//	      if (mapping.get(e).equals(value)) {
//	    	  System.out.println("eee: "+e.toString());
//	        return e;
//	      }
//	    }
//	    return null;
//	  }
}
