/*
 * This works around an issue with ajax in jsf that causes the view state to be
 * lost or inaccessible.
 *
 * See http://stackoverflow.com/a/11412138/297938
 */

(function () {

jsf.ajax.addOnEvent(function(data) {
    if (data.status == "success") {
        fixViewState(data.responseXML);
    }
});

function fixViewState(responseXML) {
    var viewState = getViewState(responseXML);

    if (viewState) {
        for (var i = 0; i < document.forms.length; i++) {
            var form = document.forms[i];

            if (form.method == "post") {
                if (!hasViewState(form)) {
                    createViewState(form, viewState);
                }
            }
            else { // PrimeFaces also adds them to GET forms!
                removeViewState(form);
            }
        }
    }
}

function getViewState(responseXML) {
    var updates = responseXML.getElementsByTagName("update");

    for (var i = 0; i < updates.length; i++) {
        var update = updates[i];

        if (update.getAttribute("id").match(/^([\w]+:)?javax\.faces\.ViewState(:[0-9]+)?$/)) {
            return update.firstChild.nodeValue;
        }
    }

    return null;
}

function hasViewState(form) {
    for (var i = 0; i < form.elements.length; i++) {
        if (form.elements[i].name == "javax.faces.ViewState") {
            return true;
        }
    }

    return false;
}

function createViewState(form, viewState) {
    var hidden;

    try {
        hidden = document.createElement("<input name='javax.faces.ViewState'>"); // IE6-8.
    } catch(e) {
        hidden = document.createElement("input");
        hidden.setAttribute("name", "javax.faces.ViewState");
    }

    hidden.setAttribute("type", "hidden");
    hidden.setAttribute("value", viewState);
    hidden.setAttribute("autocomplete", "off");
    form.appendChild(hidden);
}

function removeViewState(form) {
    for (var i = 0; i < form.elements.length; i++) {
        var element = form.elements[i];
        if (element.name == "javax.faces.ViewState") {
            element.parentNode.removeChild(element);
        }
    }
}

})();
