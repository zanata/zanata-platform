package net.openl10n.consumer.po.publican;

import java.io.File;
import java.io.FilenameFilter;

public class ExtentionBasedFilenameFilter implements FilenameFilter {

	private boolean caseSensitive = false;
	private String[] extensions;

	private void lowercaseExtensions() {
		for (int i = 0; i < extensions.length; i++) {
			extensions[i] = extensions[i].toLowerCase();
		}
	}

	public ExtentionBasedFilenameFilter(String... extensions) {
		this.extensions = extensions;
		lowercaseExtensions();
	}

	public ExtentionBasedFilenameFilter(boolean caseSensitive,
			String... extensions) {
		this.extensions = extensions;
		this.caseSensitive = caseSensitive;
		if (!caseSensitive)
			lowercaseExtensions();
	}

	@Override
	public boolean accept(File dir, String name) {
		for (int i = 0; i < extensions.length; i++) {
			if (name.length() < extensions[i].length())
				continue;
			if (caseSensitive) {
				if (extensions[i].equals(name.substring(
						name.length() - extensions[i].length()).toLowerCase())) {
					return true;
				}
			} else {
				if (extensions[i].equals(name.substring(name.length()
						- extensions[i].length()))) {
					return true;
				}
			}
		}

		return false;

	}

}