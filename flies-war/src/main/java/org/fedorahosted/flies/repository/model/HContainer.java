package org.fedorahosted.flies.repository.model;

import javax.persistence.Entity;

import org.fedorahosted.flies.rest.dto.Container;
import org.fedorahosted.flies.rest.dto.Resource;


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
