package org.fedorahosted.flies.rest.service;


import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.common.ResourceType;
import org.fedorahosted.flies.model.HDocument;
import org.fedorahosted.flies.model.HPerson;
import org.fedorahosted.flies.model.HSimpleComment;
import org.fedorahosted.flies.model.HTextFlow;
import org.fedorahosted.flies.model.HTextFlowTarget;
import org.fedorahosted.flies.model.po.AbstractPoHeader;
import org.fedorahosted.flies.model.po.HPoHeader;
import org.fedorahosted.flies.model.po.HPoTargetHeader;
import org.fedorahosted.flies.model.po.HPotEntryData;
import org.fedorahosted.flies.model.po.PoUtility;
import org.fedorahosted.flies.rest.StringSet;
import org.fedorahosted.flies.rest.dto.v1.AbstractTextFlow;
import org.fedorahosted.flies.rest.dto.v1.AbstractResource;
import org.fedorahosted.flies.rest.dto.v1.ExtensionSet;
import org.fedorahosted.flies.rest.dto.v1.Person;
import org.fedorahosted.flies.rest.dto.v1.SourceResource;
import org.fedorahosted.flies.rest.dto.v1.SourceTextFlow;
import org.fedorahosted.flies.rest.dto.v1.TextFlowTarget;
import org.fedorahosted.flies.rest.dto.v1.ResourceMeta;
import org.fedorahosted.flies.rest.dto.v1.ext.PoHeader;
import org.fedorahosted.flies.rest.dto.v1.ext.PoTargetHeader;
import org.fedorahosted.flies.rest.dto.v1.ext.PoTargetHeaders;
import org.fedorahosted.flies.rest.dto.v1.ext.PotEntryHeader;
import org.fedorahosted.flies.rest.dto.v1.ext.SimpleComment;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;

@Name("resourceUtils")
@Scope(ScopeType.STATELESS)
@AutoCreate
@BypassInterceptors
public class ResourceUtils {
	
	Log log = Logging.getLog(ResourceUtils.class);

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
		changed |= transfer( (AbstractResource) from, to);
		changed |= mergeTextFlows(from.getTextFlows(), to);
		return changed;
	}

	public boolean transfer(AbstractResource from, HDocument to) {
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

	public boolean transfer(ExtensionSet from, HDocument to, StringSet extensions) {
		boolean changed = false;

		if( extensions.contains(PoHeader.ID) ) {
			PoHeader poHeaderExt = from.findByType(PoHeader.class);
			if(poHeaderExt != null) {
				HPoHeader poHeader = to.getPoHeader(); 
				if ( poHeader == null) {
					poHeader = new HPoHeader();
				}
				changed |= transfer(poHeaderExt, poHeader);

				if(to.getPoHeader() == null && changed) {
					poHeader.setDocument(to);
					to.setPoHeader( poHeader );
				}
				
			}
		}
		
		return changed;
	}

	public boolean transfer(ExtensionSet from, HDocument to, StringSet extensions, Collection<LocaleId> locales) {
		boolean changed = false;
		if( extensions.contains(PoTargetHeaders.ID) ) {
			PoTargetHeaders poTargetHeadersExt = from.findByType(PoTargetHeaders.class);
			if(poTargetHeadersExt != null) {
				Map<LocaleId, HPoTargetHeader> targetHeaders = to.getPoTargetHeaders();
				Set<LocaleId> unProcessedHeaders = new HashSet<LocaleId>(locales);
				for(PoTargetHeader header : poTargetHeadersExt.getHeaders()) {
					HPoTargetHeader poTargetHeader = targetHeaders.get(header.getLocale());
					if(poTargetHeader == null) {
						changed = true;
						poTargetHeader = new HPoTargetHeader();
						transfer(header, poTargetHeader);
						targetHeaders.put(header.getLocale(), poTargetHeader);
					}
					else{
						changed |= transfer(header, poTargetHeader);
					}
					
					unProcessedHeaders.remove(header.getLocale());
				}
				for(LocaleId locale : unProcessedHeaders) {
					changed |= targetHeaders.remove(locale) != null;
				}
			}
		}
		
		return changed;
	}
	
	private boolean transfer(PoTargetHeader from, HPoTargetHeader to) {
		boolean changed = false;

		if( !equals(from.getLocale(), to.getTargetLanguage())) {
			to.setTargetLanguage(from.getLocale());
			changed = true;
		}
		
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
	
	private boolean transfer(PoHeader from, HPoHeader to) {
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
	
	public boolean transfer(AbstractTextFlow from, HTextFlow to) {
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
	
	private void transfer(HPoTargetHeader from, PoTargetHeader to) {
		to.setLocale(from.getTargetLanguage());
		HSimpleComment comment = from.getComment();
		if(comment != null) {
			to.setComment(comment.getComment());
		}
		to.getEntries().addAll(PoUtility.headerToList( from.getEntries() ) );
		
	}
	
	public void transfer(HTextFlow from, AbstractTextFlow to) {
		to.setContent(from.getContent());
		// TODO HTextFlow should have a lang
		//to.setLang(from.get)
	}

	public void transfer(HDocument from, ResourceMeta to) {
		to.setContentType(from.getContentType());
		to.setLang(from.getLocale());
		to.setName(from.getDocId());
		// TODO ADD support within the hibernate model for multiple resource types
		to.setType(ResourceType.FILE); 
	}

	public void transfer(HDocument from, ExtensionSet to, StringSet extensions) {
		if(extensions.contains(PoHeader.ID)) {
			PoHeader poHeaderExt = new PoHeader();
			if(from.getPoHeader() != null) {
				transfer(from.getPoHeader(), poHeaderExt);
				to.add(poHeaderExt);
			}
		}
	}
	
	public void transfer(HDocument from, ExtensionSet to, StringSet extensions, Collection<LocaleId> locales) {
		if(extensions.contains(PoTargetHeaders.ID)) {
			PoTargetHeaders poTargetHeaders = new PoTargetHeaders();
			for(LocaleId locale : locales) {
				HPoTargetHeader fromHeader = from.getPoTargetHeaders().get(locale);
				if(fromHeader != null) {
					PoTargetHeader header = new PoTargetHeader();
					transfer(fromHeader, header);
					poTargetHeaders.getHeaders().add(header);
				}
			}
			to.add(poTargetHeaders);
		}
	}
	
	public void transfer(HTextFlow from, ExtensionSet to, StringSet extensions) {
		if(extensions.contains(PotEntryHeader.ID) && from.getPotEntryData() != null) {
			PotEntryHeader header = new PotEntryHeader();
			transfer(from.getPotEntryData(), header);
			to.add(header);
			
		}

		if(extensions.contains(SimpleComment.ID) && from.getComment() != null) {
			SimpleComment comment = new SimpleComment();
			comment.setValue(from.getComment().getComment());
			to.add(comment);
		}
		
	}
	
	private void transfer(HPotEntryData from, PotEntryHeader to) {
		to.setContext(from.getContext());
		HSimpleComment comment = from.getExtractedComment();
		if(comment != null) {
			to.setExtractedComment(comment.getComment());
		}
	}

	public void transfer(HTextFlowTarget from, ExtensionSet to, StringSet extensions) {
		if(extensions.contains(SimpleComment.ID) && from.getComment() != null) {
			SimpleComment comment = new SimpleComment();
			comment.setValue(from.getComment().getComment());
		}
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

	public void transfer(HTextFlowTarget from, TextFlowTarget to) {
		to.setContent(from.getContent());
		to.setState(from.getState());
		HPerson translator = from.getLastModifiedBy();
		if(translator != null) {
			to.setTranslator(new Person(translator.getEmail(), translator.getName()));
		}
	}

}
