package org.fedorahosted.flies.rest.service;


import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.CharSetUtils;
import org.apache.commons.lang.StringUtils;
import org.fedorahosted.flies.common.ResourceType;
import org.fedorahosted.flies.model.HDocument;
import org.fedorahosted.flies.model.HSimpleComment;
import org.fedorahosted.flies.model.HTextFlow;
import org.fedorahosted.flies.model.po.HPoHeader;
import org.fedorahosted.flies.model.po.PoUtility;
import org.fedorahosted.flies.rest.dto.po.HeaderEntry;
import org.fedorahosted.flies.rest.dto.v1.AbstractTranslationResource;
import org.fedorahosted.flies.rest.dto.v1.SourceResource;
import org.fedorahosted.flies.rest.dto.v1.SourceTextFlow;
import org.fedorahosted.flies.rest.dto.v1.TranslationResource;
import org.fedorahosted.flies.rest.dto.v1.ext.PoHeader;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;

@Name("documentUtils")
@Scope(ScopeType.STATELESS)
@AutoCreate
@BypassInterceptors
public class DocumentUtils {
	
	Log log = Logging.getLog(DocumentUtils.class);

	public boolean mergeTextFlows(List<SourceTextFlow> from, HDocument to) {
		boolean changed = false;
		to.getTextFlows().clear();
		Set<String> ids = new HashSet<String>(to.getAllTextFlows().keySet());
		for(SourceTextFlow tf : from) {
			HTextFlow textFlow;
			if(ids.contains(tf.getId())) {
				ids.remove(tf.getId());
				textFlow = to.getAllTextFlows().get(tf.getId());
				if ( transfer(tf, textFlow) || textFlow.isObsolete() ) {
					textFlow.setRevision(to.getRevision());
					changed = true;
					log.debug("TextFlow with id {0} has changed", tf.getId());
				}
				textFlow.setObsolete(false);
			}
			else{
				textFlow = new HTextFlow();
				textFlow.setResId(tf.getId());
				textFlow.setRevision(to.getRevision());
				transfer(tf, textFlow);
				changed = true;
				log.debug("TextFlow with id {0} is new", tf.getId());
			}
			to.getTextFlows().add(textFlow);
		}
		
		// set remaining textflows to obsolete.
		for(String id : ids) {
			HTextFlow textFlow = to.getAllTextFlows().get(id);
			if( !textFlow.isObsolete()) {
				changed = true;
				log.debug("TextFlow with id {0} is now obsolete", id);
				textFlow.setRevision(to.getRevision());
				textFlow.setObsolete(true);
			}
		}

		return changed;
	}
	
	public boolean transfer(SourceResource from, HDocument to) {
		boolean changed = false;
		changed |= transfer( (AbstractTranslationResource) from, to);
		changed |= mergeTextFlows(from.getTextFlows(), to);
		return changed;
	}

	public boolean transfer(AbstractTranslationResource from, HDocument to) {
		boolean changed = false;

		// name
		if( ! equals(from.getName(), to.getName()) ) {
			to.setName(from.getName());
			changed = true;
		}
		
		// locale
		if( ! equals(from.getLang(), to.getLocale()) ) {
			to.setLocale(from.getLang());
			changed = true;
		}
		
		// contentType
		if( ! equals(from.getContentType(), to.getContentType()) ) {
			to.setContentType(from.getContentType());
			changed = true;
		}
		
		return changed;
	}

	public boolean transfer(PoHeader from, HPoHeader to) {
		boolean changed = false;

		HSimpleComment comment = to.getComment();
		if(comment == null) {
			comment = new HSimpleComment();
		}
		if( ! equals(from.getComment(), comment.getComment()) ) {
			changed = true;
			comment.setComment(from.getComment());
			to.setComment(comment);
		}
		
		String entries = PoUtility.listToHeader(from.getEntries());
		if( ! equals(entries, to.getEntries()) ) {
			to.setEntries(entries);
			changed = true;
		}
		
		return changed;
		
	}
	
	
	private static <T> boolean equals(T a, T b) {
		if(a == null && b == null ) {
			return true;
		}
		if(a == null || b == null) {
			return false;
		}
		
		return a.equals(b);
	}
	
	public boolean transfer(SourceTextFlow from, HTextFlow to) {
		boolean changed = false;
		if( ! equals(from.getContent(), to.getContent()) ) {
			to.setContent(from.getContent());
			changed = true;
		}

		// TODO from.getLang()
		
		return changed;
	}
	
	public void transfer(HDocument from, SourceResource to) {
		
		to.setName(from.getName());
		to.setLang(from.getLocale());
		to.setContentType(from.getContentType());
	}

	public void transfer(HPoHeader from, PoHeader to) {
		if(from.getComment() != null) {
			to.setComment(from.getComment().getComment());
		}
		to.getEntries().addAll(PoUtility.headerToList( from.getEntries() ) );
	}
	
	public void transfer(HTextFlow from, SourceTextFlow to) {
		to.setContent(from.getContent());
		// TODO
		//to.setLang(from.get)
	}

	public void transfer(HDocument from, TranslationResource to) {
		to.setContentType(from.getContentType());
		to.setLang(from.getLocale());
		to.setName(from.getDocId());
		to.setType(ResourceType.FILE); // TODO
	}
	
	public String encodeDocId(String id){
		String other = StringUtils.replace(id,"/", ",");
		try{
			other = URLEncoder.encode(other, "UTF-8");
			return StringUtils.replace(other, "%2C", ",");
		}
		catch(UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public String decodeDocId(String id) {
		try{
			String other = URLDecoder.decode(id, "UTF-8");
			return StringUtils.replace(other,",", "/");
		}
		catch(UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

}
