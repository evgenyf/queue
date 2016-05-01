package com.gigaspaces.queue;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Barak Bar Orion
 * on 3/30/16.
 *
 * @since 11.0
 */
public class EventsQueue<T extends Event> implements BlockingQueue<T> {
    private List<T> data = new LinkedList<>();
    private final int size;

    public EventsQueue(int size) {
        this.size = size;
    }

    @Override
    public synchronized T get() throws InterruptedException {
        while(data.isEmpty()){
            wait();
        }

        return data.remove(0);
    }

    @Override
    public synchronized void put(T t) throws InterruptedException {

        Event newEvent = t;
        boolean contains = data.contains( newEvent );

        //if such event already exists in the list
        if( contains ){
            Op operation = newEvent.getOperation();
            boolean compressionAllowed = allowToCompress(operation);
            if( compressionAllowed ){
                data.remove( newEvent );
            }
        }

        addEvent(t);
    }

    private void addEvent( T t ){
        if( data.size() >= size ){
            throw new IllegalStateException();
        }

        data.add( t );
        notifyAll();
    }

    private boolean allowToCompress( Op operation ){

        switch ( operation ){
            case UPDATE:
                return true;
            default:
                return false;
        }
    }


    public static void main( String[] args ){
        final EventsQueue eventsQueue = new EventsQueue( 10 );


        try {
            eventsQueue.put( new Event( (byte)1, Op.CREATE ) );
            eventsQueue.get();

            (new Thread(){  public void run(){
                try {
                    eventsQueue.put( new Event( (byte)1, Op.CREATE ) );
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }}).start();

            eventsQueue.get();

            eventsQueue.put( new Event( (byte)1, Op.CREATE ) );
            eventsQueue.put( new Event( (byte)1, Op.CREATE ) );
            eventsQueue.put( new Event( (byte)1, Op.CREATE ) );
            eventsQueue.put( new Event( (byte)1, Op.CREATE ) );

            eventsQueue.get();

            eventsQueue.put( new Event( (byte)2, Op.UPDATE ) );
            eventsQueue.put( new Event( (byte)2, Op.UPDATE ) );
            eventsQueue.put( new Event( (byte)2, Op.UPDATE ) );

            eventsQueue.put( new Event( (byte)3, Op.UPDATE ) );
            eventsQueue.put( new Event( (byte)3, Op.UPDATE ) );
            eventsQueue.put( new Event( (byte)3, Op.UPDATE ) );

            eventsQueue.put( new Event( (byte)3, Op.DELETE ) );
            eventsQueue.put( new Event( (byte)3, Op.DELETE ) );
            eventsQueue.put( new Event( (byte)3, Op.DELETE ) );



        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class MyPutThread extends Thread{
        private final EventsQueue eventsQueue;
        private final Event event;

        public MyPutThread( EventsQueue eventsQueue, Event event ){
            this.eventsQueue = eventsQueue;
            this.event = event;
        }

        public void run(){
            try {
                eventsQueue.put( new Event( (byte)1, Op.CREATE ) );
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private class MyGetThread extends Thread{
        private final EventsQueue eventsQueue;

        public MyGetThread( EventsQueue eventsQueue, Event event ){
            this.eventsQueue = eventsQueue;
        }

        public void run(){
            try {
                eventsQueue.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}