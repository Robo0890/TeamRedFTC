package org.firstinspires.ftc.teamcode.FTCUtilities.animation;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.firstinspires.ftc.teamcode.FTCUtilities.DriveController;

import java.lang.reflect.Field;


public class MoveAction extends Action implements Runnable {

    private LinearOpMode opMode;

    private double targetPositionX;
    private double targetPositionY;
    private double targetRotation;

    private RobotPose pose;

    private DriveController driveController;

    public boolean running = false;

    public MoveAction(LinearOpMode opMode, double xPosition, double yPosition, double rotation) {
        this.opMode = opMode;
        targetPositionX = xPosition;
        targetPositionY = yPosition;
        targetRotation = rotation;

        try {
            Field poseField = opMode.getClass().getField("pose");
            pose = (RobotPose) poseField.get(opMode);
            Field DCField = opMode.getClass().getField("driveController");
            driveController = (DriveController) DCField.get(opMode);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public Vector2D rotateVector(Vector2D vector2D, double radians) {
        double x0 = vector2D.getX();
        double y0 = vector2D.getY();
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);

        double vectorX = x0 * cos - y0 * sin;
        double vectorY = x0 * sin + y0 * cos;

        return new Vector2D(vectorX,vectorY);
    }

    public void run() {
        super.run();
        running = true;

        System.out.println(targetPositionX);
        System.out.println(targetPositionY);
        System.out.println(targetRotation);

        while (running) {

            pose.update();

            double x = 0;
            double y = 0;
            double h = 0;

            double successTolerance = 0.2;

            if (Math.abs(targetPositionX - pose.getX()) > successTolerance) {
                x = targetPositionX - pose.getX();
            }
            if (Math.abs(targetPositionY - pose.getY()) > successTolerance) {
                y = targetPositionY - pose.getY();
            }
            if (Math.abs(targetRotation - pose.getRotation()) > successTolerance) {
                h = targetRotation - pose.getRotation();
            }




            Vector2D vector2D = new Vector2D(x, y);

            vector2D = rotateVector(vector2D, -Math.toRadians(pose.getRotation()));

            x = -vector2D.getY();
            y = vector2D.getX();




            if (x == 0 && y == 0 && h == 0) {
                System.out.println("Done");
                running = false;
            }


            opMode.telemetry.addData("Delta X", x);
            opMode.telemetry.addData("Delta Y", y);
            opMode.telemetry.addData("Delta H", h);

            opMode.telemetry.update();

            driveController.moveXY(x, y, -h);


        }

        driveController.moveXY(0,0,0);


    }


}
