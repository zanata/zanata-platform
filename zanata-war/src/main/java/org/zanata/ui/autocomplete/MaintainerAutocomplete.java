package org.zanata.ui.autocomplete;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.Component;
import org.zanata.dao.PersonDAO;
import org.zanata.model.HPerson;
import org.zanata.seam.scope.ConversationScopeMessages;
import org.zanata.ui.AbstractAutocomplete;
import org.zanata.ui.FilterUtil;
import org.zanata.util.ZanataMessages;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public abstract class MaintainerAutocomplete extends
        AbstractAutocomplete<HPerson> {

    protected PersonDAO personDAO = (PersonDAO) Component
            .getInstance(PersonDAO.class);

    protected ZanataMessages zanataMessages = (ZanataMessages) Component
            .getInstance(ZanataMessages.class);

    protected ConversationScopeMessages conversationScopeMessages =
            ConversationScopeMessages.instance();

    protected abstract List<HPerson> getMaintainers();

    protected abstract void addMaintainers(HPerson maintainer);

    protected abstract void displaySuccessfulMessage(String maintainerName);

    protected abstract void update();

    @Override
    public List<HPerson> suggest() {
        List<HPerson> personList = personDAO.findAllContainingName(getQuery());
        return FilterUtil.filterOutPersonList(getMaintainers(), personList);
    }

    @Override
    public void onSelectItemAction() {
        if (StringUtils.isEmpty(getSelectedItem())) {
            return;
        }

        HPerson maintainer = personDAO.findByUsername(getSelectedItem());
        addMaintainers(maintainer);
        update();
        reset();
        displaySuccessfulMessage(maintainer.getName());
    }
}
