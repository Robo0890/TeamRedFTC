package org.firstinspires.ftc.teamcode.FTCUtilities;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

public class DriveController {


    private DcMotor frontRight;
    private DcMotor frontLeft;
    private DcMotor backRight;
    private DcMotor backLeft;

    LinearOpMode opMode;

    public DriveController(LinearOpMode opMode, String fr, String fl, String br, String bl) {

        this.opMode = opMode;

        frontRight = opMode.hardwareMap.get(DcMotor.class, fr);
        frontLeft = opMode.hardwareMap.get(DcMotor.class, fl);
        backRight = opMode.hardwareMap.get(DcMotor.class, br);
        backLeft = opMode.hardwareMap.get(DcMotor.class, bl);
    }

    public void moveXY(double x, double y, double h) {
        double axial = y;
        double lateral = -x;
        double yaw = 0;

        double max;

        if (Math.abs(h) < 0.05) h = 0;
        double leftFrontPower  = axial + lateral + yaw;
        double rightFrontPower = axial - lateral - yaw;
        double leftBackPower   = axial - lateral + yaw;
        double rightBackPower  = axial + lateral - yaw;

        // Normalize the values so no wheel power exceeds 100%
        // This ensures that the robot maintains the desired motion.
        max = Math.max(Math.abs(leftFrontPower), Math.abs(rightFrontPower));
        max = Math.max(max, Math.abs(leftBackPower));
        max = Math.max(max, Math.abs(rightBackPower));

        if (max > 1.0) {
            leftFrontPower  /= max;
            rightFrontPower /= max;
            leftBackPower   /= max;
            rightBackPower  /= max;
        }

        frontLeft.setPower(leftFrontPower);
        frontRight.setPower(rightFrontPower);
        backLeft.setPower(leftBackPower);
        backRight.setPower(rightBackPower);

    }

}
