package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.FTCUtilities.LinearSlide;
import org.firstinspires.ftc.teamcode.FTCUtilities.LinearSlideGroup;
import org.firstinspires.ftc.teamcode.FTCUtilities.RobotGamepad;
import org.firstinspires.ftc.teamcode.FTCUtilities.animation.Action;
import org.firstinspires.ftc.teamcode.FTCUtilities.animation.Animation;
import org.firstinspires.ftc.teamcode.FTCUtilities.animation.Keyframe;
import org.firstinspires.ftc.teamcode.FTCUtilities.animation.ProgramAction;
import org.firstinspires.ftc.teamcode.FTCUtilities.animation.WaitAction;

@TeleOp(name = "Manual Drive Controller", group = "Linear OpMode")
public class DriveController extends LinearOpMode {


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

    private RobotGamepad manualController;
    private RobotGamepad smartController;

    private Animation reachIntake;
    private Animation retractIntake;

    private Animation smartTransfer;



    @Override
    public void runOpMode() throws InterruptedException {



        initialize();
        buildAnimations();

        telemetry.addData(reachIntake.animationName, reachIntake.animationString);
        telemetry.update();


        waitForStart();

        //ready();

        while (opModeIsActive()) {
            gameTick();
        }


    }

    private void ready() {
        leftSlide.setActive(true);
        rightSlide.setActive(true);

        clawArm.setActive(true);

    }

    private void buildAnimations() {

        reachIntake = new Animation(this);
        reachIntake.fromAsset("reachIntake.robotanimation");

        retractIntake = new Animation(this);
        retractIntake.fromAsset("retractIntake.robotanimation");

        smartTransfer = new Animation(this);
        smartTransfer.fromAsset("transferSample.robotanimation");

        reachIntake = new Animation(new Keyframe[]{
                new Keyframe(new Action[]{
                        new ProgramAction(this, "setIntakeExtension", 1.0),
                        new WaitAction(.5),
                }),
                new Keyframe(new Action[] {
                        new ProgramAction(this, "setIntakeWrist", 1.0),
                        new ProgramAction(this, "setIntakePower", -1.0),
                        new ProgramAction(this, "waitForInput", manualController, "reachIntake")
                }),
                new Keyframe(new Action[] {
                        new ProgramAction(this, "setIntakeWrist", 0.0),
                        new WaitAction(.5)
                }),
                new Keyframe(new Action[] {
                        new ProgramAction(this, "setIntakePower", 0.0),
                        new ProgramAction(this, "setIntakeExtension", 0.0)
                }),
                new Keyframe(new Action[] {
                        new ProgramAction(this, "setIntakePower", .5),
                        new WaitAction(.1)
                }),
                new Keyframe(new Action[] {
                        new ProgramAction(this, "setIntakePower", 0.0)
                })
        });

    }

    private void initialize() {

        manualController = new RobotGamepad(gamepad2);
        smartController = new RobotGamepad(gamepad1);

        String controllerMapping =
                "movement_axial:left_stick_y," +
                        "movement_lateral:left_stick_x," +
                        "movement_yaw:right_stick_x," +
                        "intakeExtend:y," +
                        "intakeDrop:dpad_up," +
                        "intakeSuck:dpad_down," +
                        "intakeWristDown:b," +
                        "clawPivot:left_trigger," +
                        "slideUp:right_bumper," +
                        "slideDown:left_bumper," +
                        "reachIntake:right_trigger," +
                        "claw:x,";

        String smartMapping =
                "movement_axial:left_stick_y," +
                        "movement_lateral:left_stick_x," +
                        "movement_yaw:right_stick_x," +
                        "reachIntake:right_trigger,";

        manualController.mapFromString(controllerMapping);
        smartController.mapFromString(smartMapping);

        manualController.rumbleBlip(2);

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


        leftFrontDrive.setDirection(DcMotor.Direction.REVERSE);
        leftBackDrive.setDirection(DcMotor.Direction.REVERSE);
        rightFrontDrive.setDirection(DcMotor.Direction.FORWARD);
        rightBackDrive.setDirection(DcMotor.Direction.FORWARD);

    }

    private void gameTick() {

        manualController.update();
        smartController.update();


        if (!reachIntake.isPlaying()) {
            if (manualController.isActionJustPressed("reachIntake")) {
                reachIntake.play();
            }

            if (manualController.isActionToggled("intakeSuck")) {
                setIntakePower(-1);
            }
            else if (manualController.isActionPressed("intakeDrop")) {
                setIntakePower(1);
            }
            else {
                setIntakePower(0);
            }

            if (manualController.isActionToggled("intakeExtend")) {
                setIntakeExtension(1);
            }
            else {
                setIntakeExtension(0);
            }

            if (manualController.isActionToggled("intakeWristDown")) {
                setIntakeWrist(1);
            }
            else {
                setIntakeWrist(0);
            }

            if (manualController.isActionToggled("clawPivot")) {
                setClawPivot(.5);
            }
            else {
                setClawPivot(1);
            }
                setClawOpen(manualController.isActionToggled("claw"));


        }

        if (manualController.isActionPressed("slideUp")) {
            clawArm.setPosition(clawArm.getCurrentPosition() + 1);
        }
        if (manualController.isActionPressed("slideDown")) {
            clawArm.setPosition(clawArm.getCurrentPosition() - 1);
        }




        drive();


    }




    private void drive() {

        double movementSpeed = .5;
        double turnSpeed = .5;

        if (leftExtendIntake.getPosition() > .5) {
            turnSpeed /= 2;
        }


        double max;

        // POV Mode uses left joystick to go forward & strafe, and right joystick to rotate.
        double axial   = -manualController.getActionAxis("movement_axial") * movementSpeed;  // Note: pushing stick forward gives negative value
        double lateral =  manualController.getActionAxis("movement_lateral") * movementSpeed;
        double yaw     =  manualController.getActionAxis("movement_yaw") * turnSpeed;

        // Combine the joystick requests for each axis-motion to determine each wheel's power.
        // Set up a variable for each drive wheel to save the power level for telemetry.
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

        leftFrontDrive.setPower(leftFrontPower);
        rightFrontDrive.setPower(rightFrontPower);
        leftBackDrive.setPower(leftBackPower);
        rightBackDrive.setPower(rightBackPower);

    }

    public void waitForInput(RobotGamepad controller, String actionName) {
        while (!controller.isActionPressed(actionName)) {
            controller.update();
        }
    }

    public void setIntakeExtension(double position) {

        position = (position * .5) + .1;
        leftExtendIntake.setPosition(position);
        rightExtendIntake.setPosition(position);


    }

    public void setIntakeWrist(double position) {

        position = (position * .725) + .2;
        intakeWrist.setPosition(position);
    }

    public void setIntakePower(double power) {
        rightIntake.setPower(power);
        leftIntake.setPower(-power);
    }

    public void setClawPivot(double position) {
        clawPivot.setPosition(position);
    }

    public void setClawOpen(boolean isOpen) {
        if (isOpen) {
            clawGrip.setPosition(1);
        }
        else {
            clawGrip.setPosition(.9);
        }
    }



}
