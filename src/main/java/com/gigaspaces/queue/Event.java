package com.gigaspaces.queue;

/**
 * Created by Barak Bar Orion
 * on 3/30/16.
 *
 * @since 11.0
 */
public class Event{
    private byte id;
    private Op operation;

    public Event(byte id, Op operation) {
        this.id = id;
        this.operation = operation;
    }

    public byte getId() {
        return id;
    }

    public Op getOperation() {
        return operation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Event event = (Event) o;

        if (id != event.id) return false;
        return operation == event.operation;

    }

    @Override
    public int hashCode() {
        int result = (int) id;
        result = 31 * result + operation.hashCode();
        return result;
    }
}
