package software.processmining.componentbehaviordiscovery;

import java.util.HashMap;
import java.util.Set;



/**
 * this class defines the mapping from component name (a string) to its hierarchical petri net model. 
 * @author cliu3
 *
 */
public class ComponentModelSet {

	private HashMap<String, HierarchicalMultiinstancePetriNet> component2HPN; 
	
	public ComponentModelSet()
	{
		component2HPN= new HashMap<String, HierarchicalMultiinstancePetriNet>();
	}
	
	/**
	 * adding com --> hpn 
	 * @param com
	 * @param hpn
	 */
	public void addComponentModel(String com, HierarchicalMultiinstancePetriNet hpn) {
		component2HPN.put(com, hpn);
	}
	
	/**
	 * get the component set
	 * @return
	 */
	public Set<String> getComponentSet()
	{
		return component2HPN.keySet();
	}
	
	/**
	 * get the hpn of a component
	 * @param com
	 * @return
	 */
	public HierarchicalMultiinstancePetriNet getComponentHPN(String com) 
	{
		return component2HPN.get(com);
	}

	/**
	 * the number of components
	 * @return
	 */
	public int size()
	{
		return component2HPN.size();
	}
}
