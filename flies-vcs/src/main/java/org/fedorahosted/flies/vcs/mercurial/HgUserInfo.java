package org.fedorahosted.flies.vcs.mercurial;

import org.fedorahosted.flies.vcs.UserInfo;

public class HgUserInfo implements UserInfo{
	private String email;
	private String username;
	
	public HgUserInfo(String mail, String name){
		email = mail;
		username = name;
	}
	
	@Override
	public String getEmail() {
		// TODO Auto-generated method stub
		return email;
	}

	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return username;
	}

}
