package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.FTCUtilities.DriveController;
import org.firstinspires.ftc.teamcode.FTCUtilities.LinearSlide;
import org.firstinspires.ftc.teamcode.FTCUtilities.LinearSlideGroup;
import org.firstinspires.ftc.teamcode.FTCUtilities.RobotGamepad;
import org.firstinspires.ftc.teamcode.FTCUtilities.animation.Action;
import org.firstinspires.ftc.teamcode.FTCUtilities.animation.Animation;
import org.firstinspires.ftc.teamcode.FTCUtilities.animation.Keyframe;
import org.firstinspires.ftc.teamcode.FTCUtilities.animation.MoveAction;
import org.firstinspires.ftc.teamcode.FTCUtilities.animation.RobotPose;

@Autonomous(name = "AutoTest", group = "Strobel")
public class AutoTest extends LinearOpMode {


    private ElapsedTime doubleClickTimer = new ElapsedTime();

    private DcMotor leftFrontDrive;
    private DcMotor rightFrontDrive;
    private DcMotor leftBackDrive;
    private DcMotor rightBackDrive;

    private LinearSlide leftSlide;
    private LinearSlide rightSlide;

    private LinearSlideGroup clawArm;

    private Servo leftExtendIntake;
    private Servo rightExtendIntake;

    private CRServo rightIntake;
    private CRServo leftIntake;

    private Servo intakeWrist;

    private Servo clawPivot;
    private Servo clawGrip;


    private DigitalChannel leftSlideLimit;

    //private ColorSensor intakeColorSensor;


    private RobotGamepad controller;

    public RobotPose pose;

    public DriveController driveController;

    private Animation auto;




    public void runOpMode() throws InterruptedException {



        initialize();
        buildAnimations();

        telemetry.update();


        waitForStart();

        ready();



        while (opModeIsActive()) {
            pose.update();
            controller.update();


            telemetry.addData("X", pose.getX());
            telemetry.addData("Y", pose.getY());
            telemetry.addData("Rotation", pose.getRotation());

            telemetry.addData("right", rightFrontDrive.getCurrentPosition());
            telemetry.addData("left", leftFrontDrive.getCurrentPosition());
            telemetry.addData("x", leftBackDrive.getCurrentPosition());

            telemetry.update();

            driveController.moveXY(gamepad1.left_stick_x / 2, gamepad1.left_stick_y / 2, gamepad1.right_stick_x / 2);
        }




    }

    private void ready() {

        auto.play();


    }

    private void buildAnimations() {

        auto = new Animation(new Keyframe[] {
                new Keyframe(
                        new Action[]{
                                new MoveAction(this, 10, 0, 0)
                        }
                )
        });

    }

    private void initialize() {

        controller = new RobotGamepad(gamepad1);

        driveController = new DriveController(this,
                "rightFrontDrive",
                "leftFrontDrive",
                "rightBackDrive",
                "leftBackDrive"
        );

        pose = new RobotPose(hardwareMap, "rightFrontDrive", "leftFrontDrive", "leftBackDrive");

        rightBackDrive = hardwareMap.get(DcMotor.class, "rightBackDrive");
        leftBackDrive = hardwareMap.get(DcMotor.class, "leftBackDrive");
        rightFrontDrive = hardwareMap.get(DcMotor.class, "rightFrontDrive");
        leftFrontDrive = hardwareMap.get(DcMotor.class, "leftFrontDrive");



        leftExtendIntake = hardwareMap.get(Servo.class, "leftExtendIntake");
        rightExtendIntake = hardwareMap.get(Servo.class, "rightExtendIntake");

        rightExtendIntake.setDirection(Servo.Direction.REVERSE);

        rightIntake = hardwareMap.get(CRServo.class, "rightIntakeWheel");
        leftIntake = hardwareMap.get(CRServo.class, "leftIntakeWheel");

        intakeWrist = hardwareMap.get(Servo.class, "intakeWrist");

        clawPivot = hardwareMap.get(Servo.class, "slideclawpivot");

        clawGrip = hardwareMap.get(Servo.class, "slideclaw");

        rightSlide = new LinearSlide(hardwareMap.get(DcMotor.class, "rightSlide"));
        leftSlide = new LinearSlide(hardwareMap.get(DcMotor.class, "leftSlide"));

        leftSlide.setDirection(LinearSlide.DIRECTION_REVERSE);

        clawArm = new LinearSlideGroup(rightSlide, leftSlide);


        leftFrontDrive.setDirection(DcMotor.Direction.FORWARD);
        leftBackDrive.setDirection(DcMotor.Direction.FORWARD);
        rightFrontDrive.setDirection(DcMotor.Direction.REVERSE);
        rightBackDrive.setDirection(DcMotor.Direction.REVERSE);







    }
}
