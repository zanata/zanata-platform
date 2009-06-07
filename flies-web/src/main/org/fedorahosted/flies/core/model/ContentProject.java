package org.fedorahosted.flies.core.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("content")
public class ContentProject extends Project{

}
