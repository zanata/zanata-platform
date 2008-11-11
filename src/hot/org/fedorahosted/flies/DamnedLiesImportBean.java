package org.fedorahosted.flies;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Validators;
import org.jboss.seam.international.StatusMessages;
import org.jboss.seam.log.Log;
import org.jboss.seam.faces.FacesMessages;
import org.fedorahosted.flies.validator.url.Url;
import org.hibernate.validator.ClassValidator;
import org.hibernate.validator.InvalidValue;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;

@Stateful
@Name("dlImport")
public class DamnedLiesImportBean implements DamnedLiesImport {

	@Logger
	private Log log;

	@In
	FacesMessages facesMessages;
	
	@In
	StatusMessages statusMessages;

	private String url;
	private String source;
	
	private String step = "step_1";
	
	public String damnedLiesImport() {
		
		switch(getStepAsInt()){
		case 2:
			return step2();
		case 3:
			return step3();
		default:
			return step1();
		}
	}
	
	private String step1(){
		if ("other".equals(source)) {
			
			ClassValidator<DamnedLiesImportBean> validator = Validators.instance().getValidator(this);
			
			InvalidValue[] ivs = validator
					.getPotentialInvalidValues("url", url);
			if (ivs.length > 0) {
				statusMessages.add(ivs[0]);
				facesMessages.addToControl("url", "blah!");
				return null;
			}
		}
		setStep(2);
		return null;
		
	}
	
	private String step2(){
		statusMessages.add("All good!");
		setStep(3);
		return null;
		
	}
	
	private String step3(){
		statusMessages.add("All good - at step 3!");
		setStep(1);
		return null;
	}
	

	@Url
	@NotEmpty
	public String getUrl() {
		return url;
	}

	@NotNull
	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getStep() {
		return step;
	}
	
	
	private int getStepAsInt(){
		return Integer.valueOf(step.substring(5));
	}
	private void setStep(int step){
		setStep("step_"+step);
	}
	
	public void setStep(String step) {
		
		this.step = step;
	}
	
	@Destroy
	@Remove
	public void destroy() {
	}

}
