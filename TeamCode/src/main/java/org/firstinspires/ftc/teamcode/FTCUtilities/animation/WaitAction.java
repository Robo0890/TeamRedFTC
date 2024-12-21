package org.firstinspires.ftc.teamcode.FTCUtilities.animation;

public class WaitAction extends Action implements Runnable {

    private double timeout;


    public WaitAction(double time) {
        timeout = time;
        elapsedTime.reset();
    }

    @Override
    public void run() {
        super.run();

        elapsedTime.startTime();

        while(elapsedTime.seconds() < timeout) {

        }

    }

}
