package org.fedorahosted.flies.util;

import java.security.MessageDigest;

import org.jboss.seam.security.management.PasswordHash;
import org.jboss.seam.util.Base64;

public class PasswordGenerator {

	public static String generatePassword(String saltPhrase, String password, String algorithm) {
		try {        
	        MessageDigest md = MessageDigest.getInstance(algorithm);
	                 
	        if (saltPhrase != null)
	        {
	           md.update(saltPhrase.getBytes());
	           byte[] salt = md.digest();
	           
	           md.reset();
	           md.update(password.getBytes());
	           md.update(salt);
	        }
	        else
	        {
	           md.update(password.getBytes());
	        }
	        
	        byte[] raw = md.digest();
	        return Base64.encodeBytes(raw);
	    } 
	    catch (Exception e) {
	        throw new RuntimeException(e);        
	    } 
		
	}
	
	
	public static void main(String[] args) {
		String [] users;
		if(args.length != 0){
			users = args;
		}
		else{
			users = new String[]{
					"joe", "bob"
			};
		}

		for(int i=0;i<users.length;i++){
			System.out.println("User: "
						+ users[i] 
						+ " \"" 
						+ generatePassword(users[i], users[i], PasswordHash.ALGORITHM_MD5)
						+ "\"");
		}
	}
}
