package org.example.follow.me.api;

public enum RoomEnum {

    KITCHEN("kitchen"),
    BEDROOM("bedroom"),
    LIVINGROOM("livingroom"),
    BATHROOM("bathroom");

    private String name;

    public String getName() {
        return this.name;
    }

    private RoomEnum(String room) {
    	this.name=room;
    }

}
