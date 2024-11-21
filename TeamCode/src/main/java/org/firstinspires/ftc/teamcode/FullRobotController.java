package org.firstinspires.ftc.teamcode;

import com.arcrobotics.ftclib.hardware.motors.Motor;
import com.arcrobotics.ftclib.hardware.motors.MotorGroup;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.configuration.ConfigurationType;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType;
import com.qualcomm.robotcore.util.ElapsedTime;

import java.util.List;
import java.util.Timer;


@TeleOp(name="Full Robot Controller", group="Linear OpMode")
@Disabled
public class FullRobotController extends LinearOpMode {

    private DcMotor leftFrontDrive;
    private DcMotor leftBackDrive;
    private DcMotor rightFrontDrive;
    private DcMotor rightBackDrive;

    private DcMotorEx rightSlide;
    private DcMotorEx leftSlide;

    private int SLIDE_MIN;
    private int SLIDE_MAX;

    private DcMotor armMotor;

    private Servo intakePivot;
    private CRServo intakeServo;


    private ElapsedTime runtime = new ElapsedTime();

    private int slideSpeed = 40;

    private double armSpeed = 3;

    private boolean pivotControl = false;
    private boolean pivotUp = false;
    private boolean intakeControl = false;
    private boolean intakeOpen = true;
    private Servo rightWrist;
    private Servo leftWrist;




    private double wristPosition = 0.5;
    private int rightSlideOffset;
    private int slidePosition;



    //Controller effects



    @Override
    public void runOpMode() throws InterruptedException {

        //Mapping
        leftFrontDrive = hardwareMap.get(DcMotor.class, "leftFrontDrive");
        leftBackDrive = hardwareMap.get(DcMotor.class, "leftBackDrive");
        rightFrontDrive = hardwareMap.get(DcMotor.class, "rightFrontDrive");
        rightBackDrive = hardwareMap.get(DcMotor.class, "rightBackDrive");

        leftFrontDrive.setDirection(DcMotor.Direction.REVERSE);
        leftBackDrive.setDirection(DcMotor.Direction.REVERSE);
        rightFrontDrive.setDirection(DcMotor.Direction.FORWARD);
        rightBackDrive.setDirection(DcMotor.Direction.FORWARD);

        rightSlide = hardwareMap.get(DcMotorEx.class, "rightSlide");
        leftSlide = hardwareMap.get(DcMotorEx.class, "leftSlide");

        armMotor = hardwareMap.get(DcMotor.class, "armMotor");

        intakePivot = hardwareMap.get(Servo.class, "intakePivot");
        intakeServo = hardwareMap.get(CRServo.class, "intakeServo");

        rightSlide = hardwareMap.get(DcMotorEx.class, "rightSlide");
        leftSlide = hardwareMap.get(DcMotorEx.class, "leftSlide");

        rightSlide.setTargetPosition(0);
        leftSlide.setTargetPosition(0);

        rightSlide.setMode(DcMotorEx.RunMode.RUN_TO_POSITION);
        leftSlide.setMode(DcMotorEx.RunMode.RUN_TO_POSITION);

        rightSlideOffset = rightSlide.getCurrentPosition();

        slidePosition = 0;

        SLIDE_MIN = slidePosition;
        SLIDE_MAX = SLIDE_MIN + 10000;

        rightWrist = hardwareMap.get(Servo.class, "rightWrist");
        leftWrist = hardwareMap.get(Servo.class, "leftWrist");


        armMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);


        printToTablet("Initialized");

        waitForStart();
        runtime.reset();


        gamepad1.setLedColor(125, 218, 88, -1);
        gamepad1.rumbleBlips(2);

        leftWrist.setPosition(wristPosition);
        rightWrist.setPosition(1 - wristPosition);

        intakePivot.setPosition(0);


        while (opModeIsActive()) {


            gameTick();
            telemetry.update();

        }
    }

    public void gameTick(){


        //intake pivot toggle
        if (gamepad1.cross != pivotControl && gamepad1.cross) {
            pivotUp = !pivotUp;
            gamepad1.rumble(.3,.3,50);
            if (pivotUp) {
                //90 degree position
                intakePivot.setPosition(.35);
            }
            else {
                //zero position
                intakePivot.setPosition(0);
            }
            printToTablet(String.valueOf(pivotUp));
        }
        pivotControl = gamepad1.cross;


        //Idk if we are gonna keep this part tbh, just a proof of concept
        //Basically a press starts the motor in one direction until release
        //Next press goes the opposite direction
        //TODO: Think of other ways to do this, D-pad is a decent option
        //BTW: We have access to the trackpad in the middle of the controller ¯\_(ツ)_/¯

        if (gamepad1.dpad_down) {
            intakeServo.setPower(1);
        }
        else if (gamepad1.dpad_up) {
            intakeServo.setPower(-1);
        }
        else {
            intakeServo.setPower(0);
        }




        //Arm control
        if (gamepad1.square) {
            //Pivot calculation
            //Maybe I look at doing something similar to the calc for the big arm movements
            wristPosition -= (gamepad1.right_trigger - gamepad1.left_trigger) / 200;
            wristPosition = Math.min(Math.max(wristPosition, -1),1);

            //fuck servos
            leftWrist.setPosition(wristPosition);
            rightWrist.setPosition(1 - wristPosition);



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
                                    .1 * (Math.pow(gamepad1.right_trigger,10) * 5) - (Math.pow(gamepad1.left_trigger,10) * .1)
                            )
                    )

            );
            armMotor.setPower(armPower);

        }

        //TODO: Linear slide. Some working code exists somewhere in old versions of the "DriveController.java" file. But I didn't like it.
        if (gamepad1.left_bumper || gamepad1.right_bumper) {
            if (gamepad1.left_bumper) {
                slidePosition -= slideSpeed;
                gamepad1.rumble(1,0,100);
            }
            if (gamepad1.right_bumper) {
                slidePosition += slideSpeed;
                gamepad1.rumble(0,1,100);
            }
            slidePosition =  Math.min(Math.max(slidePosition,SLIDE_MIN),SLIDE_MAX);
            setSlidePosition(
                    slidePosition
            );
        }
        rightSlide.setVelocity(5000);
        leftSlide.setVelocity(5000);

        if (gamepad1.dpad_left) {
            slidePosition = SLIDE_MIN;
            setSlidePosition(SLIDE_MIN);
        }
        if (gamepad1.dpad_right) {
            slidePosition = SLIDE_MAX;
            setSlidePosition(SLIDE_MAX);
        }


        printToTablet("Position", String.valueOf(slidePosition));


        printToTablet("RightSlidePos", String.valueOf(rightSlide.getCurrentPosition()));
        printToTablet("LeftSlidePos", String.valueOf(leftSlide.getCurrentPosition()));


        // POV Mode uses left joystick to go forward & strafe, and right joystick to rotate.
        double axial   = -gamepad1.left_stick_y;  // Note: pushing stick forward gives negative value
        double lateral =  gamepad1.left_stick_x;
        double yaw     =  gamepad1.right_stick_x;
        double speed;
        double max;

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
        double leftFrontPower  = ((axial + lateral) + yaw) * speed;
        double rightFrontPower = ((axial - lateral) - yaw) * speed;
        double leftBackPower   = ((axial - lateral) + yaw) * speed;
        double rightBackPower  = ((axial + lateral) - yaw) * speed;


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

    private void setSlidePosition(int position) {
        position *= -1;
        rightSlide.setTargetPosition(position - rightSlideOffset);
        leftSlide.setTargetPosition(-(position));
    }


    //Utility functions
    public void printToTablet(String label, String msg) {
        telemetry.addData(label, msg);
    }
    public void printToTablet(String msg) {
        telemetry.addData("Output", msg);
    }



}
