package software.processmining.exports;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.HashSet;

import org.processmining.contexts.uitopia.annotations.UIExportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;

import UtilityClasses.ClassClass;
import UtilityClasses.ClassTypeHierarchy;

/**
 * export the class type hierarchy files to disk. 
 * the export is a txt file, and each line is formatted as: TestStateStrategy.TS;TestStateStrategy.TS1;TestStateStrategy.TS2
 * @author cliu3
 *
 */
@Plugin(
	name = "Export Component Configuration (.cth)",
	returnLabels = {},
	returnTypes = {},
	parameterLabels = {"Exported Class Type Hierarhy","file" },
	userAccessible = true
)
@UIExportPlugin(
	description = "Export Class Type Hierarhy (.cth)",//show the type in window for recording files
	extension = "cth" // the suffix name
)
public class ExportClassTypeHierarchy {
	@PluginVariant(
			variantLabel = "Export Class Type Hierarhy (.cth)",
			requiredParameterLabels = {0, 1}// the input has two para, one the the object and one for the file.
		) 
	public void exportComponent2Classes(PluginContext context, ClassTypeHierarchy cth, File file) throws Exception {
		
		context.log("Class Type Hierarchy Starts..."); 
		System.out.println("Class Type Hierarchy Starts..");
		
		FileOutputStream fos = new FileOutputStream(file);
	 
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
		
		for(HashSet<ClassClass> classSet: cth.getAllCTH())
		{
			StringBuffer classes =new StringBuffer();
			for(ClassClass c: classSet)
			{
				classes.append(c.toString()+";");
			}
			
			bw.write(classes.toString());
			bw.newLine();
		}
		
		bw.close();
		context.log("Class Type Hierarhy Export completes!");
	} 
}
