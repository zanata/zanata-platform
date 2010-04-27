package org.fedorahosted.flies.rest.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fedorahosted.flies.common.ContentState;
import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.core.dao.TextFlowDAO;
import org.fedorahosted.flies.core.dao.TextFlowTargetDAO;
import org.fedorahosted.flies.repository.model.HDocument;
import org.fedorahosted.flies.repository.model.HSimpleComment;
import org.fedorahosted.flies.repository.model.HTextFlow;
import org.fedorahosted.flies.repository.model.HTextFlowHistory;
import org.fedorahosted.flies.repository.model.HTextFlowTarget;
import org.fedorahosted.flies.repository.model.po.HPoHeader;
import org.fedorahosted.flies.repository.model.po.HPoTargetHeader;
import org.fedorahosted.flies.repository.model.po.HPotEntryData;
import org.fedorahosted.flies.repository.model.po.PoUtility;
import org.fedorahosted.flies.rest.MediaTypes;
import org.fedorahosted.flies.rest.dto.Document;
import org.fedorahosted.flies.rest.dto.Link;
import org.fedorahosted.flies.rest.dto.Relationships;
import org.fedorahosted.flies.rest.dto.SimpleComment;
import org.fedorahosted.flies.rest.dto.TextFlow;
import org.fedorahosted.flies.rest.dto.TextFlowTarget;
import org.fedorahosted.flies.rest.dto.TextFlowTargets;
import org.fedorahosted.flies.rest.dto.po.PoHeader;
import org.fedorahosted.flies.rest.dto.po.PoTargetHeader;
import org.fedorahosted.flies.rest.dto.po.PoTargetHeaders;
import org.fedorahosted.flies.rest.dto.po.PotEntryData;
import org.hibernate.Session;
import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

@AutoCreate
@Scope(ScopeType.STATELESS)
@Name("documentConverter")
public class DocumentConverter {

	@Logger
	private Log log;

	@In
	private TextFlowDAO textFlowDAO;
	@In
	private TextFlowTargetDAO textFlowTargetDAO;
	@In
	private Session session;

	ClassValidator<HTextFlow> resValidator = new ClassValidator<HTextFlow>(
			HTextFlow.class);
	ClassValidator<HTextFlowTarget> tftValidator = new ClassValidator<HTextFlowTarget>(
			HTextFlowTarget.class);
	ClassValidator<HSimpleComment> commentValidator = new ClassValidator<HSimpleComment>(
			HSimpleComment.class);
	ClassValidator<HPotEntryData> potEntryValidator = new ClassValidator<HPotEntryData>(
			HPotEntryData.class);
	ClassValidator<HPoHeader> poValidator = new ClassValidator<HPoHeader>(
			HPoHeader.class);
	ClassValidator<HPoTargetHeader> poTargetValidator = new ClassValidator<HPoTargetHeader>(
			HPoTargetHeader.class);

	/**
	 * Recursively copies from the source Document to the destination HDocument.
	 * Increments toHDoc's revision number if any resources were changed
	 * 
	 * @param fromDoc
	 *            source Document
	 * @param toHDoc
	 *            destination HDocument
	 */
	public void copy(Document fromDoc, HDocument toHDoc) {
		boolean docChanged = false;
		int nextDocRev = 1;
		if (!session.contains(toHDoc)) {
			// new document
			docChanged = true;
			log.debug("CHANGED: Document {0} is new", toHDoc.getDocId());
		} else {
			nextDocRev = toHDoc.getRevision() + 1;
		}
		// changing these attributes probably shouldn't
		// invalidate existing translations, so we don't
		// bother incrementing the doc rev
		toHDoc.setDocId(fromDoc.getId());
		toHDoc.setName(fromDoc.getName());
		toHDoc.setPath(fromDoc.getPath());
		toHDoc.setContentType(fromDoc.getContentType());
		toHDoc.setLocale(fromDoc.getLang());
		// toHDoc.setProject(container); // this must be done by the caller

		// don't copy revision; we don't accept revision from the client
		List<TextFlow> fromDocResources = fromDoc.getTextFlows();
		
		List<HTextFlow> hResources;
		Map<String, HTextFlow> oldResourceMap = new HashMap<String, HTextFlow>();
		List<HTextFlow> oldResources = toHDoc.getTextFlows();
		for (HTextFlow oldResource : oldResources) {
			oldResourceMap.put(oldResource.getResId(), oldResource);
		}
		// We create an empty list for HDocument.resources, and build it up
		// in the order of fromDoc's resources. This ensures that we preserve
		// the order of the list.
		hResources = new ArrayList<HTextFlow>(fromDocResources.size());
		for (TextFlow fromRes : fromDocResources) {
			HTextFlow hRes = null;
			if (session.contains(toHDoc)) {
				// document already exists, see if the resource does too
				hRes = textFlowDAO.getById(toHDoc, fromRes.getId());
			}
			boolean resChanged = false;
			if (hRes == null) {
				resChanged = true; // this will cause res.revision to be set
									// below
				hRes = toHDoc.create(fromRes, nextDocRev);
			} else {
				hRes.setObsolete(false);
			}
			hResources.add(hRes);
			// session.save(hRes);

			resChanged |= copy(fromRes, hRes, nextDocRev);
			if (resChanged) {
				hRes.setRevision(nextDocRev);
				docChanged = true;
			}
			if (oldResourceMap.remove(fromRes.getId()) == null) {
				docChanged = true;
				log.debug("CHANGED: Resource {0}:{1} was added", toHDoc
						.getDocId(), hRes.getResId());
			}

			InvalidValue[] invalidValues = resValidator.getInvalidValues(hRes);
			if (invalidValues.length != 0) {
				String message = "TextFlow with content '" + hRes.getContent()
						+ "' is invalid: " + Arrays.asList(invalidValues);
				log.error(message);
			}
		}
		if (fromDoc.hasExtensions())
			for (Object ext : fromDoc.getExtensions()) {
				if (ext instanceof PoHeader) {
					PoHeader fromHeader = (PoHeader) ext;
					HPoHeader toHeader = toHDoc.getPoHeader();
					if (toHeader == null) {
						toHeader = new HPoHeader();
						toHDoc.setPoHeader(toHeader);
						// toHPoHeader.setDocument(toHDoc);
						docChanged = true;
					}
					HSimpleComment toComment = toHeader.getComment();
					if (toComment == null) {
						toComment = new HSimpleComment();
						toHeader.setComment(toComment);
						docChanged = true;
					}
					String fromComment = fromHeader.getComment().getValue();
					if (!equal(fromComment, toComment.getComment())) {
						toComment.setComment(fromComment);
						docChanged = true;
					}
					String fromEntries = PoUtility.listToHeader(fromHeader
							.getEntries());
					if (!equal(toHeader.getEntries(), fromEntries)) {
						toHeader.setEntries(fromEntries);
						docChanged = true;
					}
					InvalidValue[] invalidValues = poValidator.getInvalidValues(toHeader);
					if (invalidValues.length != 0) {
						String message = "PO header for document '" + toHeader.getDocument().getDocId()
								+ "' is invalid: " + Arrays.asList(invalidValues);
						log.error(message);
					}
				} else if (ext instanceof PoTargetHeaders) {
					PoTargetHeaders fromHeaders = (PoTargetHeaders) ext;
					Map<LocaleId, HPoTargetHeader> toHeaders = toHDoc
							.getPoTargetHeaders();
					for (PoTargetHeader fromHeader : fromHeaders.getHeaders()) {
						// List<HeaderEntry> fromEntries =
						// fromHeader.getEntries();
						LocaleId localeId = fromHeader.getTargetLanguage();
						HPoTargetHeader toHeader = toHeaders.get(localeId);
						if (toHeader == null) {
							toHeader = new HPoTargetHeader();
							toHeader.setDocument(toHDoc);
							toHeader.setTargetLanguage(localeId);
							toHeaders.put(localeId, toHeader);
							docChanged = true;
						}
						HSimpleComment toComment = toHeader.getComment();
						if (toComment == null) {
							toComment = new HSimpleComment();
							toHeader.setComment(toComment);
							docChanged = true;
						}
						String fromComment = fromHeader.getComment().getValue();
						if (!equal(fromComment, toComment.getComment())) {
							toComment.setComment(fromComment);
							docChanged = true;
						}
						String fromEntries = PoUtility.listToHeader(fromHeader
								.getEntries());
						if (!equal(toHeader.getEntries(), fromEntries)) {
							toHeader.setEntries(fromEntries);
							docChanged = true;
						}
						InvalidValue[] invalidValues = poTargetValidator.getInvalidValues(toHeader);
						if (invalidValues.length != 0) {
							String message = "PO target header for document '" + toHeader.getDocument().getDocId()
									+ "' is invalid: " + Arrays.asList(invalidValues);
							log.error(message);
						}
					}
				} else {
					throw new RuntimeException("Unknown Document extension "
							+ ext.getClass() + " - please ensure your client is up to date");
				}
			}

		// even if we just move around resources without changing them,
		// the document is considered changed
		if (!oldResources.equals(hResources)) {
			// mark any removed resources as obsolete
			for (HTextFlow oldResource : oldResourceMap.values()) {
				oldResource.setObsolete(true);
				log.debug("CHANGED: Resource {0}:{1} was removed", toHDoc
						.getDocId(), oldResource.getResId());
			}
			toHDoc.setTextFlows(hResources);
			docChanged = true;
		}
		if (docChanged)
			toHDoc.setRevision(nextDocRev);
	}

	private static boolean equal(String a, String b) {
		return a == null ? b == null : a.equals(b);
	}

	/**
	 * Returns true if the content (or a comment) of htf was changed
	 */
	private boolean copy(TextFlow fromTf, HTextFlow htf, int nextDocRev) {
		boolean changed = false;
		if (!fromTf.getContent().equals(htf.getContent())) {
			changed = true;
			log.debug("CHANGED: TextFlow {0}:{1} content changed", htf
					.getDocument().getDocId(), htf.getResId());

			// save old version to history
			HTextFlowHistory history = new HTextFlowHistory(htf);
			htf.getHistory().put(htf.getRevision(), history);

			// make sure to set the status of any targets to NeedReview
			for (HTextFlowTarget target : htf.getTargets().values()) {
				// TODO not sure if this is the correct state
				target.setState(ContentState.NeedReview);
			}

			htf.setRevision(nextDocRev);
			htf.setContent(fromTf.getContent());
		}

		htf.setContent(fromTf.getContent());
		for (Object ext : fromTf.getExtensions()) {
			if (ext instanceof PotEntryData) {
				PotEntryData potEntryData = (PotEntryData) ext;
				HPotEntryData hPotEntryData = htf.getPotEntryData();
				if (hPotEntryData == null) {
					hPotEntryData = new HPotEntryData();
					// hPotEntryData.setTextFlow(htf);
					htf.setPotEntryData(hPotEntryData);
				}
				changed |= copy(potEntryData, hPotEntryData);
				InvalidValue[] invalidValues = potEntryValidator.getInvalidValues(hPotEntryData);
				if (invalidValues.length != 0) {
					String message = "POT entry for TextFlow with id '" + htf.getResId()
							+ "' is invalid: " + Arrays.asList(invalidValues);
					log.error(message);
				}
			} else if (ext instanceof TextFlowTargets) {
				// do nothing here, we want to do targets last:
				// if the comment changes, the resourceRev will have to be
				// incremented
			} else if (ext instanceof SimpleComment) {
				SimpleComment simpleComment = (SimpleComment) ext;
				HSimpleComment hComment = htf.getComment();
				if (hComment == null) {
					changed = true;
					log.debug("CHANGED: TextFlow {0}:{1} comment changed", htf
							.getDocument().getDocId(), htf.getResId());
					// NB HTextFlowHistory doesn't record comments
					hComment = new HSimpleComment();
					htf.setComment(hComment);
				} else {
					if (!hComment.getComment().equals(simpleComment.getValue()))
						changed = true;
				}
				hComment.setComment(simpleComment.getValue());
				InvalidValue[] invalidValues = commentValidator.getInvalidValues(hComment);
				if (invalidValues.length != 0) {
					String message = "Comment for TextFlow with id '" + htf.getResId()
							+ "' is invalid: " + Arrays.asList(invalidValues);
					log.error(message);
				}
			} else {
				throw new RuntimeException("Unknown TextFlow extension "
						+ ext.getClass());
			}
		}
		TextFlowTargets targets = fromTf.getTargets();
		if (targets != null) {
			for (TextFlowTarget target : targets.getTargets()) {
				HTextFlowTarget hTarget = null;
				if (session.contains(htf)) {
					hTarget = textFlowTargetDAO.getByNaturalId(htf, target
							.getLang());
				}
				if (hTarget == null) {
					hTarget = new HTextFlowTarget();
					hTarget.setLocale(target.getLang());
					hTarget.setTextFlow(htf);
					Integer tfRev;

					if (changed || htf.getRevision() == null)
						tfRev = nextDocRev;
					else
						tfRev = htf.getRevision();

					hTarget.setState(target.getState());
					hTarget.setContent(target.getContent());
					copy(target, hTarget, htf);
					hTarget.setTextFlowRevision(tfRev);
					hTarget.setRevision(1);
				} else {
					copy(target, hTarget, htf);
				}
				htf.getTargets().put(target.getLang(), hTarget);
				InvalidValue[] invalidValues = tftValidator.getInvalidValues(hTarget);
				if (invalidValues.length != 0) {
					String message = "TextFlowTarget with id '" + hTarget.getTextFlow().getResId()
							+ "' is invalid: " + Arrays.asList(invalidValues);
					log.error(message);
				}
			}
		}
		return changed;
	}

	private boolean copy(PotEntryData fromPotEntryData,
			HPotEntryData toHPotEntryData) {
		boolean changed = false;
		toHPotEntryData.setContext(fromPotEntryData.getContext());
		SimpleComment fromExtractedSimpleComment = fromPotEntryData
				.getExtractedComment();
		String fromExtractedComment = fromExtractedSimpleComment == null ? null
				: fromExtractedSimpleComment.getValue();

		HSimpleComment toExtractedComment = toHPotEntryData
				.getExtractedComment();
		if (fromExtractedComment != null && toExtractedComment == null) {
			toExtractedComment = new HSimpleComment();
			toHPotEntryData.setExtractedComment(toExtractedComment);
		}
		if (fromExtractedComment == null) {
			if (toExtractedComment != null) {
				changed = true;
				toHPotEntryData.setExtractedComment(null);
			}
		} else {
			changed |= !fromExtractedComment.equals(toExtractedComment
					.getComment());
			toExtractedComment.setComment(fromExtractedComment);
		}
		String flags = PoUtility.concatFlags(fromPotEntryData.getFlags());
		changed |= !flags.equals(toHPotEntryData.getFlags());
		toHPotEntryData.setFlags(flags);
		String references = PoUtility.concatRefs(fromPotEntryData
				.getReferences());
		changed |= !references.equals(toHPotEntryData.getReferences());
		toHPotEntryData.setReferences(references);
		return changed;
	}

	/**
	 * Creates the Hibernate equivalent of the TextFlow, setting parent to
	 * 'parent', setting document to hDocument, inheriting hDocument's revision.
	 */
	private HTextFlow create(TextFlow fromTextFlow, HDocument hDocument, int nextDocRev) {
		HTextFlow hTextFlow = new HTextFlow();
		hTextFlow.setDocument(hDocument);
		hTextFlow.setResId(fromTextFlow.getId());
		hTextFlow.setRevision(nextDocRev);
		hTextFlow.setContent(fromTextFlow.getContent());
		// copy TextFlowTargets to HTextFlowTargets:
		copy(fromTextFlow, hTextFlow, nextDocRev);
		return hTextFlow;
	}

	private void copy(TextFlowTarget target, HTextFlowTarget hTarget,
			HTextFlow htf) {
		boolean changed = false;
		changed |= !target.getContent().equals(hTarget.getContent());
		hTarget.setContent(target.getContent());
		changed |= !target.getLang().equals(hTarget.getLocale());
		hTarget.setLocale(target.getLang());
		hTarget.setTextFlowRevision(htf.getRevision());
		changed |= !target.getState().equals(hTarget.getState());
		hTarget.setState(target.getState());
		hTarget.setTextFlow(htf);
		if (target.hasComment()) {
			HSimpleComment hComment = hTarget.getComment();
			if (hComment == null) {
				hComment = new HSimpleComment();
				hTarget.setComment(hComment);
			}
			changed |= !target.getComment().equals(hComment.getComment());
			hComment.setComment(target.getComment().getValue());
		} else {
			changed |= (hTarget.getComment() != null);
		}
		if (changed)
			hTarget.setRevision(hTarget.getRevision() + 1);
	}



	public void addLinks(Document doc, URI docUri, URI iterationUri) {
		// add self relation
		Link link = new Link(docUri, Relationships.SELF);
		doc.getLinks().add(link);

		// add container relation
		link = new Link(iterationUri, Relationships.DOCUMENT_CONTAINER,
				MediaTypes.APPLICATION_FLIES_PROJECT_ITERATION_XML);
		doc.getLinks().add(link);
	}

	private static Map<String, HTextFlow> toMap(
			List<HTextFlow> textFlows) {
		Map<String, HTextFlow> map = new HashMap<String, HTextFlow>(
				textFlows.size());
		for (HTextFlow res : textFlows) {
			map.put(res.getResId(), res);
		}
		return map;
	}
}
