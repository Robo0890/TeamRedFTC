package org.firstinspires.ftc.teamcode.FTCUtilities.animation;

import java.util.ArrayList;

public class Keyframe {


    private ArrayList<Action> actions;

    private ArrayList<Thread> threads;

    private boolean isRunning = false;

    public Keyframe(Action[] actions) {
        this.actions = new ArrayList<>();
        for (Action a : actions) {
            addAction(a);
        }
    }
    public Keyframe() {
        this.actions = new ArrayList<>();
    }

    public void addAction(Action action) {
        actions.add(action);
    }

    public void cancel() {
        isRunning = false;
    }

    public void run() {

        isRunning = true;
        threads = new ArrayList<>();

        for (Action action : actions) {
            Thread thread = new Thread(action);
            threads.add(thread);
            thread.start();
        }
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        isRunning = false;
    }

}
