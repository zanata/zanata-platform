package org.fedorahosted.flies.core.model;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;

@Entity
public class Pofile implements java.io.Serializable {

	@Id
	@GeneratedValue
	private Long id;
	public Long getId() { return this.id; }
	public void setId(Long id) { this.id = id; }
	
	private String name;
	public String getName() { return this.name;	}
	public void setName(String name) { this.name = name; }
	
	private long size;
	public long getSize() { return this.size; }
	public void setSize(long size) { this.size = size; }
	
	private String contentType;
	public String getContentType() { return this.contentType; }
	public void setContentType(String contentType) { this.contentType = contentType; }
	
	@Lob
	@Column(length = 2147483647)
	@Basic(fetch = FetchType.LAZY)
	private byte[] data;
	public byte[] getData() { return this.data; }
	public void setData(byte[] data) { this.data = data; }
	
}
