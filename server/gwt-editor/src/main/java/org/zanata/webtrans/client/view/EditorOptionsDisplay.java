package org.zanata.webtrans.client.view;

import com.google.gwt.user.client.ui.IsWidget;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;

import org.zanata.webtrans.client.presenter.UserConfigHolder;
import org.zanata.webtrans.client.ui.EnumRadioButtonGroup;
import org.zanata.webtrans.shared.model.DiffMode;
import org.zanata.webtrans.shared.rpc.NavOption;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public interface EditorOptionsDisplay extends WidgetDisplay {
    void setListener(Listener listener);
    void setSourceLangListBox(IsWidget sourceLangListBoxView);
    void setOptionsState(UserConfigHolder.ConfigurationState state);

    interface Listener extends
            EnumRadioButtonGroup.SelectionChangeListener<NavOption> {
        void onPageSizeClick(int pageSize);

        void onEnterSaveOptionChanged(Boolean enterSaveApproved);

        void onEditorButtonsOptionChanged(Boolean editorButtons);

        void onUseCodeMirrorOptionChanged(Boolean useCodeMirrorChkValue, boolean force);

        void onShowSaveApprovedWarningChanged(Boolean showSaveApprovedWarning);

        void onTransMemoryDisplayModeChanged(DiffMode displayMode);

        void onTMOrGlossaryDisplayOptionsChanged(Boolean showTMChkValue,
                Boolean showGlossaryChkValue);

        void onDisplayTransUnitDetailsOptionChanged(
                Boolean showTransUnitDetailsChkValue);
    }
}
