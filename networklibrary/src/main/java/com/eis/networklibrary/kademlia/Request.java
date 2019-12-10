package com.eis.networklibrary.kademlia;

public enum Request {
    JOIN_PROPOSAL("JP"),
    PING("PI"),
    STORE("ST"),
    FIND_NODE("FN"),
    FIND_VALUE("FV");

    String command;

    Request(String command) {
        this.command = command;
    }

    String getCommand() {
        return command;
    }
}
