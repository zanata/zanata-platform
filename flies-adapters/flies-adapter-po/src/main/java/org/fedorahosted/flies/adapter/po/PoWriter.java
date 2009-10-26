package org.fedorahosted.flies.adapter.po;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;


import org.apache.commons.lang.StringUtils;
import org.fedorahosted.flies.LocaleId;
import org.fedorahosted.flies.LocaleOutputSourcePair;
import org.fedorahosted.flies.OutputSource;
import org.fedorahosted.flies.rest.dto.Document;
import org.fedorahosted.flies.rest.dto.DocumentResource;
import org.fedorahosted.flies.rest.dto.SimpleComment;
import org.fedorahosted.flies.rest.dto.TextFlow;
import org.fedorahosted.flies.rest.dto.TextFlowTarget;
import org.fedorahosted.flies.rest.dto.TextFlowTargets;
import org.fedorahosted.flies.rest.dto.po.HeaderEntry;
import org.fedorahosted.flies.rest.dto.po.PoHeader;
import org.fedorahosted.flies.rest.dto.po.PoTargetHeader;
import org.fedorahosted.flies.rest.dto.po.PoTargetHeaders;
import org.fedorahosted.flies.rest.dto.po.PotEntryData;
import org.fedorahosted.tennera.jgettext.HeaderFields;
import org.fedorahosted.tennera.jgettext.Message;

public class PoWriter {

	private final org.fedorahosted.tennera.jgettext.PoWriter poWriter = new org.fedorahosted.tennera.jgettext.PoWriter();
	
	public PoWriter() {
	}
	
	public void write(final Document document, final LocaleOutputSourcePair localeOutputSourcePair) throws IOException{

		final Writer writer;
		final LocaleId locale;

		if(document == null)
			throw new IllegalArgumentException("document");
		
		if(localeOutputSourcePair == null)
			throw new IllegalArgumentException("localeOutputSourcePair");

		OutputSource outputSource = localeOutputSourcePair.getOutputSource();
		
		locale = localeOutputSourcePair.getLocaleId();
		
		// the writer has first priority
		if(outputSource.getWriter() != null)
			writer = outputSource.getWriter();
		else if (outputSource.getOutputStream() != null) { // outputstream has 2nd priority
			if(outputSource.getEncoding() != null){
				writer = new OutputStreamWriter(outputSource.getOutputStream(), Charset.forName(outputSource.getEncoding()));
			}
			else{
				writer = new OutputStreamWriter(outputSource.getOutputStream() );
			}
		}
		else if(outputSource.getFile() != null){ // file has 3rd priority
			try{
				OutputStream os = new BufferedOutputStream(new FileOutputStream(outputSource.getFile()));
				if(outputSource.getEncoding() != null){
					writer = new OutputStreamWriter(os, Charset.forName(outputSource.getEncoding()));
				}
				else{
					writer = new OutputStreamWriter(os);
				}
			}
			catch(FileNotFoundException fnf){
				throw new IllegalArgumentException("localeOutputSourcePair", fnf);
			}
		}
		else
			throw new IllegalArgumentException("localeOutputSourcePair");
		
		
//		if(!document.getTargetLanguages().contains(locale))
//			throw new RuntimeException("could not find target locale");

		PoHeader potHeader = document.getExtension(PoHeader.class);
		PoTargetHeaders poHeaders = document.getExtension(PoTargetHeaders.class);
		
		PoTargetHeader poHeader = poHeaders.getByLocale(locale);
		
		
		HeaderFields hf = new HeaderFields();
		for(HeaderEntry e : potHeader.getEntries()){
			hf.setValue(e.getKey(), e.getValue());
		}
		for(HeaderEntry e : poHeader.getEntries()){
			hf.setValue(e.getKey(), e.getValue());
		}
		Message message = hf.unwrap();
		
		for(String s : poHeader.getComment().getValue().split("\n")){
			message.addComment(s);
		}
		poWriter.write(message, writer);
		writer.write("\n");

		// first write header
		if(!document.hasResources())
			return;
		for(DocumentResource resource : document.getResources()){
			TextFlow textFlow = (TextFlow) resource;

			PotEntryData entryData = textFlow.getExtension(PotEntryData.class);
			TextFlowTargets entryTargets = textFlow.getExtension(TextFlowTargets.class);
			TextFlowTarget contentData = entryTargets.getByLocale(locale);
			SimpleComment poComment = contentData.getExtension(SimpleComment.class);
			
			if(!entryData.getId().equals(textFlow.getId()) || ! contentData.getId().equals(textFlow.getId())){
				throw new RuntimeException("hey, expected something else here!");
			}

			message = new Message();
			message.setMsgid(textFlow.getContent());
			message.setMsgstr(contentData.getContent());
			
			String [] comments = poComment.getValue().split("\n");
			if(comments.length == 1 && comments[0].isEmpty()){
				
			}
			else{
				for(String comment : comments){
					message.getComments().add(comment);
				}
			}
			
			switch(contentData.getState()){
			case Final:
				message.setFuzzy(false);
				break;
			case ForReview:
			case Leveraged:
			case New:
				message.setFuzzy(true);
				break;
			}
			copyToMessage(entryData, message);
			
			poWriter.write(message, writer);
			writer.write("\n");
		}
	}
	
	private static void copyToMessage(PotEntryData data, Message message){
		String context = data.getContext();
		if(context != null)
			message.setMsgctxt(context);
		String [] comments = StringUtils.splitPreserveAllTokens(data.getExtractedComment().getValue(), "\n");
		if(!(comments.length == 1 && comments[0].isEmpty())){
			for(String comment : comments){
				message.addExtractedComment(comment);
			}
		}
		for(String flag : data.getFlags()){
			message.addFormat(flag);
		}
		for(String ref : data.getReferences()){
			message.addSourceReference(ref);
		}
	}
}
