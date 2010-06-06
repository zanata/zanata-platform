package org.fedorahosted.flies.adapter.properties;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.rest.dto.deprecated.Document;
import org.fedorahosted.flies.rest.dto.deprecated.IExtensible;
import org.fedorahosted.flies.rest.dto.deprecated.TextFlow;
import org.fedorahosted.flies.rest.dto.deprecated.TextFlowTarget;
import org.fedorahosted.flies.rest.dto.deprecated.TextFlowTargets;
import org.fedorahosted.openprops.Properties;

public class PropWriter {

	private static void logVerbose(String msg) {
		System.out.println(msg);
	}

	private static void makeParentDirs(File f) {
		File parentFile = f.getParentFile();
		if (parentFile != null)
			parentFile.mkdirs();
	}

	public static void write(final Document doc, final File baseDir, boolean exportRoot)
			throws IOException {
		File docDir = new File(baseDir, doc.getPath());
		File baseFile = new File(docDir, doc.getName());
		makeParentDirs(baseFile);

		if (exportRoot) {
			logVerbose("Creating base file " + baseFile);
			Properties props = new Properties();
			for (TextFlow textFlow : doc.getTextFlows()) {
				props.setProperty(textFlow.getId(), textFlow.getContent());
				if (textFlow.hasComment() && textFlow.getComment().getValue() != null)
					props.setComment(textFlow.getId(), textFlow.getComment().getValue());
			}
			// props.store(System.out, null);
			PrintStream out = new PrintStream(new FileOutputStream(baseFile));
			props.store(out, null);
		}
		
		String baseName = baseFile.getName();
		String bundleName = baseName.substring(0, baseName.length()
				- ".properties".length());

		Map<LocaleId, Properties> targetProps = new HashMap<LocaleId, Properties>();
		for (TextFlow textflow : doc.getTextFlows()) {
			for (TextFlowTarget target : getTargets(textflow)) {
				Properties targetProp = targetProps.get(target.getLang());
				if (targetProp == null) {
					targetProp = new Properties();
					targetProps.put(target.getLang(), targetProp);
				}
				targetProp.setProperty(textflow.getId(), target
						.getContent());
				if (target.hasComment() && target.getComment().getValue() != null)
					targetProp.setComment(textflow.getId(), target.getComment().getValue());
			}
		}
		Set<LocaleId> targetLangs = targetProps.keySet();

		for (LocaleId lang : targetLangs) {
			File langFile = new File(docDir, bundleName + "_"
					+ lang.toJavaName() + ".properties");
			logVerbose("Creating target file " + langFile);
			Properties targetProp = targetProps.get(lang);
			// targetProp.store(System.out, null);
			PrintStream out2 = new PrintStream(new FileOutputStream(langFile));
			targetProp.store(out2, null);
		}

	}

	/*
	 * private static Set<LocaleId> buildTargetLangs(Document doc) {
	 * Set<LocaleId> targetLangs = doc.getTargetLanguages();
	 * if(targetLangs.isEmpty()) { for (Resource resource : doc.getResources())
	 * { for (TextFlowTarget target : getTargets(resource)) {
	 * targetLangs.add(target.getLang()); } } } return targetLangs; }
	 */

	private static Set<TextFlowTarget> getTargets(IExtensible resource) {
		TextFlowTargets targets = resource.getExtension(TextFlowTargets.class);
		if (targets != null) {
			return targets.getTargets();
		} else {
			return Collections.EMPTY_SET;
		}
	}
}
