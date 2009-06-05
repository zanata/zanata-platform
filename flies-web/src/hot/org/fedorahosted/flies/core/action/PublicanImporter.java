package org.fedorahosted.flies.core.action;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringUtils;
import org.fedorahosted.flies.core.model.FliesLocale;
import org.fedorahosted.flies.core.model.ProjectIteration;
import org.fedorahosted.flies.core.model.ResourceCategory;
//import org.fedorahosted.flies.projects.AdapterException;
//import org.fedorahosted.flies.projects.publican.PublicanProjectAdapter;
import org.fedorahosted.flies.repository.dao.DocumentDAO;
import org.fedorahosted.flies.repository.dao.TextUnitDAO;
import org.fedorahosted.flies.repository.model.Document;
import org.fedorahosted.flies.repository.model.DocumentTarget;
import org.fedorahosted.flies.repository.model.TextUnit;
import org.fedorahosted.flies.repository.model.TextUnitTarget;
import org.fedorahosted.flies.repository.model.AbstractTextUnitTarget.Status;
import org.fedorahosted.tennera.jgettext.Message;
import org.fedorahosted.tennera.jgettext.catalog.parse.MessageStreamParser;
import org.fedorahosted.tennera.jgettext.catalog.parse.ParseException;
import org.hibernate.Session;
import org.hibernate.validator.InvalidStateException;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.log.Log;
import org.jboss.seam.util.Hex;
import org.jboss.serial.util.HashStringUtil;

@Name("publicanImporter")
@Scope(ScopeType.CONVERSATION)
@AutoCreate
public class PublicanImporter {
/*
	@In 
	EntityManager entityManager;
	
	@In
	DocumentDAO documentDAO;
	
	@In
	TextUnitDAO textUnitDAO;
	
	@In(value="#{entityManager.delegate}")
	Session session;
	
	@In
	TargetManager targetManager;
	
	@Logger 
	Log log;
	
//	PublicanProjectAdapter adapter;

	ResourceCategory cat;
	
	private void initialize(){
		cat = entityManager.find(ResourceCategory.class, 1L);
		if (cat == null) {
			cat = new ResourceCategory();
			cat.setName("Documentation");
			session.save(cat);
		}			
	}
	
	@Asynchronous
	public void process(String rootDir, Long targetId){

		
		initialize();
		
		targetManager.setId(targetId);
		
		Set<String> remainingResources = new HashSet<String>(targetManager.getDocumentNames()); 
			
		log.info("processing {0} for {1} - {2}", rootDir, targetManager.getTarget().getProject().getName(), targetManager.getTarget().getName());

		File rootFile = new File(rootDir);
		if(!rootFile.exists()){
			log.error("Directory does not exist, aborting..");
			return;
		}
		
		adapter = new PublicanProjectAdapter(rootFile);
		
		try{
			log.info("Processing {0} resources in {1} languages", 
					adapter.getResources().size(), adapter.getTargetLanguages().size());
			
			
			for(String resource : adapter.getResources()){
				log.info("Importing {0}", resource);
				Document document = targetManager.getDocumentByName(resource);
				if(document == null){
					document = addNewDocument(resource);
				}
				else{
					remainingResources.remove(resource);
					updateExistingDocument(resource);
				}

				addDocumentTargets(document, resource);
				
				for(String lang : adapter.getTargetLanguages(resource)){
//					DocumentTarget dTarget = new DocumentTarget();
//					dTarget.setTemplate(document);
//					dTarget.setLocale(getLocaleForId(lang));
//					session.save(dTarget);
//					
//					poFile = new File(adapter.getResourceBasePath(lang),resource + ".po");
//					importTranslations(dTarget, poFile, unitCache);
				}
			}
			
			for(String resource : remainingResources){
				// delete document?
			}
			
			log.info("Imported {0} templates", adapter.getResources().size());
			
		}
		catch(AdapterException ex){
			log.error("Import failed {0}", ex.getMessage());
		}
	}
	
	private void addDocumentTargets(Document document, String resource) {

		HashMap<String, DocumentTarget> existingTargets = new HashMap<String, DocumentTarget>();
		for(DocumentTarget target : document.getTargets()){
			existingTargets.put(target.getLocale().getId(), target);
		}
		
		for(String lang : adapter.getTargetLanguages()){
			boolean resourceExists = adapter.getResources(lang).contains(resource.substring(0, resource.length()-1));
			if(resourceExists){
				if(existingTargets.containsKey(lang)){
					updateExistingDocumentTarget(existingTargets.get(lang), resource, lang);
					existingTargets.remove(lang);
				}
				else{
					addNewDocumentTarget(document, resource, lang);
				}
			}
		}
		
		for(String langDoc : existingTargets.keySet()){
			// remove
		}
		
		
	}

	private void updateExistingDocumentTarget(DocumentTarget docTarget,
			String resource, String lang) {
		
	}

	@Transactional
	private void addNewDocumentTarget(Document document, String resource,
			String lang) {

		FliesLocale locale = getLocaleForId(lang); 
		if(locale == null){
			log.info("skipping locale {0}", lang);
			return;
		}
			
		DocumentTarget docTarget = new DocumentTarget();
		docTarget.setTemplate(document);
		docTarget.setLocale(locale);
		session.save(docTarget);
		addNewTextUnitTargets(docTarget, resource);
		
	}

	@Transactional
	private void updateExistingDocument(String resource) {
		Document document = targetManager.getDocumentByName(resource);
		updateTextUnits(document, resource);
	}

	@Transactional
	private Document addNewDocument(String resource) {
		Document document = new Document();
		document.setRevision(1);
		document.setName(resource.substring(0, resource.length()-4)+".xml");
		document.setProject(targetManager.getTarget().getProject());
		document.setProjectIteration(targetManager.getTarget());
		document.setResourceCategory(cat);
		document.setContentType("pot");
		session.save(document);
		
		addNewTextUnits(document, resource);
		session.flush();
		return document;
		
	}

	private void updateTextUnitTargets(DocumentTarget docTarget, String resource){
		
	}	
	
	private void updateTextUnits(Document doc, String resource){

		//Set<String> remainingUnits = new HashSet<String>(documentManager.getResourceIds());
		
		File poFile = new File(
				new File(adapter.getBasePath(), adapter.getResourceBasePath()),
				resource);
		
		if(!poFile.exists()){
			log.error("Failed to find pot file {0}", poFile.getAbsoluteFile());
			return;
		}
		try{
			MessageStreamParser parser = new MessageStreamParser(poFile);
			while(parser.hasNext()){
				Message message = parser.next();
				if (!message.isHeader()) {
					String rId = getHash(message.getMsgid());
					log.info("processing {0}", message.getMsgid());
					TextUnit tu = textUnitDAO.getTextUnitById(doc.getId(), rId);
					if(tu == null){
						if(message.getMsgid() == null){
							log.debug("skipping entry with empty msgid \n{0}", message);
							return;
						}
						tu = createTextUnit(rId, doc, message);
					}
					else{
						tu.setObsolete(message.isObsolete());
						session.saveOrUpdate(tu);
						//remainingUnits.remove(rId);
					}
					// create Template...
				}
			}
//			for(String tuId : remainingUnits){
//				TextUnit tu = documentManager.getTextUnitWithResourceId(tuId);
//				tu.setObsolete(true);
//				session.saveOrUpdate(tu);
//			}
		}
		catch (ParseException e) {
			log.error("Error parsing document", e);
		}
		catch(IOException e){
			log.error("Error opening document", e);
		}
		

	}

	private static String getHash(String key){
	    try {
	        MessageDigest md5 = MessageDigest.getInstance("MD5");
	        md5.reset();
	        return new String(Hex.encodeHex(md5.digest(key.getBytes("UTF-8"))));
	    } catch (Exception exc) {
	        throw new RuntimeException(exc);
	    }
	}
	
	private void addNewTextUnitTargets(DocumentTarget docTarget, String resource){
		File poFile = new File(
				new File(adapter.getBasePath(), adapter.getResourceBasePath(docTarget.getLocale().getId())),
				resource.substring(0, resource.length()-1));
		if(!poFile.exists()){
			log.error("Failed to find po file {0}", poFile.getAbsoluteFile());
			return;
		}
		
		try{
			MessageStreamParser parser = new MessageStreamParser(poFile);
			
			while(parser.hasNext()){
				Message message = parser.next();
				if (!message.isHeader()) {
					if(message.getMsgid() == null){
						log.debug("skipping entry with empty msgid \n{0}", message);
						return;
					}
					String rId = getHash(message.getMsgid());
					TextUnit tu = textUnitDAO.getTextUnitById(docTarget.getTemplate().getId(), rId);
					if(tu == null){
						
						tu = createTextUnit(rId, docTarget.getTemplate(), message);
					}

					TextUnitTarget tuTarget = new TextUnitTarget();
					tuTarget.setTemplate(tu);
					tuTarget.setLocale(docTarget.getLocale());
					tuTarget.setContent(message.getMsgstr());
					tuTarget.setDocument(docTarget.getTemplate());
					tuTarget.setDocumentRevision(1);
					tuTarget.setStatus(message.isFuzzy() ? Status.ForReview : Status.Approved);
					session.save(tuTarget);
				}
			}
		}
		catch (ParseException e) {
			log.error("Error parsing document", e);
		}
		catch(IOException e){
			log.error("Error opening document", e);
		}
	}	
	
	private void addNewTextUnits(Document doc, String resource){
		File poFile = new File(
				new File(adapter.getBasePath(), adapter.getResourceBasePath()),
				resource);
		if(!poFile.exists()){
			log.error("Failed to find pot file {0}", poFile.getAbsoluteFile());
			return;
		}
		try{
			MessageStreamParser parser = new MessageStreamParser(poFile);
			while(parser.hasNext()){
				Message message = parser.next();
				if (!message.isHeader()) {
					// create Template...
					if(message.getMsgid() == null){
						log.debug("skipping entry with empty msgid \n{0}", message);
						return;
					}
					String rId = getHash(message.getMsgid());
					TextUnit tu = createTextUnit(rId, doc, message);
				}
			}
		}
		catch (ParseException e) {
			log.error("Error parsing document", e);
		}
		catch(IOException e){
			log.error("Error opening document", e);
		}
		
	}
	
	private TextUnit createTextUnit(String id, Document doc, Message message){
		TextUnit tu = new TextUnit();
		tu.setResourceId(id);
		tu.setDocument(doc);
		tu.setContent(message.getMsgid());
		tu.setDocumentRevision(doc.getRevision());
		tu.setObsolete(message.isObsolete());
		session.save(tu);
		return tu;
	}
	
	private FliesLocale getLocaleForId(String id){
		return entityManager.find(FliesLocale.class, id);
	}
	
	private void importTemplateMessages(Document template, File poFile, Map<String, TextUnit> cache){
		
		try{
			MessageStreamParser parser = new MessageStreamParser(poFile);
			while(parser.hasNext()){
				Message message = parser.next();
				if (!message.isHeader()) {
					// create Template...
					TextUnit tu = new TextUnit();
					if(message.getMsgid() == null){
						log.debug("skipping entry with empty msgid \n{0}", message);
						return;
					}
					String rId = String.valueOf(HashStringUtil.hashName(message.getMsgid()));
					cache.put(rId, tu);
					tu.setResourceId(rId);
					tu.setDocument(template);
					tu.setContent(message.getMsgid());
					tu.setDocumentRevision(template.getRevision());
					tu.setObsolete(message.isObsolete());
					session.save(tu);
				}
			}
		}
		catch (ParseException e) {
			log.error("Error parsing document", e);
		}
		catch(IOException e){
			log.error("Error opening document", e);
		}
	}
*/	
}
