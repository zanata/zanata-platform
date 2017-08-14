package org.zanata.webtrans.shared.model;

import com.google.common.base.Preconditions;
import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;

/**
 *
 * @author Hannes Eskebaek
 */
public class Locale implements HasIdentifier<IdForLocale>, IsSerializable,
        Serializable {
    private static final long serialVersionUID = 6051655587833509927L;
    private IdForLocale id;
    private String displayName;
    public final static Locale notChosenLocale = new Locale();

    // for GWT
    private Locale() {
    }

    public Locale(IdForLocale id, String displayName) {
        Preconditions.checkNotNull(id, "localeId cannot be null");
        this.id = id;
        this.displayName = displayName;
    }

    public IdForLocale getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }
}
