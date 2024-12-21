package org.firstinspires.ftc.teamcode.FTCUtilities;

import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.primitives.Primitives;
import com.qualcomm.robotcore.util.ElapsedTime;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Represents an event that is scheduled to execute after a specified delay.
 * The event is tied to a method of an object and can optionally include arguments.
 */
public class EventSchedule implements Runnable {

    private Object eventObject;
    private Method eventMethod;
    private double executeDelay;

    private double timeRemaining;

    private Object[] args;

    ElapsedTime timer = new ElapsedTime();

    private Thread thread;

    private boolean active = false;

    public EventSchedule() {

    }

    /**
     * Constructs an EventSchedule with a target object, method, and optional arguments.
     * @param eventObject The target object on which the method is invoked.
     * @param eventMethodName The name of the method to invoke.
     * @param args Arguments to pass to the method during invocation.
     */
    public EventSchedule(Object eventObject, String eventMethodName, Object... args) {
        active = true;
        this.eventObject = eventObject;
        try {
            Class[] paramTypes = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                Class type = args[i].getClass();
                if(Primitives.isWrapperType(type)) {
                    type = Primitives.unwrap(type);
                }

                paramTypes[i] = type;
            }

            this.eventMethod = eventObject.getClass().getMethod(eventMethodName, paramTypes);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        this.args = args;
    }

    /**
     * Constructs an EventSchedule with a target object and method.
     * @param eventObject The target object on which the method is invoked.
     * @param eventMethod The name of the method to invoke.
     */
    public EventSchedule(Object eventObject, String eventMethod) {
        this.eventObject = eventObject;
        try {
            this.eventMethod = eventObject.getClass().getMethod(eventMethod);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        this.args = new Object[] {};
    }

    /**
     * Marks an event as completed by nullifying its reference.
     * @param completedEvent The completed event to nullify.
     */
    private static void completed(EventSchedule completedEvent) {
        completedEvent = null;
    }

    /**
     * Queues the event for execution after a specified delay.
     * This method starts a new thread to monitor the delay and invoke the event.
     * @param time The delay in seconds before the event is executed.
     */
    public void queue(double time) {
        timer.reset();
        timer.startTime();
        executeDelay = time;
        thread = new Thread(this);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        completed(this);

    }
    


    /**
     * The method executed by the thread to monitor the delay and invoke the scheduled method.
     */
    @Override
    public void run() {
        while (executeDelay - timer.seconds() > 0) {
            timeRemaining = executeDelay - timer.seconds();
        }
        try {
            eventMethod.invoke(eventObject, args);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        completed(this);
    }
}
