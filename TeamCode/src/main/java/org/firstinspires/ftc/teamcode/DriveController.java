package org.firstinspires.ftc.teamcode;

import com.arcrobotics.ftclib.hardware.motors.Motor;
import com.arcrobotics.ftclib.hardware.motors.MotorGroup;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import java.util.List;


@TeleOp(name="Strobel DriveController", group="Linear OpMode")

public class DriveController extends LinearOpMode {

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


        while (opModeIsActive()) {




            gameTick();
            telemetry.update();

        }
    }

    public void gameTick(){
        double max;

        // POV Mode uses left joystick to go forward & strafe, and right joystick to rotate.
        double axial   = -gamepad1.left_stick_y;  // Note: pushing stick forward gives negative value
        double lateral =  gamepad1.left_stick_x;
        double yaw     =  gamepad1.right_stick_x;
        double speed;

        armMotor.setPower(armSpeed * (gamepad1.right_trigger * 2) - gamepad1.left_trigger);

        //min is 2, max is -7550
        int direction = 0;

        if (gamepad1.right_bumper) {
            linearSlide.set(1);
        }
        else if (gamepad1.left_bumper) {
            linearSlide.set(-1);
        }
        else {
            linearSlide.set(0);
        }


        List<Double> velocities = linearSlide.getVelocities();
        printToTablet("Right", String.valueOf(velocities.get(0)));
        printToTablet("Left", String.valueOf(velocities.get(0)));

        boolean currentSquarePress = (gamepad1.square);
        if (currentSquarePress && !previousSquarePress) {
            pivotOpen = !pivotOpen;  // Toggle pivot state
            if (pivotOpen) {
                intakePivot.setPosition(1.0);  // Open position
            } else {
                intakePivot.setPosition(-.5);  // Close position
            }
        }
        previousSquarePress = currentSquarePress;

        // Intake toggle logic for X button with time limit
        boolean currentXPress = (gamepad1.cross);
        if (currentXPress && !previousXPress) {
            if (intakeActive) {
                intakeServo.setPower(0.0);  // Stop intake
                intakeActive = false;
            } else {
                intakeServo.setPower(1.0);  // Start intake
                intakeActive = true;
                outtakeActive = false;     // Ensure outtake is off
                intakeTimer.reset();        // Start timer for intake
            }
        }
        previousXPress = currentXPress;

        // Outtake toggle logic for O button with time limit
        boolean currentOPress = (gamepad1.circle);
        if (currentOPress && !previousOPress) {
            if (outtakeActive) {
                intakeServo.setPower(0.0);  // Stop outtake
                outtakeActive = false;
            } else {
                intakeServo.setPower(-1.0); // Start outtake
                outtakeActive = true;
                intakeActive = false;      // Ensure intake is off
                intakeTimer.reset();        // Start timer for outtake
            }
        }
        previousOPress = currentOPress;

        // Stop the intake or outtake after the specified duration
        if (intakeActive && intakeTimer.seconds() > RUN_DURATION) {
            intakeServo.setPower(0.0);
            intakeActive = false;
        }
        if (outtakeActive && intakeTimer.seconds() > RUN_DURATION) {
            intakeServo.setPower(0.0);
            outtakeActive = false;
        }

        // Telemetry feedback
        telemetry.addData("Pivot Open", pivotOpen);
        telemetry.addData("Intake Status", intakeActive ? "Intake" : outtakeActive ? "Outtake" : "Stopped");
        telemetry.update();




        if (gamepad1.right_stick_button) {
            speed = .5;
        }
        else  {
            speed = 1;
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


    public void printToTablet(String label, String msg) {
        telemetry.addData(label, msg);
    }
    public void printToTablet(String msg) {
        telemetry.addData("Output", msg);
    }



}
