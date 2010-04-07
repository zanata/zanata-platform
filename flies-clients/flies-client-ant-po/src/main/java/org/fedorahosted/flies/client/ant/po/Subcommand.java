package org.fedorahosted.flies.client.ant.po;


public interface Subcommand {

	void processArgs(String[] args, GlobalOptions globals) throws Exception;

}
