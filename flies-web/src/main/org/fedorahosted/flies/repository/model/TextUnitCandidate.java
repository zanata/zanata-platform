package org.fedorahosted.flies.repository.model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.fedorahosted.flies.core.model.Person;
import org.hibernate.validator.NotNull;

@Entity
public class TextUnitCandidate extends AbstractTextUnit {

	public static enum Type {
		Suggestion
	}

	public static enum Source {
		Web, PreviousVersion
	}

	private Type type = Type.Suggestion;
	private Source source = Source.Web;

	private TextUnitTarget target;
	private Person translator;

	public TextUnitCandidate() {
	}

	public TextUnitCandidate(TextUnitTarget target) {
		this.target = target;
	}

	@NotNull
	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Source getSource() {
		return source;
	}

	@NotNull
	public void setSource(Source source) {
		this.source = source;
	}

	@ManyToOne
	@JoinColumn(name = "target_id")
	public TextUnitTarget getTarget() {
		return target;
	}

	public void setTarget(TextUnitTarget target) {
		this.target = target;
	}

	@ManyToOne
	@JoinColumn(name = "translator_id")
	public Person getTranslator() {
		return translator;
	}

	public void setTranslator(Person translator) {
		this.translator = translator;
	}

}
