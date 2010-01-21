package org.fedorahosted.flies.gwt.rpc;

import net.customware.gwt.dispatch.shared.Result;

/**
 * A result with an offset into the global event queue
 * specifying the current 'state' of the returned data 
 * 
 * @author asgeirf
 */
public interface SequenceResult extends Result{

	public int getSequence();
}
