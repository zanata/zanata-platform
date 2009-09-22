package org.fedorahosted.flies.rest.client;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.fedorahosted.flies.LocaleId;

import com.google.common.collect.ImmutableSet;
/**
 * Valid values are {"all"|"source"|languages}
 * where languages = lang1[,lang]* (e.g de,fr-FR,nb-NO)
 * @author asgeirf
 *
 */
public class ContentQualifier{
		
		private final boolean all;
		private final boolean source;
		private final boolean none;
		private final Set<LocaleId> languages;

		private ContentQualifier(LocaleId ... locales){
			all = none = source = false;
			languages = ImmutableSet.of(locales);
		}
		
		private ContentQualifier(String qualifier) {
			if("all".equals(qualifier) ){
				all = true;
				source = none = false;
				languages = Collections.emptySet();
			}
			else if("source".equals(qualifier)){
				source = true;
				all = none = false;
				languages = Collections.emptySet();
			}
			else if("".equals(qualifier)){
				none = true;
				all = source = false;
				languages = Collections.emptySet();
			}
			else{
				all = source = none = false;
				String [] langs = qualifier.split(";");
				languages = new HashSet<LocaleId>();
				for (int i = 0; i < langs.length; i++) {
					LocaleId locale = new LocaleId(langs[i]);
					languages.add(locale);
				}
			}
				
		}
		
		public boolean isAll(){
			return all;  
		}
		
		public boolean isSource(){
			return source;
		}

		public boolean isNone(){
			return none;
		}
		
		public boolean isLanguages(){
			return !all && !source && !none;
		}
		
		public Set<LocaleId> getLanguages(){
			return languages;
		}
		
		public static final ContentQualifier ALL = new ContentQualifier("all");
		public static final ContentQualifier SOURCE = new ContentQualifier("source");
		public static final ContentQualifier NONE = new ContentQualifier("");
		
		public static final ContentQualifier fromLocales(LocaleId ... locales){
			return new ContentQualifier(locales);
		}
		
		public static ContentQualifier valueOf(String str) {
			return new ContentQualifier(str);
		}
		
		@Override
		public String toString() {
			if(all) return "all";
			if(none) return "";
			if(source) return "source";
			return StringUtils.join(languages, ';');
		}
	}
