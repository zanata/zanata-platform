/*
 * Ext GWT - Ext for GWT
 * Copyright(c) 2007-2009, Ext JS, LLC.
 * licensing@extjs.com
 * 
 * http://extjs.com/license
 */
package org.fedorahosted.flies.webtrans.client;

import java.io.Serializable;

import com.extjs.gxt.ui.client.data.BaseTreeModel;

public class Folder extends BaseTreeModel implements Serializable {

  private static int ID = 0;
  
  public Folder() {
    set("id", ID++);
  }

  public Folder(String name) {
    set("id", ID++);
    set("name", name);
  }

  public Folder(String name, BaseTreeModel[] children) {
    this(name);
    for (int i = 0; i < children.length; i++) {
      add(children[i]);
    }
  }

  public Integer getId() {
    return (Integer) get("id");
  }

  public String getName() {
    return (String) get("name");
  }

  public String toString() {
    return getName();
  }

}
