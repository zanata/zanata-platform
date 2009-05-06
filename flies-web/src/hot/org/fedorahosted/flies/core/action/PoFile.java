package org.fedorahosted.flies.core.action;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import org.fedorahosted.tennera.jgettext.*;
import org.fedorahosted.tennera.jgettext.catalog.parse.ParseException;

public class PoFile {
	private String name;
	private long length;
        private String content;	

	public PoFile() {
        }
        	
	private String ParseFile(File file) throws ParseException, IOException{
		PoParser poParser = new PoParser();
		PoWriter poWriter = new PoWriter();
		Catalog fileCatalog = poParser.parseCatalog(file);
		StringWriter outputWriter = new StringWriter();
		poWriter.write(fileCatalog, outputWriter);
		outputWriter.flush();
		return outputWriter.toString();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getLength() {
		return length;
	}

	public void setLength(long length) {
		this.length = length;
	}


        public String getContent() {
                return content;
        }

        public void setContent(File file) throws ParseException, IOException{
                content = ParseFile(file);
        }
}
