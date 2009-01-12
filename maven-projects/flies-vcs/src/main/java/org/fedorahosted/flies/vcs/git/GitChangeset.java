package org.fedorahosted.flies.vcs.git;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joda.time.DateTime;

import org.fedorahosted.flies.vcs.Changeset;
import org.fedorahosted.flies.vcs.UserInfo;
import org.fedorahosted.flies.vcs.git.GitCommand;


public class GitChangeset implements Changeset{

	private String idnumber;
	private DateTime timestamp;
	private UserInfo userinfo;
	private File rootpath;
	private Map<String, Modification> changes;
	
	public GitChangeset(String id, File path) {
		idnumber = id;
		rootpath = path;
	}
	
	public GitChangeset(String id, String user, String email, String times, File path) {
		// TODO Auto-generated constructor stub
		idnumber = id;
		String[] part = times.split(" ", 3);
		timestamp = new DateTime(part[0]+"T"+part[1]+part[2]);
		userinfo = new GitUserInfo(email, user);
		rootpath = path;
	}
	
	@Override
	public Map<String, Modification> getChanges() {
		// TODO Auto-generated method stub
		String[] lines = null;
		
		if(changes == null) {
			
			GitCommand command = new GitCommand("log", rootpath, true);
			command.addOptions("-1", "--name-status", "--pretty=format:");
			command.addOptions(idnumber);
			
			String result = command.executeToString();
			//System.out.println(result);
			if(!result.equals("")) {
	         	lines = result.split("\n");
			}
			
			HashMap<String, Modification> map = new HashMap<String, Modification>();
			
	        for(String line:lines)
	        {
	        	if(!line.equals("")) {
	        		String[] addfile = line.split("\t", 2);
	        	if(addfile[0].equals("A"))
					map.put(addfile[1], Modification.Add);
	        	if(addfile[0].equals("M"))
					map.put(addfile[1], Modification.Modify);
	        	if(addfile[0].equals("D"))
					map.put(addfile[1], Modification.Delete);
	        	}
	        }
			
	        changes = Collections.unmodifiableMap(map);
	        }
   
		return changes;
	}

	@Override
	public List<String> getChanges(Modification type) {
		// TODO Auto-generated method stub
		Map<String, Modification> map = getChanges();
		List<String> filelist = new ArrayList<String>();
		for (Map.Entry<String, Modification> entry : map.entrySet()) {
	        if(entry.getValue().equals(type))
	        {
	        	filelist.add(entry.getKey());
	        }
	    }
 		return filelist;
	}

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return idnumber;
	}

	@Override
	public Modification getModificationType(File file) {
		// TODO Auto-generated method stub
		Map<String, Modification> map = getChanges();
		return map.get(file.getName());
	}

	@Override
	public DateTime getTimestamp() {
		// TODO Auto-generated method stub
		return timestamp;
	}

	@Override
	public UserInfo getUser() {
		// TODO Auto-generated method stub
		return userinfo;
	}
	
}
