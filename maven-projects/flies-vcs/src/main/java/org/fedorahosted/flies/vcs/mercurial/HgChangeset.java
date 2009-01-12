package org.fedorahosted.flies.vcs.mercurial;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joda.time.DateTime;

import org.fedorahosted.flies.vcs.Changeset;
import org.fedorahosted.flies.vcs.UserInfo;

public class HgChangeset implements Changeset {
	
	private String idnumber;
	private DateTime timestamp;
	private UserInfo userinfo;
	private File rootpath;
	private Map<String, Modification> changes;
	
	public HgChangeset(String id, File path) {
		idnumber = id;
		rootpath = path;
	}
	
	public HgChangeset(String id, String times, String user, String email, File path) {
		// TODO Auto-generated constructor stub
		idnumber = id;
		String[] part = times.split(" ", 3);
		timestamp = new DateTime(part[0]+"T"+part[1]+part[2]);
		userinfo = new HgUserInfo(email, user);
		rootpath = path;
	}

	@Override
	public Map<String, Modification> getChanges() {
		// TODO Auto-generated method stub
		String[] lines = null;
		
		if(changes == null) {
			
			HgCommand command = new HgCommand("log", rootpath, true);
			command.addOptions("-r", idnumber);
			command.addOptions("--template", "{file_adds}:{file_dels}:{file_mods}");
			String result = command.executeToString();
			if(result!=null) {
	         	lines = result.split(":", 3);
			}
			
			HashMap<String, Modification> map = new HashMap<String, Modification>();
			
	        if(!lines[0].equals("")) {
				String[] addfile = lines[0].split(" ");
				for(String name:addfile)
				{
					map.put(name, Modification.Add);
				}
			}
			
			if(!lines[1].equals("")) {
				String[] delfile = lines[1].split(" ");
	        	for(String name:delfile)
	        	{
	        		map.put(name, Modification.Delete);
	        	}
			}
			
			if(!lines[2].equals("")) {
				String[] modfile = lines[2].split(" ");
	        	for(String name:modfile)
	        	{
	        		map.put(name, Modification.Modify);
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
