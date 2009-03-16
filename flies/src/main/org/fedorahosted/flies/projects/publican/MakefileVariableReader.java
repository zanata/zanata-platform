package org.fedorahosted.flies.projects.publican;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

public class MakefileVariableReader {

	boolean failOnMissingVariables = false;
	
	private static final Pattern MAKEFILE_VARIABLE_PATTERN = Pattern
    .compile("^[ \t]*([\\w]+)[ \t]*=[ \t]*(.*)$");
	
	public void setFailOnMissingVariables(boolean failOnMissingVariables) {
		this.failOnMissingVariables = failOnMissingVariables;
	}
	
	public boolean isFailOnMissingVariables() {
		return failOnMissingVariables;
	}

	public Map<String, String> read(File file, String ... vars){
		
		Map<String, String> vMap = new HashMap<String, String>();
		List<String> variables = new ArrayList<String>(Arrays.asList(vars));
		
		BufferedReader reader = null;
		try{
			reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(file),Charset.forName("UTF-8")));
			
			String line = reader.readLine();
			boolean inMultiline = false;
			String variable = null;
			StringBuilder value = null;
			while(line != null && (!variables.isEmpty() || inMultiline) ){
				if(inMultiline){
					line = line.trim();
					value.append(' ');
			    	if(line.charAt(value.length()-1) == '\\'){
			    		value.append(line.substring(0, value.length()-1));
			    		inMultiline = true;
			    	}
			    	else{
			    		value.append(line);
						inMultiline = false;
						vMap.put(variable, value.toString());
			    	}
				}
				else{
			        Matcher m = MAKEFILE_VARIABLE_PATTERN.matcher(line);
				    if (m.matches()) {
				    	variable = m.group(1);
				    	int index = variables.indexOf(variable); 
				    	if(index != -1){
				    		variables.remove(index);
					    	String val = m.group(2).trim();
					    	if(val.charAt(val.length()-1) == '\\'){
					    		value = new StringBuilder(val.substring(0, val.length()-1).trim());
					    		inMultiline = true;
					    	}
					    	else{
					    		value = new StringBuilder(val);
								vMap.put(variable, value.toString());
					    	}
				    	}
				    } 
				}
				line = reader.readLine();
	
			}
		}
		catch(IOException e){
			throw new MakefileReadException("Error reading Makefile", e);
		}
		finally{
			try{
				reader.close();
			}
			catch(Exception e){}
		}
		if(failOnMissingVariables && !variables.isEmpty()){
			throw new MakefileReadException("Missing variables: " + StringUtils.join(variables, ", "));
		}
		return vMap;
	}

	
}
