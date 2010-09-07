/**
 * 
 */
package net.openl10n.flies.client.config;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.openl10n.flies.common.Namespaces;

/**
 * @author Sean Flanigan <sflaniga@redhat.com>
 *
 */
@XmlType(name = "docSetType", namespace = Namespaces.FLIES_CONFIG)
@XmlRootElement(name = "docset", namespace = Namespaces.FLIES_CONFIG)
@XmlAccessorOrder(XmlAccessOrder.ALPHABETICAL)
public class DocSet
{

}
