package org.fedorahosted.flies.adapter.properties;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fedorahosted.flies.LocaleId;
import org.fedorahosted.flies.rest.dto.Document;
import org.fedorahosted.flies.rest.dto.Resource;
import org.fedorahosted.flies.rest.dto.TextFlow;
import org.fedorahosted.flies.rest.dto.TextFlowTarget;
import org.fedorahosted.flies.rest.dto.TextFlowTarget.ContentState;
import org.fedorahosted.openprops.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

/**
 * A PropReader. NOT THREADSAFE.
 * 
 * @author <a href="mailto:sflaniga@redhat.com">Sean Flanigan</a>
 * @version $Revision: 1.1 $
 */
public class PropReader {

	public static final String PROP_CONTENT_TYPE = "text/plain";
	
	private static final Logger log = LoggerFactory.getLogger(PropReader.class);

	// private final MessageDigest md5;
	//
	// public PropReader() {
	// try {
	// this.md5 = MessageDigest.getInstance("MD5");
	// } catch (NoSuchAlgorithmException e) {
	// throw new RuntimeException(e);
	// }
	// }

	public void extractAll(Document doc, File basePropertiesFile,
			String[] locales) throws IOException {
		InputStream baseInput = new BufferedInputStream(
				new FileInputStream(basePropertiesFile));
		try {
			// System.out.println("processing base " + basePropertiesFile);
			extractTemplate(doc, new InputSource(baseInput));
			for (String locale : locales) {
				File localeFile = new File(dropExtension(basePropertiesFile
						.toString())
						+ "_" + locale + ".properties");
				if (!localeFile.exists())
					continue;
				// System.out.println("processing " + localeFile);
				InputStream localeInput = new BufferedInputStream(new FileInputStream(localeFile));
				try {
					extractTarget(doc, new InputSource(localeInput), new LocaleId(
						locale));
				} finally {
					localeInput.close();
				}
			}
		} finally {
			baseInput.close();
		}
	}

	private String dropExtension(String f) {
		return f.substring(0, f.length() - ".properties".length());
	}

	// pre: template already extracted
	public void extractTarget(Document doc, InputSource inputSource,
			LocaleId localeId) throws IOException {
		Map<String, TextFlow> textFlowMap = new HashMap<String, TextFlow>();
		for (Resource resource : doc.getResources(true)) {
			if (resource instanceof TextFlow) {
				TextFlow textFlow = (TextFlow) resource;
				textFlowMap.put(textFlow.getId(), textFlow);
			}
		}

		Properties props = loadProps(inputSource);
		for (String key : props.stringPropertyNames()) {
			String val = props.getProperty(key);
			String id = getID(key, val);

			TextFlow textFlow = textFlowMap.get(id);
			if (textFlow == null) {
				log.warn("Property with key {} in locale {} has no corresponding source in {}", 
						new Object[]{key, localeId, doc.getId()});
				continue;
			}
			TextFlowTarget textFlowTarget = new TextFlowTarget(); // TODO might
																	// need id,
																	// version
			textFlowTarget.setContent(val);
			textFlowTarget.setId(id);
			textFlowTarget.setLang(localeId);
			textFlowTarget.setState(ContentState.New);
			// textFlowTarget.setVersion(version)
			textFlow.addTarget(textFlowTarget);
		}
	}

	// TODO allowing Readers (via InputSource) might be a bad idea
	public void extractTemplate(Document doc, InputSource inputSource)
			throws IOException {
		List<Resource> resources = doc.getResources(true);
		Properties props = loadProps(inputSource);
		for (String key : props.stringPropertyNames()) {
			String val = props.getProperty(key);
			String id = getID(key, val);
			TextFlow textFlow = new TextFlow(id);
			textFlow.setContent(val);
			// FIXME fix OpenProps, then put this in:
//			textFlow.getComments().getComments().add(new SimpleComment(null, props.getComment(key)));
			// textFlow.setLang(LocaleId.EN);
			resources.add(textFlow);
		}
	}

	// private String generateHash(String key){
	// try {
	// md5.reset();
	// return new String(Hex.encodeHex(md5.digest(key.getBytes("UTF-8"))));
	// } catch (Exception exc) {
	// throw new RuntimeException(exc);
	// }
	// }

	private String getID(String key, String val) {
		// return generateHash(val); // TODO or just use key??
		return key;
	}

	private Properties loadProps(InputSource inputSource) throws IOException {
		Properties props = new Properties();
		InputStream byteStream = inputSource.getByteStream();
		// NB unlike SAX, we prefer the bytestream over the charstream
		if (byteStream != null) {
			props.load(byteStream);
		} else {
			Reader reader = inputSource.getCharacterStream();
			props.load(reader);
		}
		return props;
	}

}
