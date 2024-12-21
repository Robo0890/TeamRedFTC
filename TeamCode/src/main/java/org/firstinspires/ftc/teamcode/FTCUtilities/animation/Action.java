package org.firstinspires.ftc.teamcode.FTCUtilities.animation;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

public class Action implements Runnable {

    protected ElapsedTime elapsedTime = new ElapsedTime();

    private String actionName = "null";


    protected LinearOpMode opMode;
    private double timeout;


    public Action() {

    }

    @Override
    public void run() {
        elapsedTime.reset();
        elapsedTime.startTime();
    }





}
