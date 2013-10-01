package org.zanata.webtrans.client.ui;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RadioButton;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class EnumRadioButtonGroup<E extends Enum<?>> implements
        ValueChangeHandler<Boolean> {
    private final String groupName;
    private final Map<E, RadioButton> radioButtons;
    private final List<IsWidget> widgets;
    private E currentSelection;
    private SelectionChangeListener<E> selectionChangeListener;

    public EnumRadioButtonGroup(String groupName, Class<E> enumClass,
            EnumRenderer<E> enumRenderer) {
        this.groupName = groupName;
        ImmutableMap.Builder<E, RadioButton> mapBuilder =
                ImmutableMap.builder();
        ImmutableList.Builder<IsWidget> listBuilder = ImmutableList.builder();
        for (E anEnum : enumClass.getEnumConstants()) {
            RadioButton radioButton =
                    new RadioButton(groupName, enumRenderer.render(anEnum));
            mapBuilder.put(anEnum, radioButton);
            listBuilder.add(radioButton);
            radioButton.addValueChangeHandler(this);
        }
        radioButtons = mapBuilder.build();
        widgets = listBuilder.build();
    }

    public E getSelected() {
        for (Map.Entry<E, RadioButton> entry : radioButtons.entrySet()) {
            RadioButton radioButton = entry.getValue();
            if (radioButton.getValue()) {
                return entry.getKey();
            }
        }
        return null;
    }

    public EnumRadioButtonGroup<E> addToContainer(
            HasWidgets.ForIsWidget container) {
        for (IsWidget widget : widgets) {
            container.add(widget);
        }
        return this;
    }

    public EnumRadioButtonGroup<E> setDefaultSelected(E option) {
        for (Map.Entry<E, RadioButton> entry : radioButtons.entrySet()) {
            if (entry.getKey() == option) {
                RadioButton radioButton = entry.getValue();
                radioButton.setValue(true, true); // fire event
                currentSelection = entry.getKey();
            }
        }
        return this;
    }

    public void setSelectionChangeListener(SelectionChangeListener<E> listener) {
        selectionChangeListener = listener;
    }

    @Override
    public void onValueChange(ValueChangeEvent<Boolean> radioButtonChangeEvent) {
        if (radioButtonChangeEvent.getValue()) {
            for (Map.Entry<E, RadioButton> entry : radioButtons.entrySet()) {
                if (entry.getValue() == radioButtonChangeEvent.getSource()) {
                    if (currentSelection != entry.getKey()) {
                        currentSelection = entry.getKey();
                        selectionChangeListener.onSelectionChange(groupName,
                                currentSelection);
                    }
                }
            }
        }
    }

    public interface SelectionChangeListener<E extends Enum<?>> {
        void onSelectionChange(String groupName, E value);
    }
}
