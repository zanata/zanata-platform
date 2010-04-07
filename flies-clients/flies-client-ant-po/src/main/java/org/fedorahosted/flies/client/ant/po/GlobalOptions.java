package org.fedorahosted.flies.client.ant.po;

interface GlobalOptions {
	static final GlobalOptions EMPTY = new GlobalOptions() {
		@Override
		public boolean getErrors() {
			return false;
		}
		@Override
		public boolean getHelp() {
			return false;
		}
	};

	boolean getHelp();

	boolean getErrors();

}
