package org.fedorahosted.flies.entity.resources;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(	uniqueConstraints = {@UniqueConstraint(columnNames={"resource_id", "document_id", "document_revision"})})
public class TextUnitHistory extends AbstractTextUnitTemplate{

}
