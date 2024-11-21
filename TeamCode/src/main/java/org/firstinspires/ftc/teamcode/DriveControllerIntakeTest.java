package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;


@TeleOp(name="Strobel Intake Testing", group="Linear OpMode")
@Disabled
public class DriveControllerIntakeTest extends LinearOpMode {

    private DcMotor leftFrontDrive;
    private DcMotor leftBackDrive;
    private DcMotor rightFrontDrive;
    private DcMotor rightBackDrive;
    private DcMotorEx rightSlide;
    private DcMotorEx leftSlide;

    private DcMotor armMotor;



    private ElapsedTime intakeTimer = new ElapsedTime();

    private static final double RUN_DURATION = 3.0; // Duration in seconds

    private ElapsedTime runtime = new ElapsedTime();

    private double armSpeed = 1;
    private int slideSpeed = 2;

    private int armPosition = 2;


    private Servo intakePivot;
    private CRServo intakeServo;

    private boolean pivotControl = false;
    private boolean pivotUp = false;

    private boolean intakeControl = false;
    private boolean intakeOpen = true;

    private Servo rightWrist;
    private double rightWristPosition;
    private Servo leftWrist;
    private double leftWristPosition;


    private double wristPosition = 0.5;


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
        leftFrontDrive.setDirection(DcMotor.Direction.REVERSE);
        leftBackDrive.setDirection(DcMotor.Direction.REVERSE);
        rightFrontDrive.setDirection(DcMotor.Direction.FORWARD);
        rightBackDrive.setDirection(DcMotor.Direction.FORWARD);

        rightSlide = hardwareMap.get(DcMotorEx.class, "rightSlide");
        leftSlide = hardwareMap.get(DcMotorEx.class, "leftSlide");

        rightSlide.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
        leftSlide.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);

        intakePivot = hardwareMap.get(Servo.class, "intakePivot");
        intakeServo = hardwareMap.get(CRServo.class, "intakeServo");

        rightWrist = hardwareMap.get(Servo.class, "rightWrist");
        leftWrist = hardwareMap.get(Servo.class, "leftWrist");










        printToTablet("Initialized");

        waitForStart();
        runtime.reset();

        intakePivot.setPosition(-0.5);

        rightWristPosition = rightWrist.getPosition();
        leftWristPosition = leftWrist.getPosition();


        while (opModeIsActive()) {


            gameTick();
            telemetry.update();

        }
    }

    public void gameTick(){

        if (gamepad1.square != pivotControl && gamepad1.square) {
            pivotUp = !pivotUp;
            if (pivotUp) {
                intakePivot.setPosition(.65);
            }
            else {
                intakePivot.setPosition(-0.5);
            }
        }
        pivotControl = gamepad1.square;

        if (gamepad1.cross != intakeControl && gamepad1.cross) {
            intakeOpen = !intakeOpen;
            if (intakeOpen) {
                while (gamepad1.cross) {
                    intakeServo.setPower(1);
                }
                intakeServo.setPower(0);
            }
            else {
                while (gamepad1.cross) {
                    intakeServo.setPower(-1);
                }
                intakeServo.setPower(0);
            }
        }
        intakeControl = gamepad1.cross;



        if (gamepad1.circle) {
            wristPosition -= (gamepad1.right_trigger - gamepad1.left_trigger) / 1000;
            wristPosition = Math.min(Math.max(wristPosition, -1),1);
            leftWrist.setPosition(wristPosition);
            rightWrist.setPosition(1-wristPosition);
            printToTablet("left", String.valueOf(leftWrist.getPosition()));
            printToTablet("right", String.valueOf(rightWrist.getPosition()));
            printToTablet("pos", String.valueOf(wristPosition));
        }
        else {
            armMotor.setPower(
                    0 + (
                            armSpeed * (
                                    .1 * (Math.pow(gamepad1.right_trigger,3) * 5) - (Math.pow(gamepad1.left_trigger,3) * .1)
                            )
                    )

            );
        }



    }



    public void printToTablet(String label, String msg) {
        telemetry.addData(label, msg);
    }
    public void printToTablet(String msg) {
        telemetry.addData("Output", msg);
    }





}
