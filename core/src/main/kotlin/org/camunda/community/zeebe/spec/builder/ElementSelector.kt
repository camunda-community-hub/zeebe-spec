package org.camunda.community.zeebe.spec.builder

class ElementSelector internal constructor(val elementName: String?, val elementId: String?) {
    companion object {
        fun byName(elementName: String): ElementSelector {
            return ElementSelector(elementName, null);
        }

        fun byId(elementId: String): ElementSelector {
            return ElementSelector(null, elementId);
        }
    }
}