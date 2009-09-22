package org.fedorahosted.flies.adapter.po.publican;
import java.io.File;
import java.io.IOException;
import java.util.List;


import org.fedorahosted.flies.LocaleId;
import org.fedorahosted.flies.adapter.po.PoReader;
import org.fedorahosted.flies.rest.dto.Document;
import org.fedorahosted.flies.rest.dto.ProjectIteration;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.InputSource;


public class PublicanExtractionTests {

	@Before
	public void setup() throws IOException {
		File file = File.createTempFile("poReaderTests", ".olp");
		System.out.println("creating file: " + file);
		if(file.exists())
			file.delete();
	}
	
	@Test
	public void testExtractingDeploymentGuide() throws IOException{
		File projectDir = new File("d:/projects/SELinux_User_Guide/");
		PublicanProjectAdapter adapter = new PublicanProjectAdapter(projectDir);
		
		ProjectIteration project = new ProjectIteration(adapter.getBookName(), adapter.getBookName());

		PoReader poReader = new PoReader();

		// loop through each file
		File potPath = new File(adapter.getBasePath(), adapter.getResourceBasePath());
		for (String resource: adapter.getResources() ){
			System.out.println(resource);
			File potFile = new File(potPath, resource);
			Document doc = new Document(resource,resource, "/", PoReader.PO_CONTENT_TYPE);
			project.getDocuments(true).add( doc );
			InputSource inputSource = new InputSource( potFile.toURI().toString() );
			inputSource.setEncoding("utf8");
			poReader.extractTemplate(doc, inputSource);
			
			// loop through each language
			for(String targetLanguage : new String[]{"de-DE"}/*adapter.getTargetLanguages(resource) */){
				System.out.print(targetLanguage + ",");
				File targetLangPath = new File(adapter.getBasePath(), adapter.getResourceBasePath(targetLanguage));
				File poFile = new File(targetLangPath, resource.substring(0, resource.length()-1));
				inputSource = new InputSource( poFile.toURI().toString() );
				inputSource.setEncoding("utf8");
				poReader.extractTarget(doc, inputSource, new LocaleId(targetLanguage));
			}
			System.out.println();
		}
		//projectPack.close();		

	}
	
}
