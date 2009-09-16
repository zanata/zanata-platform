package org.fedorahosted.flies.repository.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.fedorahosted.flies.ContentType;
import org.fedorahosted.flies.LocaleId;
import org.fedorahosted.flies.core.model.AbstractFliesEntity;
import org.fedorahosted.flies.hibernate.type.ContentTypeType;
import org.fedorahosted.flies.hibernate.type.LocaleIdType;
import org.fedorahosted.flies.rest.dto.Container;
import org.fedorahosted.flies.rest.dto.DataHook;
import org.fedorahosted.flies.rest.dto.Document;
import org.fedorahosted.flies.rest.dto.DocumentRef;
import org.fedorahosted.flies.rest.dto.Reference;
import org.fedorahosted.flies.rest.dto.Resource;
import org.fedorahosted.flies.rest.dto.TextFlow;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import org.hibernate.annotations.Where;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;

@Entity
@TypeDefs({
	@TypeDef(name="localeId", typeClass=LocaleIdType.class),
	@TypeDef(name = "contentType", typeClass = ContentTypeType.class)
})
public class HDocument extends AbstractFliesEntity{

	private String docId;
	private String name;
	private String path;
	private ContentType contentType;
	private Integer revision = 1;
	private LocaleId locale;
	
	private HProjectContainer project;
	private Integer pos;

	private Map<String, HResource> resources;
	private List<HResource> resourceTree;
	
	private Map<LocaleId, HDocumentTarget> targets;

	public HDocument(String fullPath, ContentType contentType) {
		this(fullPath, contentType, LocaleId.EN_US);
	}
	
	public HDocument(String fullPath, ContentType contentType, LocaleId locale) {
		int lastSepChar =  fullPath.lastIndexOf('/');
		switch(lastSepChar){
		case -1:
			this.path = "";
			this.docId = this.name = fullPath;
			break;
		case 0:
			this.path = "/";
			this.docId = fullPath;
			this.name = fullPath.substring(1);
			break;
		default:
			this.path = fullPath.substring(0,lastSepChar+1);
			this.docId = fullPath;
			this.name = fullPath.substring(lastSepChar+1);
		}
		this.contentType = contentType;
		this.locale = locale;
	}
	
	public HDocument(String docId, String name, String path, ContentType contentType) {
		this(docId, name, path, contentType, LocaleId.EN_US, 1);
	}
	
	public HDocument(String docId, String name, String path, ContentType contentType, LocaleId locale) {
		this(docId, name, path, contentType, locale, 1);
	}
	
	public HDocument(String docId, String name, String path, ContentType contentType, LocaleId locale, int revision) {
		this.docId = docId;
		this.name = name;
		this.path = path;
		this.contentType = contentType;
		this.locale = locale;
	}
	
	public HDocument() {
	}

	public HDocument(DocumentRef docRef) {
		this.docId = docRef.getRef().getId();
		this.name = docRef.getName();
		this.path = docRef.getPath();
		this.contentType = docRef.getContentType();
		this.locale = docRef.getLang();
		this.revision = docRef.getVersion();
	}

	
	public HDocument(Document docInfo) {
		this.docId = docInfo.getId();
		this.name = docInfo.getName();
		this.path = docInfo.getPath();
		this.contentType = docInfo.getContentType();
		this.locale = docInfo.getLang();
		this.revision = docInfo.getVersion();
	}

	public static HResource create(Resource res){
		if(res instanceof TextFlow){
			TextFlow tf = (TextFlow) res;
			return new HTextFlow( tf );
		}
		else if (res instanceof Container){
			Container cont = (Container) res;
			return new HContainer(cont);
		}
		else if (res instanceof DataHook){
			DataHook hook = (DataHook) res;
			return new HDataHook(hook);
		}
		else if (res instanceof Reference){
			Reference ref = (Reference) res;
			return new HReference(ref);
		}
		throw new IllegalStateException("could not find subclass of Resource: " + res.getClass().toString());
	}
	
	public void copy(List<Resource> content){
		for(Resource res :content){
			HResource hRes = create(res);
			hRes.setDocument(this);
			getResourceTree().add(hRes);
		}
	}
	
	@NaturalId
	@Length(max=128)
	@NotEmpty
	public String getDocId() {
		return docId;
	}
	
	public void setDocId(String docId) {
		this.docId = docId;
	}
	
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@NotNull
	public String getPath() {
		return path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	
	@NotNull
	@Type(type="localeId")
	public LocaleId getLocale() {
		return locale;
	}
	
	public void setLocale(LocaleId locale) {
		this.locale = locale;
	}
	
	@ManyToOne
	@JoinColumn(name="project_id",insertable=false, updatable=false, nullable=false)
	@NaturalId
	public HProjectContainer getProject() {
		return project;
	}
	
	public void setProject(HProjectContainer project) {
		this.project = project;
	}

	@Column(insertable=false, updatable=false, nullable=false)
	public Integer getPos() {
		return pos;
	}
	
	public void setPos(Integer pos) {
		this.pos = pos;
	}
	@NotNull
	public Integer getRevision() {
		return revision;
	}

	public void setRevision(Integer revision) {
		this.revision = revision;
	}

	@Transient
	public void incrementRevision() {
		revision++;
	}

	@Type(type="contentType")
	@NotNull
	public ContentType getContentType() {
		return contentType;
	}

	public void setContentType(ContentType contentType) {
		this.contentType = contentType;
	}

	@OneToMany(mappedBy="document")
	@MapKey(name="resId")
	public Map<String,HResource> getResources() {
		if(resources == null)
			resources = new HashMap<String, HResource>();
		return resources;
	}
	
	public void setResources(Map<String, HResource> resources) {
		this.resources = resources;
	}
	
	@OneToMany(mappedBy = "template")
	@OnDelete(action=OnDeleteAction.CASCADE)
	@MapKey(name="locale")
	public Map<LocaleId, HDocumentTarget> getTargets() {
		if(targets == null)
			targets = new HashMap<LocaleId, HDocumentTarget>();
		return targets;
	}

	public void setTargets(Map<LocaleId, HDocumentTarget> targets) {
		this.targets = targets;
	}

	@OneToMany(cascade=CascadeType.ALL)
	@Where(clause="parent_id=NULL")
	@IndexColumn(name="pos",base=0,nullable=false)
	@JoinColumn(name="document_id",nullable=false)
	public List<HResource> getResourceTree() {
		if(resourceTree == null)
			resourceTree = new ArrayList<HResource>();
		return resourceTree;
	}
	
	public void setResourceTree(List<HResource> resourceTree) {
		this.resourceTree = resourceTree;
	}
	
	public Document toDocument() {
	    return new Document(docId, name, path, contentType, revision, locale);
	}
}
