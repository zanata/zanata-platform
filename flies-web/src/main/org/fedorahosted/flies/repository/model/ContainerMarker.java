package org.fedorahosted.flies.repository.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("cnt")
public class ContainerMarker extends InlineMarker{

	private static final long serialVersionUID = 6869475898958474578L;

}
