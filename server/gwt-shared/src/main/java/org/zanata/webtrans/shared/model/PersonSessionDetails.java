package org.zanata.webtrans.shared.model;

import com.google.gwt.user.client.rpc.IsSerializable;

public class PersonSessionDetails implements IsSerializable {
    private Person person;
    private TransUnitId selectedTransUnitId;

    // for GWT
    @SuppressWarnings("unused")
    private PersonSessionDetails() {
    }

    public PersonSessionDetails(Person person, TransUnitId selectedTransUnitId) {
        this.person = person;
        this.selectedTransUnitId = selectedTransUnitId;
    }

    public Person getPerson() {
        return person;
    }

    public TransUnitId getSelectedTransUnitId() {
        return selectedTransUnitId;
    }

    public void setSelectedTransUnitId(TransUnitId selectedTransUnitId) {
        this.selectedTransUnitId = selectedTransUnitId;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((person == null) ? 0 : person.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PersonSessionDetails other = (PersonSessionDetails) obj;
        // return Objects.equal(person, other.person);
        if (person == null) {
            if (other.person != null) {
                return false;
            }
        } else if (!person.equals(other.person)) {
            return false;
        }
        return true;
    }

}
