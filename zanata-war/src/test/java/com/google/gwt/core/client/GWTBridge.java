package com.google.gwt.core.client;

/**
 * After upgrade to GWT 2.5, some of the gwt related libraries start to break.
 * i.e. gwt-log see http://code.google.com/p/gwt-log/issues/detail?id=70
 * <p/>
 * Basically the libraries still reference com.google.gwt.core.client.GWTBridge and
 * it's been moved to com.google.gwt.core.shared in GWT 2.5.
 * Before they release new version this class needs to be in class path to make things work.
 *
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public abstract class GWTBridge extends com.google.gwt.core.shared.GWTBridge
{
}
