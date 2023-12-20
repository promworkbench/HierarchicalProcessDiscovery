package UtilityFunctions;

import java.io.FileOutputStream;
import java.io.IOException;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.out.XesXmlSerializer;

public class XESLogSerialization {

	/**
	 * 
	 * @param log
	 * @param fileLocation: "D:\\[7]\\interaction\\"+log.getAttributes().get(XConceptExtension.KEY_NAME)+".xes"
	 */
	public static void LogSerialization(XLog log, String fileLocation)
	{
		//serialization the current XESlog to disk
		try {
			FileOutputStream fosgz = new FileOutputStream(fileLocation); 					
			new XesXmlSerializer().serialize(log, fosgz); 

			fosgz.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
}
