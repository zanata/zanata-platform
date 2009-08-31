package org.fedorahosted.flies.adapter.po.publican;

import java.io.File;


public class ProjectAdapterFactory {

	public static final class NoSuchAdapterException extends RuntimeException {
		public NoSuchAdapterException(String adapterId, int adapterVersion) {
			super("No Adapter '" + adapterId + "' version " + adapterVersion);
		}
	}

	public IProjectAdapter create(String adapterId, File baseDirectory) {
		return create(adapterId, -1, baseDirectory);
	}

	public IProjectAdapter create(String adapterId, int adapterVersion,
			File baseDirectory) {
		if (PublicanProjectAdapter.ID.equals(adapterId)) {
			switch (adapterVersion) {
			case 1:
			case -1:
				return new PublicanProjectAdapter(baseDirectory);
			}
		}

		throw new NoSuchAdapterException(adapterId, adapterVersion);
	}

}
