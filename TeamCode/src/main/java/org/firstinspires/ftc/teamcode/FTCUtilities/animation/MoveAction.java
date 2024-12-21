package org.firstinspires.ftc.teamcode.FTCUtilities.animation;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

public class MoveAction extends Action implements Runnable {

    private LinearOpMode opMode;

    private double[] targetPosition = {0,0};

    public MoveAction(LinearOpMode opMode, double xPosition, double yPosition, double speed) {
        this.opMode = opMode;
        targetPosition[1] = xPosition;
        targetPosition[2] = yPosition;
    }


}
