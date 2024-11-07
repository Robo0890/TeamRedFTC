package org.firstinspires.ftc.teamcode;

import com.arcrobotics.ftclib.hardware.motors.MotorGroup;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import java.util.List;


@TeleOp(name="Strobel Robot Controller", group="Linear OpMode")

public class FullRobotController extends LinearOpMode {

    private DcMotor leftFrontDrive;
    private DcMotor leftBackDrive;
    private DcMotor rightFrontDrive;
    private DcMotor rightBackDrive;
    private DcMotor rightSlide;
    private DcMotor leftSlide;
    private MotorGroup linearSlide;

    private DcMotor armMotor;

    private Servo intakePivot;
    private CRServo intakeServo;

    private boolean pivotOpen= true;
    private boolean previousSquarePress = false;
    private boolean intakeActive = false;
    private boolean outtakeActive = false;
    private boolean previousXPress = false;
    private boolean previousOPress = false;
    private ElapsedTime intakeTimer = new ElapsedTime();

    private static final double RUN_DURATION = 3.0; // Duration in seconds

    private ElapsedTime runtime = new ElapsedTime();

    private double armSpeed = 1;
    private int slideSpeed = 2;

    private int armPosition = 2;

    private boolean pivotControl = false;
    private boolean pivotUp = false;

    private boolean intakeControl = false;
    private boolean intakeOpen = true;

    private Servo rightWrist;
    private double rightWristPosition;
    private Servo leftWrist;
    private double leftWristPosition;


    private double wristPosition = 0.5;

    //Controller effects



//    @Override
//    public void init() {
//        telemetry.addData("Initialization:", "success");
//        telemetry.update(pres);
//    }

    @Override
    public void runOpMode() throws InterruptedException {

        //Mapping
        leftFrontDrive = hardwareMap.get(DcMotor.class, "leftFrontDrive");
        leftBackDrive = hardwareMap.get(DcMotor.class, "leftBackDrive");
        rightFrontDrive = hardwareMap.get(DcMotor.class, "rightFrontDrive");
        rightBackDrive = hardwareMap.get(DcMotor.class, "rightBackDrive");

        armMotor = hardwareMap.get(DcMotor.class, "armMotor");
        rightSlide = hardwareMap.get(DcMotor.class, "rightSlide");
        leftSlide = hardwareMap.get(DcMotor.class, "leftSlide");
        intakePivot = hardwareMap.get(Servo.class, "intakePivot");
        intakeServo = hardwareMap.get(CRServo.class, "intakeServo");
        leftFrontDrive.setDirection(DcMotor.Direction.REVERSE);
        leftBackDrive.setDirection(DcMotor.Direction.REVERSE);
        rightFrontDrive.setDirection(DcMotor.Direction.FORWARD);
        rightBackDrive.setDirection(DcMotor.Direction.FORWARD);

        rightSlide.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        leftSlide.setMode(DcMotor.RunMode.RUN_USING_ENCODER);



        double kP = 20;
        double kV = 0.7;
        linearSlide.setVeloCoefficients(kP, 0, 0);
        linearSlide.setFeedforwardCoefficients(0, kV);



        printToTablet("Initialized");

        waitForStart();
        runtime.reset();


        gamepad1.setLedColor(125, 218, 88, 3000);
        gamepad1.rumbleBlips(2);


        while (opModeIsActive()) {


            gameTick();
            telemetry.update();

        }
    }

    public void gameTick(){

        // POV Mode uses left joystick to go forward & strafe, and right joystick to rotate.
        double axial   = -gamepad1.left_stick_y;  // Note: pushing stick forward gives negative value
        double lateral =  gamepad1.left_stick_x;
        double yaw     =  gamepad1.right_stick_x;
        double speed;
        double max;


        //intake pivot toggle
        if (gamepad1.square != pivotControl && gamepad1.square) {
            pivotUp = !pivotUp;
            if (pivotUp) {
                //90 degree position
                intakePivot.setPosition(.65);
            }
            else {
                //zero position
                intakePivot.setPosition(-0.5);
            }
        }
        pivotControl = gamepad1.square;


        //Idk if we are gonna keep this part tbh, just a proof of concept
        //Basically a press starts the motor in one direction until release
        //Next press goes the opposite direction
        //TODO: Think of other ways to do this, D-pad is a decent option
        //BTW: We have access to the trackpad in the middle of the controller ¯\_(ツ)_/¯
        if (gamepad1.cross != intakeControl && gamepad1.cross) {
            intakeOpen = !intakeOpen;
            if (intakeOpen) {
                while (gamepad1.cross) {
                    intakeServo.setPower(1);
                    //TODO: Haptic controller feedback
                    gamepad1.rumble(.05,.05,10);
                }
                intakeServo.setPower(0);
                gamepad1.setLedColor(255, 255, 255, -1);
            }
            else {
                while (gamepad1.cross) {
                    intakeServo.setPower(-1);
                    gamepad1.rumble(.05,.05,10);
                }
                intakeServo.setPower(0);
                gamepad1.setLedColor(0, 0, 0, -1);
            }
        }
        intakeControl = gamepad1.cross;




        //Arm control
        if (gamepad1.circle) {
            //Pivot calculation
            //Maybe I look at doing something similar to the calc for the big arm movements
            wristPosition -= (gamepad1.right_trigger - gamepad1.left_trigger) / 1000;
            wristPosition = Math.min(Math.max(wristPosition, -1),1);

            //fuck servos
            leftWrist.setPosition(wristPosition);
            rightWrist.setPosition(1 - wristPosition);

            if (gamepad1.left_trigger > .5) {
                gamepad1.rumble(.1,0,10);
            }
            if (gamepad1.right_trigger < .5) {
                gamepad1.rumble(0,.1,10);
            }


            printToTablet("left", String.valueOf(leftWrist.getPosition()));
            printToTablet("right", String.valueOf(rightWrist.getPosition()));
            printToTablet("pos", String.valueOf(wristPosition));
        }
        else {

            //Calculate big arm movement
            //TODO: Fix erectile dysfunction
            double armPower = (
                    (
                            //Motor speed should be exponentially related to trigger strength
                            armSpeed * (
                                    .1 * (Math.pow(gamepad1.right_trigger,3) * 5) - (Math.pow(gamepad1.left_trigger,3) * .1)
                            )
                    )

            );
            armMotor.setPower(armPower);
            if (gamepad1.left_trigger > .5) {
                gamepad1.rumble(.5,0,10);
            }
            if (gamepad1.right_trigger < .5) {
                gamepad1.rumble(0,.5,10);
            }

        }

        //TODO: Linear slide. Some working code exists somewhere in old versions of the "DriveController.java" file. But I didn't like it.

        //Sprint function
        if (gamepad1.left_stick_button) {
            speed = 1; //For more speed
        }
        //Sneak function
        else if (gamepad1.right_stick_button) {
            speed = .25; //For more control
        }
        //default speed
        else  {
            speed = .5;
        }

        // Combine the joystick requests for each axis-motion to determine each wheel's power.
        // Set up a variable for each drive wheel to save the power level for telemetry.
        double leftFrontPower  = (axial + lateral) * speed + yaw;
        double rightFrontPower = (axial - lateral) * speed - yaw;
        double leftBackPower   = (axial - lateral) * speed + yaw;
        double rightBackPower  = (axial + lateral) * speed - yaw;


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

        // Send calculated power to wheels
        leftFrontDrive.setPower(leftFrontPower);
        rightFrontDrive.setPower(rightFrontPower);
        leftBackDrive.setPower(leftBackPower);
        rightBackDrive.setPower(rightBackPower);

    }


    //Utility functions
    public void printToTablet(String label, String msg) {
        telemetry.addData(label, msg);
    }
    public void printToTablet(String msg) {
        telemetry.addData("Output", msg);
    }



}
