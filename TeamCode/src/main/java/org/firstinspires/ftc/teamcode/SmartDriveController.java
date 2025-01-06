package org.firstinspires.ftc.teamcode;

import androidx.annotation.NonNull;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.FTCUtilities.LinearSlide;
import org.firstinspires.ftc.teamcode.FTCUtilities.LinearSlideGroup;
import org.firstinspires.ftc.teamcode.FTCUtilities.RobotGamepad;
import org.firstinspires.ftc.teamcode.FTCUtilities.animation.Animation;

import java.util.ArrayList;

@TeleOp(name = "Smart Drive Controller", group = "Linear OpMode")
public class SmartDriveController extends LinearOpMode {

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


    private RobotGamepad manualController;
    private RobotGamepad smartController;

    private Animation reachIntake;
    private Animation retractIntake;

    private Animation smartTransfer;

    private Animation zeroSlides;

    private Animation ready;


    private ArrayList<String[]> tabletData = new ArrayList<>();

    @Override
    public void runOpMode() throws InterruptedException {

        initialize();
        configureControllers();
        buildAnimations();

        waitForStart();
        ready();

        while (opModeIsActive()) {
            gameTick();
        }


    }

    private void initialize() {

        rightBackDrive = hardwareMap.get(DcMotor.class, "rightBackDrive");
        leftBackDrive = hardwareMap.get(DcMotor.class, "leftBackDrive");
        rightFrontDrive = hardwareMap.get(DcMotor.class, "rightFrontDrive");
        leftFrontDrive = hardwareMap.get(DcMotor.class, "leftFrontDrive");

        leftExtendIntake = hardwareMap.get(Servo.class, "leftExtendIntake");
        rightExtendIntake = hardwareMap.get(Servo.class, "rightExtendIntake");
        rightExtendIntake.setDirection(Servo.Direction.REVERSE);

        rightIntake = hardwareMap.get(CRServo.class, "rightIntakeWheel");
        leftIntake = hardwareMap.get(CRServo.class, "leftIntakeWheel");

        rightIntake.setDirection(CRServo.Direction.REVERSE);
        leftIntake.setDirection(CRServo.Direction.REVERSE);

        //intakeColorSensor = hardwareMap.get(ColorSensor.class, "intakeColorSensor");

        intakeWrist = hardwareMap.get(Servo.class, "intakeWrist");

        clawPivot = hardwareMap.get(Servo.class, "slideclawpivot");
        clawGrip = hardwareMap.get(Servo.class, "slideclaw");

        clawArm = new LinearSlideGroup(rightSlide, leftSlide);

        rightSlide = new LinearSlide(hardwareMap.get(DcMotor.class, "rightSlide"));
        leftSlide = new LinearSlide(hardwareMap.get(DcMotor.class, "leftSlide"));

        leftSlide.setDirection(LinearSlide.DIRECTION_REVERSE);

        leftSlideLimit = hardwareMap.get(DigitalChannel.class, "LimitSwitchLeft");
        leftSlideLimit.setMode(DigitalChannel.Mode.INPUT);



        leftFrontDrive.setDirection(DcMotor.Direction.REVERSE);
        leftBackDrive.setDirection(DcMotor.Direction.REVERSE);
        rightFrontDrive.setDirection(DcMotor.Direction.FORWARD);
        rightBackDrive.setDirection(DcMotor.Direction.FORWARD);

        addTabletData("Robot Hardware: Ready");

    }

    private void configureControllers() {
        manualController = new RobotGamepad(gamepad2);
        manualController.fromAsset("manualController.control");

        smartController = new RobotGamepad(gamepad1);
        smartController.fromAsset("smartController.control");

        smartController.rumbleBlip(2);
        manualController.rumble(.5, 500);


        addTabletData("Controllers: Ready");

    }

    private void buildAnimations() {

        reachIntake = new Animation(this);
        reachIntake.fromAsset("reachIntake.robotanimation");

        retractIntake = new Animation(this);
        retractIntake.fromAsset("retractIntake.robotanimation");

        smartTransfer = new Animation(this);
        smartTransfer.fromAsset("transferSample.robotanimation");

        zeroSlides = new Animation(this);
        zeroSlides.fromAsset("zeroSlides.robotanimation");

        ready = new Animation(this);
        ready.fromAsset("ready.robotanimation");


        addTabletData("Animations: Ready");

    }

    private void ready() {
        doubleClickTimer.startTime();

        leftSlide.setActive(true);
        rightSlide.setActive(true);

        //clawArm.setActive(true);

        addTabletData("Getting Ready...");

        ready.play();
        ready.waitForCompleted(5000);

        clearTablet();
        addTabletData("Robot Ready!");

    }

    private void gameTick() {

        RobotGamepad.update(smartController, manualController);

        if (!Animation.isPlaying(retractIntake, reachIntake, smartTransfer)) {
            if (smartController.isActionJustPressed("reachIntake")) {
                if (getIntakeExtension() < .5) {
                    reachIntake.play();
                    smartController.overrideToggle("intakeIn", true);
                    smartController.overrideToggle("intakeWrist", false);
                }
                else {
                    retractIntake.play();
                    smartController.overrideToggle("intakeIn", false);
                    smartController.overrideToggle("intakeWrist", true);
                }
            }


            if (smartController.isActionPressed("intakeOut")) {
                smartController.overrideToggle("intakeIn", false);
                setIntakePower(1);
            }
            else if (smartController.isActionToggled("intakeIn")) {
                setIntakePower(-1);
            }
            else {
                setIntakePower(0);
            }

            if (smartController.isActionPressed("transfer")) {
                smartTransfer.play();
                smartController.overrideToggle("claw", false);
            }

            if (smartController.isActionPressed("left_trigger")) {

                double motion = smartController.getActionAxis("clawPivot") / 10;
                motion *= -1;

                setClawPivot(clawPivot.getPosition() - (motion / 50));
                setTabletData("ClawPivot", clawPivot.getPosition());

            }

            setTabletData("Claw", smartController.isActionToggled("claw"));

            setClawOpen(smartController.isActionToggled("claw"));

            setTabletData("LeftSwitch", leftSlideLimit.getState());

            if (smartController.isActionJustPressed("slideDown")) {
                if (doubleClickTimer.seconds() < .2) {
                    zeroSlides.play();
                }
                doubleClickTimer.reset();
            }

            if (smartController.isActionJustPressed("clawSet")) {
                setClawPivot(.155);
            }

            if (!zeroSlides.isPlaying()) {

                if (smartController.isActionPressed("slideDown") && leftSlideLimit.getState()) {
                    setSlidePower(-.5);
                } else if (smartController.isActionPressed("slideUp")) {
                    setSlidePower(.5);
                } else {
                    setSlidePower(0);
                }
            }
        }

        drive();

    }

    public void addTabletData(Object value) {
        String[] data = new String[2];
        data[0] = "";
        data[1] = value.toString();
        tabletData.add(data);
        updateTablet();
    }
    public void addTabletData(String caption, Object value) {
        String[] data = new String[] {caption, value.toString()};
        tabletData.add(data);
        updateTablet();
    }

    public void setTabletData(String caption, Object value) {
        boolean newData = true;
        String[] data = new String[] {caption, value.toString()};
        for (int i = 0; i < tabletData.size(); i++) {
            if (tabletData.get(i)[0].equals(caption)) {
                tabletData.get(i)[1] = value.toString();
                newData = false;
            }
        }
        if (newData) {
            addTabletData(caption, value);
        }
        else {
            updateTablet();
        }
    }

    public void removeTabletData(String caption) {
        for (int i = 0; i < tabletData.size(); i++) {
            if (tabletData.get(i)[0].equals(caption)) {
                tabletData.remove(i);
                i = i - 1;
            }
        }
        updateTablet();
    }

    public void removeTabletData(int index) {
        tabletData.remove(index);
        updateTablet();
    }

    public void clearTablet() {
        tabletData.clear();
        updateTablet();
    }

    private void updateTablet() {
        for (int i = 0; i < tabletData.size(); i++) {
            if (!tabletData.get(i)[0].equals("")) {
                String[] data = (String[]) tabletData.get(i);
                telemetry.addData(data[0], data[1]);
            }
            else {
                telemetry.addLine(tabletData.get(i)[1]);
            }
        }
        telemetry.update();
    }

    private void drive() {

        double movementSpeed = .5;
        double turnSpeed = .5;

        if (leftExtendIntake.getPosition() > .5) {
            turnSpeed /= 2;
        }

        if (smartController.isActionToggled("crouch")) {
            movementSpeed /= 2;
            turnSpeed /=2;
        }
        if (smartController.isActionPressed("sprint")) {
            movementSpeed *= 2;
        }


        double max;

        // POV Mode uses left joystick to go forward & strafe, and right joystick to rotate.
        double axial   = -smartController.getActionAxis("movement_axial") * movementSpeed;  // Note: pushing stick forward gives negative value
        double lateral =  smartController.getActionAxis("movement_lateral") * movementSpeed;
        double yaw     =  smartController.getActionAxis("movement_yaw") * turnSpeed;

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


    public void zeroSlides() {
        while (leftSlideLimit.getState()) {
            setSlidePower(-1);
            smartController.update();
            if (smartController.isActionPressed("slideUp")) {
                setSlidePower(0);
                return;
            }
        }
        setSlidePower(0);
    }

    public void waitForInput(@NonNull RobotGamepad controller, String actionName) {
        while (!controller.isActionPressed(actionName)) {
            controller.update();
        }
    }

    public void setSlidePower(double power) {
        power *= 1.5;
        leftSlide.getMotor().setPower(power);
        rightSlide.getMotor().setPower(power);
    }

    private double getIntakeExtension() {
        double position = leftExtendIntake.getPosition();
        position = (position - .2) / .5;
        return position;
    }

    public void setIntakeExtension(double position) {
        position = (position * .45) + .1;
        leftExtendIntake.setPosition(position);
        rightExtendIntake.setPosition(position);

    }

    public void setIntakeWrist(double position) {

        position = (position * .745) + .25;
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
        if (!isOpen) {
            clawGrip.setPosition(.55);
        }
        else {
            clawGrip.setPosition(.425);
        }
    }


}
