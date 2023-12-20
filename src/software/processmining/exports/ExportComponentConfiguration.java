package software.processmining.exports;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import org.processmining.contexts.uitopia.annotations.UIExportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

import UtilityClasses.ClassClass;
import UtilityClasses.ComponentConfig;
/**
 * export the component to classes configuration files to disk. 
 * the export is a txt file, and each line is formatted as: component;class1;class2
 * @author cliu3
 *
 */
@Plugin(
	name = "Export Component Configuration (.conf)",
	returnLabels = {},
	returnTypes = {},
	parameterLabels = {"Exported Component Configuration","file" },
	userAccessible = true
)
@UIExportPlugin(
	description = "Export Component Configuration (.conf)",//show the type in window for recording files
	extension = "conf" // the suffix name
)
public class ExportComponentConfiguration {
	@PluginVariant(
			variantLabel = "Export Component Configuration (.conf)",
			requiredParameterLabels = {0, 1}// the input has two para, one the the object and one for the file.
		) 
	public void exportComponent2Classes(PluginContext context, ComponentConfig comConfig, File file) throws Exception {
		
		context.log("Component Configuration Export Starts..."); 
		System.out.println("Component Configuration Export Starts..");
		

		FileOutputStream fos = new FileOutputStream(file);
	 
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
		
		for(String component: comConfig.getAllComponents())
		{
			StringBuffer classes =new StringBuffer();
			for(ClassClass c: comConfig.getClasses(component))
			{
				classes.append(";"+c.toString());
			}
			
			bw.write(component+classes);
			bw.newLine();
		}
		bw.close();
		context.log("Component Configuration Export completes!");
	} 
}
