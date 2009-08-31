package net.openl10n.packaging.jpa.document;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("cnt")
public class HContainerMarker extends HInlineMarker{

	private static final long serialVersionUID = 6869475898958474578L;

}
