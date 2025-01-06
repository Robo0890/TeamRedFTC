package org.firstinspires.ftc.teamcode.FTCUtilities;

import com.qualcomm.robotcore.hardware.DcMotor;

public class LinearSlide implements Runnable{


    private DcMotor slideMotor;

    private int position = 0;
    private int zeroPosition = 0;

    private boolean active = false;

    private int direction = 1;

    final static public int DIRECTION_FORWARD = 1;
    final static public int DIRECTION_REVERSE = -1;
    final static public int DIRECTION_STATIC = 0;

    public LinearSlide(DcMotor slideMotor) {
        this.slideMotor = slideMotor;
        slideMotor.setTargetPosition(0);
        slideMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        slideMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        zeroPosition = slideMotor.getCurrentPosition();
    }

    public DcMotor getMotor() {
        return slideMotor;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setZeroPosition() {
        zeroPosition = position;
        position = 0;
    }

    public void setTargetPosition(int position) {
        position *= direction;
        this.position = position + zeroPosition;
    }

    public int getCurrentPosition() {
        return slideMotor.getCurrentPosition();
    }

    @Override
    public void run() {

        while (active) {
            slideMotor.setTargetPosition(position);
            if (Math.abs(slideMotor.getCurrentPosition() - slideMotor.getTargetPosition()) > 5) {
                slideMotor.setPower(.1);
            }
            else {
                slideMotor.setPower(0);
            }

        }

    }



}
