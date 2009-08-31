package net.openl10n.packaging.jpa.document;

import javax.persistence.Entity;

import net.openl10n.api.rest.document.Container;
import net.openl10n.api.rest.document.Resource;

@Entity
public class HContainer extends HParentResource{

	public HContainer() {
	}
	
	public HContainer(Container cont) {
		super(cont);
		for(Resource res : cont.getContent()){
			HResource hRes = HDocument.create(res);
			getChildren().add(hRes);
		}
	}

	private static final long serialVersionUID = 6475033994256762703L;

}
