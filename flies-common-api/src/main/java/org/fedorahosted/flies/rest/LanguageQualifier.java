package org.fedorahosted.flies.rest;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.fedorahosted.flies.common.LocaleId;

import com.google.common.collect.ImmutableSet;
/**
 * Valid values are {"all"|languages}
 * where languages = lang1[;lang]* (e.g de;fr-FR;nb-NO)
 * @author asgeirf
 *
 */
public class LanguageQualifier{
		
		private final Set<LocaleId> languages;

		private LanguageQualifier(LocaleId ... locales){
			languages = ImmutableSet.of(locales);
		}

		public static final LanguageQualifier ALL = new LanguageQualifier();
		
		private LanguageQualifier() {
			languages = Collections.emptySet();
		}
		
		public boolean isAll(){
			return languages.isEmpty();  
		}
		
		public Set<LocaleId> getLanguages(){
			return languages;
		}
		
		public static LanguageQualifier valueOf(String str) {
			if("all".equals(str)) {
				return ALL; 
			}
			else if(str == null || str.trim().isEmpty()) {
				throw new IllegalArgumentException("str cannot be null or empty");
			}
			String [] splitValues = StringUtils.split(str, ';');
			HashSet<LocaleId> locales = new HashSet<LocaleId>(splitValues.length);
			for (String val : splitValues) {
				LocaleId elem = new LocaleId(val);
				locales.add(elem);
			}

			return new LanguageQualifier(locales.toArray(new LocaleId[]{}));
			
		}
		
		@Override
		public String toString() {
			if( languages.isEmpty() ) return "all";
			return StringUtils.join(languages, ';');
		}
	}
