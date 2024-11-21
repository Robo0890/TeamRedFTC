package org.firstinspires.ftc.teamcode;

import com.arcrobotics.ftclib.hardware.motors.Motor;
import com.arcrobotics.ftclib.hardware.motors.MotorGroup;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import java.util.List;


@TeleOp(name="Strobel Slide Testing", group="Linear OpMode")
@Disabled
public class DriveControllerSlide extends LinearOpMode {

    private DcMotor leftFrontDrive;
    private DcMotor leftBackDrive;
    private DcMotor rightFrontDrive;
    private DcMotor rightBackDrive;
    private DcMotorEx rightSlide;
    private DcMotorEx leftSlide;

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

    private int slidePosition = 0;
    private int rightSlideOffset;
    private int leftSlideOffset;


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
        intakePivot = hardwareMap.get(Servo.class, "intakePivot");
        intakeServo = hardwareMap.get(CRServo.class, "intakeServo");
        leftFrontDrive.setDirection(DcMotor.Direction.REVERSE);
        leftBackDrive.setDirection(DcMotor.Direction.REVERSE);
        rightFrontDrive.setDirection(DcMotor.Direction.FORWARD);
        rightBackDrive.setDirection(DcMotor.Direction.FORWARD);

        rightSlide = hardwareMap.get(DcMotorEx.class, "rightSlide");
        leftSlide = hardwareMap.get(DcMotorEx.class, "leftSlide");

        rightSlide.setTargetPosition(0);
        leftSlide.setTargetPosition(0);

        rightSlide.setMode(DcMotorEx.RunMode.RUN_TO_POSITION);
        leftSlide.setMode(DcMotorEx.RunMode.RUN_TO_POSITION);

        leftSlideOffset = leftSlide.getCurrentPosition();
        rightSlideOffset = rightSlide.getCurrentPosition();





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

        if (gamepad1.left_bumper || gamepad1.right_bumper) {
            if (gamepad1.left_bumper) {
                slidePosition -= 50;
            }
            if (gamepad1.right_bumper) {
                slidePosition += 50;
            }
            slidePosition =  Math.min(Math.max(slidePosition,10),12200);
            setSlidePosition(
                    slidePosition
            );
        }
        rightSlide.setVelocity(5000);
        leftSlide.setVelocity(5000);


        printToTablet("Position", String.valueOf(slidePosition));


        printToTablet("RightSlidePos", String.valueOf(rightSlide.getCurrentPosition()));
        printToTablet("LeftSlidePos", String.valueOf(leftSlide.getCurrentPosition()));


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

    private void setSlidePosition(int position) {
        position *= -1;
        rightSlide.setTargetPosition(position - rightSlideOffset);
        leftSlide.setTargetPosition(-(position - leftSlideOffset));
    }


    public void printToTablet(String label, String msg) {
        telemetry.addData(label, msg);
    }
    public void printToTablet(String msg) {
        telemetry.addData("Output", msg);
    }



}
