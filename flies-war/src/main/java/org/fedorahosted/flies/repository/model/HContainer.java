package org.fedorahosted.flies.repository.model;


import javax.persistence.Entity;

import org.fedorahosted.flies.rest.dto.Container;
import org.fedorahosted.flies.rest.dto.DocumentResource;


@Entity
public class HContainer extends HParentResource{

	public HContainer() {
	}
	
	public HContainer(Container cont, HDocument hDoc, int nextDocRev) {
		super(cont, nextDocRev);
		setDocument(hDoc);
		if (cont.hasResources()) {
			for (DocumentResource res : cont.getResources()){
				HDocumentResource hRes = hDoc.create(res, nextDocRev);
				getResources().add(hRes);
				hRes.setParent(this);
			}
		}
	}

	private static final long serialVersionUID = 6475033994256762703L;

	@Override
	public Container toResource(int levels) {
		Container container = new Container(this.getResId());
		if (levels != 0) {
			for (HDocumentResource res : getResources()) {
				container.getResources().add(res.toResource(levels-1));
			}
		}
		return container;
	}
}
