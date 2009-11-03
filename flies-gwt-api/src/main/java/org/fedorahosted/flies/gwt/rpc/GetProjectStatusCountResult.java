package org.fedorahosted.flies.gwt.rpc;

import org.fedorahosted.flies.gwt.model.ProjectContainerId;

public class GetProjectStatusCountResult implements SequenceResult {
	private static final long serialVersionUID = 1L;
	
	private ProjectContainerId projectContainerId;
	private long untranslated;
	private long fuzzy;
	private long translated;
	private int offset;
	
	@SuppressWarnings("unused")
	private GetProjectStatusCountResult() {
	}
	
	public GetProjectStatusCountResult(ProjectContainerId projectContainerId, long untranslated, long fuzzy, long translated, int offset) {
		this.projectContainerId = projectContainerId;
		this.untranslated = untranslated;
		this.fuzzy = fuzzy;
		this.translated = translated;
		this.offset = offset;
	}
	
	public ProjectContainerId getProjectContainerId() {
		return projectContainerId;
	}
	
	public long getUntranslated() {
		return untranslated;
	}
	
	public long getFuzzy() {
		return fuzzy;
	}
	
	public long getTranslated() {
		return translated;
	}
	
	@Override
	public int getSequence() {
		return offset;
	}
	
}
