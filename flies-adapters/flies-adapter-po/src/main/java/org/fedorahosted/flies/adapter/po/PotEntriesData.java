package org.fedorahosted.flies.adapter.po;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="pot-entries-data", namespace=PoHeader.NAMESPACE)
public class PotEntriesData {

	private List<PotEntryData> entries;
	
	@XmlElement(name="entry", namespace=PoHeader.NAMESPACE)
	public List<PotEntryData> getEntries() {
		if(entries == null)
			entries = new ArrayList<PotEntryData>();
		return entries;
	}
}
