package UtilityFunctions;

/*
 * this class aims to provide basic functionality to extract method (last), class(last or second last), and 
 */
public class ExtractMethodClassPackage {

	/*
	 * get last
	 * E.g., a.b.c.d==> return d
	 */
	
	public static String getLast(String s)
	{
		String args[]=s.split("\\.");	
		if(args.length>=1)
		return args[args.length-1];

		return "";
	}
	
	/*
	 * get second last
	 * E.g., a.b.c.d==> return c
	 */
	
	public static String getSecondLast(String s)
	{
		String args[]=s.split("\\.");
		if(args.length>=2)
		return args[args.length-2];
		
		return "";
	}
	
	
	/*
	 * get first from 2
	 * E.g., a.b.c.d==> return a.b.c
	 */
	public static String getFirstfrom2Parts(String s)
	{
		String args[]=s.split("\\.");	
		
		StringBuffer Package = new StringBuffer();
		Package.append(args[0]);
		
		for (int i=1;i<=args.length-2;i++)
		{
			Package.append("."+args[i]);
		}
		
		return Package.toString();
		
	}
	
	
	/*
	 * get first from 3 parts
	 * E.g., a.b.c.d==> return a.b
	 */
	public static String getFirstfrom3Parts(String s)
	{
		String args[]=s.split("\\.");	
		
		StringBuffer Package = new StringBuffer();
		Package.append(args[0]);
		
		for (int i=1;i<=args.length-3;i++)
		{
			Package.append("."+args[i]);
		}
		
		return Package.toString();
		
	}
	
	public static void main(String [] args)
	{
		String temp = "a.c.d.e.s()";
		System.out.println(getLast(temp));
		System.out.println(getSecondLast(temp));
		System.out.println(getFirstfrom2Parts(temp));
		System.out.println(getFirstfrom3Parts(temp));
	}
}
