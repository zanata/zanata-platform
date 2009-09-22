package org.fedorahosted.flies.repository.model;

import java.util.Set;

import javax.persistence.Entity;

import org.fedorahosted.flies.LocaleId;
import org.fedorahosted.flies.rest.dto.Container;
import org.fedorahosted.flies.rest.dto.Resource;


@Entity
public class HContainer extends HParentResource{

	public HContainer() {
	}
	
	public HContainer(Container cont) {
		super(cont);
		if(cont.hasContent()) {
			for(Resource res : cont.getContent()){
				HResource hRes = HDocument.create(res);
				getChildren().add(hRes);
			}
		}
	}

	private static final long serialVersionUID = 6475033994256762703L;

	@Override
	public Container toResource(int levels) {
		Container container = new Container(this.getResId());
		if (levels != 0) {
			for (HResource res : getChildren()) {
				container.getContent().add(res.toResource(levels-1));
			}
		}
		return container;
	}
}
