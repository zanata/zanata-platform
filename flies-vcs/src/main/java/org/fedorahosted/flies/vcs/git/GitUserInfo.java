package org.fedorahosted.flies.vcs.git;

import org.fedorahosted.flies.vcs.UserInfo;

public class GitUserInfo implements UserInfo{
	private String email;
	private String username;
	
	public GitUserInfo(String mail, String name){
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
