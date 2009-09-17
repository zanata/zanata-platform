package org.fedorahosted.flies.rest.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.fedorahosted.flies.LocaleId;
/**
 * Valid values are {"all"|"source"|languages}
 * where languages = lang1[,lang]* (e.g de,fr-FR,nb-NO)
 * @author asgeirf
 *
 */
public class ContentQualifier{
		
		private final boolean all;
		private final boolean source;
		private final Set<LocaleId> languages;
		
		public ContentQualifier(String qualifier) {
			if("all".equals(qualifier) ){
				all = true;
				source = false;
				languages = Collections.EMPTY_SET;
			}
			else if("source".equals(qualifier)){
				all = false;
				source = true;
				languages = Collections.EMPTY_SET;
			}
			else{
				all = source = false;
				String [] langs = qualifier.split(",");
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

		public boolean isLanguages(){
			return !all && !source;
		}
		
		public Set<LocaleId> getLanguages(){
			return languages;
			
		}

	}
