package net.openl10n.api;

import java.io.File;
import java.io.OutputStream;
import java.io.Writer;
/**
 * Resolved in the following order:
 * 
 * 1) Writer, 
 * 2) stream+charset, 
 * 3) stream+utf8, 
 * 4) file + charset
 * 5) file + utf8
 * @author asgeirf
 *
 */
public class OutputSource {

	private File file;
	private Writer writer;
	private OutputStream outputStream;
	private String encoding;
	
	public OutputSource() {
	}
	
	public OutputSource(Writer writer) {
		this.writer = writer;
	}
	
	public OutputSource(OutputStream outputStream, String encoding) {
		this.outputStream = outputStream;
		this.encoding = encoding;
	}
	
	public OutputSource(OutputStream outputStream) {
		this.outputStream = outputStream;
	}
	
	public OutputSource(File file, String encoding) {
		this.file = file;
		this.encoding = encoding;
	}
	
	public OutputSource(File file) {
		this.file = file;
	}
	
	public File getFile() {
		return file;
	}
	
	public void setFile(File file) {
		this.file = file;
	}
	
	public Writer getWriter() {
		return writer;
	}
	
	public void setWriter(Writer writer) {
		this.writer = writer;
	}
	
	public OutputStream getOutputStream() {
		return outputStream;
	}
	
	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}
	
	public String getEncoding() {
		return encoding;
	}
	
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

}
