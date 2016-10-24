package org.zanata.webtrans.client.ui;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

public class UserPanel extends ListItemWidget implements HasManageUserPanel {
    private static final long serialVersionUID = 1L;
    private final Image userImage;
    private final String personName;
    private final ListItemWidget colorLabel = new ListItemWidget();

    public UserPanel(String personName, String imgUrl) {
        super();

        UnorderedListWidget container = new UnorderedListWidget();

        container.setStyleName("list--horizontal");
        this.personName = personName;
        userImage = new Image(imgUrl);

        container.add(new ListItemWidget(userImage));
        container.add(new ListItemWidget(colorLabel));
        container.add(new ListItemWidget(personName));

        add(container);
    }

    @Override
    public void setColor(String color) {
        colorLabel.getElement().getStyle().setProperty("borderColor", color);
        colorLabel.getElement().getStyle().setProperty("borderWidth", "1px");
        colorLabel.getElement().getStyle().setProperty("borderStyle", "solid");
        colorLabel.getElement().getStyle().setProperty("height", "1.25em");
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result =
                prime * result
                        + ((personName == null) ? 0 : personName.hashCode());
        result =
                prime * result
                        + ((userImage == null) ? 0 : userImage.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        UserPanel other = (UserPanel) obj;
        if (personName == null) {
            if (other.personName != null)
                return false;
        } else if (!personName.equals(other.personName))
            return false;
        if (userImage == null) {
            if (other.userImage != null)
                return false;
        } else if (!userImage.equals(other.userImage))
            return false;
        return true;
    }
}
