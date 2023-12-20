package UtilityClasses;

import java.util.HashMap;
import java.util.Set;


/*
 * component is a string (i.e., the name of the component), 
 * and class is represented as class.method name.  
 */
public class ComponentConfig {
	HashMap<String, Set<ClassClass>> com2class;
	
	public ComponentConfig()
	{
		//a component --> a set of classes that belongs to the current component
		com2class =new HashMap<String, Set<ClassClass>>();
	}
	
	public void add(String component, Set<ClassClass> classes)
	{
		com2class.put(component, classes);
	}
	
	public Set<String> getAllComponents()
	{
		return com2class.keySet();
	}
	
	public Set<ClassClass> getClasses(String component)
	{
		return com2class.get(component);
	}
	
	public void removeComponent(String com)
	{
		com2class.remove(com);
	}
}
