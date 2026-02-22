package com.temporal.initiations.messages.domain.workflows;

public final class SendLevel1Response {
    public SendLevel1Response() {
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj != null && obj.getClass() == this.getClass();
    }

    @Override
    public int hashCode() {
        return 1;
    }

    @Override
    public String toString() {
        return "SendLevel1Response[]";
    }

}
