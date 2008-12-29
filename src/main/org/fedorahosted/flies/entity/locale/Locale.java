package org.fedorahosted.flies.entity.locale;

import java.awt.ComponentOrientation;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Version;

import org.fedorahosted.flies.entity.TranslationTeam;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;

import com.ibm.icu.text.Bidi;
import com.ibm.icu.util.ULocale;

@Entity
public class Locale implements Serializable{

	private Long id;
    private Integer version;
    private String localeId;
    private String name;
    
    private String nativeName;
    
    private String countryCode;
    private String languageCode;
    private String variant;
    private String script;
    
    
    private Boolean rightToLeft = false;
    
    private Locale parent;
    
    private List<Locale> children;
    
    private List<Locale> friends; // e.g. nn, nb.
    
    private List<TranslationTeam> translationTeams;
    
    @Id @GeneratedValue
    public Long getId() {
        return id;
    }

    public static Locale fromLocale(java.util.Locale locale){
    	Locale l = new Locale();
    	l.setCountryCode(locale.getCountry());
    	l.setLanguageCode(locale.getLanguage());
    	l.setVariant(locale.getVariant());
    	String usName = locale.getDisplayName(java.util.Locale.US);
    	l.setName(usName);
    	String dispName = locale.getDisplayName(locale);
    	if(!usName.equals(dispName)){
    		l.setNativeName(dispName);
    	}
    	return l;
    }
    
    public void setId(Long id) {
        this.id = id;
    }

    @Version
    public Integer getVersion() {
        return version;
    }

    private void setVersion(Integer version) {
        this.version = version;
    }

    @NotEmpty
    public String getLocaleId() {
		return localeId;
	}
    
    public void setLocaleId(String localeId) {
		this.localeId = localeId;
	}
    
    
    public String getName() {
		return name;
	}
    
    public String getNativeName() {
		return nativeName;
	}
    
    public void setNativeName(String nativeName) {
		this.nativeName = nativeName;
	}
    
    public void setName(String name) {
		this.name = name;
	}
    
    
    public String getCountryCode() {
		return countryCode;
	}
    
    public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}
    
    public String getLanguageCode() {
		return languageCode;
	}
    
    public void setLanguageCode(String languageCode) {
		this.languageCode = languageCode;
	}

    @NotNull
    public Boolean isRightToLeft() {
		return rightToLeft;
	}
    
    public void setRightToLeft(Boolean rightToLeft) {
		this.rightToLeft = rightToLeft;
	}
    
    public String getVariant() {
		return variant;
	}
    
    public void setVariant(String variant) {
		this.variant = variant;
	}

    public String getScript() {
		return script;
	}
    
    public void setScript(String script) {
		this.script = script;
	}
    
    @ManyToMany
    @JoinTable(
            name="Locale_Friends",
            joinColumns=@JoinColumn(name="localeId"),
            inverseJoinColumns=@JoinColumn(name="friendLocaleId")
        )
    public List<Locale> getFriends() {
		return friends;
	}
    
    public void setFriends(List<Locale> friends) {
		this.friends = friends;
	}
    
    @OneToMany(mappedBy="parent")
    public List<Locale> getChildren() {
		return children;
	}
    
    public void setChildren(List<Locale> children) {
		this.children = children;
	}
    
    @ManyToOne
    @JoinColumn(name="parentId")
    public Locale getParent() {
		return parent;
	}
    
    public void setParent(Locale parent) {
		this.parent = parent;
	}
    
    @ManyToMany
    @JoinTable(
            name="TranslationTeam_Locale",
            joinColumns=@JoinColumn(name="localeId"),
            inverseJoinColumns=@JoinColumn(name="translationTeamId")
        )
    public List<TranslationTeam> getTranslationTeams() {
		return translationTeams;
	}
    
    public void setTranslationTeams(List<TranslationTeam> translationTeams) {
		this.translationTeams = translationTeams;
	}
    
 
    public static void main(String[] args) {
    	ULocale locales[] = ULocale.getAvailableLocales();
    	
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < locales.length; i++) {
			builder.append("insert into Locale(id, version, localeId, name, nativeName, countryCode, languageCode, script, rightToLeft, variant) values(");
			builder.append(i+1);
			builder.append(", 0, '");
			builder.append(locales[i].getName());
			builder.append("', '");
			builder.append(locales[i].getDisplayName(ULocale.US));
			builder.append("', '");
			builder.append(locales[i].getDisplayName(locales[i]));
			builder.append("', '");
			builder.append(locales[i].getCountry());
			builder.append("', '");
			builder.append(locales[i].getLanguage());
			builder.append("', ");
			if(locales[i].getScript().isEmpty()){
				builder.append("NULL");
			}
			else{
				builder.append("'");
				builder.append(locales[i].getScript());
				builder.append("'");
			}
			builder.append(", ");
			if(!"left-to-right".equals(locales[i].getCharacterOrientation())){
				builder.append("TRUE, '");
			}
			else{
				builder.append("FALSE, '");
			}
			builder.append(locales[i].getVariant());
			builder.append("')\n");
			
		}
		builder.append("\n");
		for (int i = 0; i < locales.length; i++) {
			if(locales[i].getFallback() != ULocale.ROOT){
				int parentId = findLocId(locales[i].getFallback(), locales);
				
				if(parentId != -1){
					builder.append("update Locale set parentId = " + 
							(parentId+1) + " where id = " + (i+1) + "\n");
				}
			}
		}
		
		System.out.println(builder.toString());
	}
    
    private static int findLocId(ULocale fallback, ULocale[] locales){
    	for (int i = 0; i < locales.length; i++) {
			if(locales[i].equals(fallback))
				return i;
		}
    	return -1;
    }
}
