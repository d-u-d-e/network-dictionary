package com.eis.networklibrary.replicated;

/**
 * Test resource used in unit tests
 *
 * @author Marco Mariotto
 */
class TestResource {

    private String resourceName;

    TestResource(String resourceName) {
        this.resourceName = resourceName;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof TestResource))
            return false;
        TestResource otherResource = (TestResource) other;
        return resourceName.equals(otherResource.resourceName);
    }

    @Override
    public int hashCode() {
        return resourceName.hashCode();
    }

    @Override
    public String toString() {
        return resourceName;
    }

}