package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;


@TeleOp(name="Strobel Comp Contoller", group="Linear OpMode")

public class StrobelConfig extends LinearOpMode {

    private DcMotor leftFrontDrive;
    private DcMotor leftBackDrive;
    private DcMotor rightFrontDrive;
    private DcMotor rightBackDrive;

    private boolean crouchDown = false;
    private boolean isCrouching = false;

    private int armPostion = 0;

    private int SLIDE_MIN;
    private int SLIDE_MAX;

    private DcMotor armMotor;

    private Servo intakePivot;
    private CRServo intakeServo;

    private boolean samplePos = false;
    private boolean samplePosToggle = false;


    private ElapsedTime runtime = new ElapsedTime();

    private int slideSpeed = 40;

    private double armSpeed = .5;

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

        configureRobot();
        gameStart();

        while (opModeIsActive()) {
            gameTick();
            telemetry.update();
        }
    }

    public void gameTick(){

        //Arm control
        if (gamepad1.square) {

            wristPosition += (gamepad1.right_trigger - gamepad1.left_trigger) / 1000;
            wristPosition = Math.min(Math.max(wristPosition, -1),1);

            //fuck servos
            leftWrist.setPosition(wristPosition);
            rightWrist.setPosition(1 - wristPosition);



            printToTablet("left", String.valueOf(leftWrist.getPosition()));
            printToTablet("right", String.valueOf(rightWrist.getPosition()));
            printToTablet("pos", String.valueOf(wristPosition));
        }
        else {
            double armPower = (
                    (
                            //Motor speed should be exponentially related to trigger strength
                            armSpeed * (
                                    (Math.pow(gamepad1.right_trigger,10) * 5) - (Math.pow(gamepad1.left_trigger,10) * 5)
                            )
                    )

            );
            setArmPostion((int) (armPostion + armPower));

            if (armPower != 0) {
                gamepad1.rumble(armPower, -armPower, 2);
            }

        }


        //intake pivot toggle
        if (gamepad1.cross != pivotControl && gamepad1.cross) {
            pivotUp = !pivotUp;
            samplePos = false;
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

        if (gamepad1.triangle && !samplePosToggle) {
            samplePos = !samplePos;
        }
        samplePosToggle = gamepad1.triangle;

        if (samplePos) {
            pivotUp = true;
            intakePivot.setPosition(.35 / 2);
            armMotor.setPower(.1);
        }


        //Intake wheel control
        if (gamepad1.dpad_down) {
            intakeServo.setPower(1);
            gamepad1.rumble(.1,.1,10);
        }
        else if (gamepad1.dpad_up) {
            intakeServo.setPower(-1);
            gamepad1.rumble(.1,.1,10);
        }
        if (gamepad1.dpad_left) {
            intakeServo.setPower(0);
        }


       // printToTablet("Position", String.valueOf(slidePosition));

        // POV Mode uses left joystick to go forward & strafe, and right joystick to rotate.
        double axial   = -gamepad1.left_stick_y;  // Note: pushing stick forward gives negative value
        double lateral =  gamepad1.left_stick_x;
        double yaw     =  gamepad1.right_stick_x;
        double speed;
        double max;

        if (gamepad1.right_stick_button && !crouchDown) {
            isCrouching = !isCrouching;
            crouchDown = true;
            if (isCrouching) {
                gamepad1.rumble(.1,.1,100);
            }
            else {
                gamepad1.rumble(.1,.1,50);
            }
        }
        else if (!gamepad1.right_stick_button)  {
            crouchDown = false;
        }

        //Sprint function
        if (gamepad1.left_stick_button) {
            speed = 1; //For more speed
        }
        //default speed
        else  {
                speed = .5;
        }
        if (isCrouching) {
            speed /= 2;
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

    public void configureRobot() {
        leftFrontDrive = hardwareMap.get(DcMotor.class, "leftFrontDrive");
        leftBackDrive = hardwareMap.get(DcMotor.class, "leftBackDrive");
        rightFrontDrive = hardwareMap.get(DcMotor.class, "rightFrontDrive");
        rightBackDrive = hardwareMap.get(DcMotor.class, "rightBackDrive");

        leftFrontDrive.setDirection(DcMotor.Direction.REVERSE);
        leftBackDrive.setDirection(DcMotor.Direction.REVERSE);
        rightFrontDrive.setDirection(DcMotor.Direction.FORWARD);
        rightBackDrive.setDirection(DcMotor.Direction.FORWARD);

        intakePivot = hardwareMap.get(Servo.class, "intakePivot");
        intakeServo = hardwareMap.get(CRServo.class, "intakeServo");

        rightWrist = hardwareMap.get(Servo.class, "rightWrist");
        leftWrist = hardwareMap.get(Servo.class, "leftWrist");

        armMotor = hardwareMap.get(DcMotor.class, "armMotor");
        armMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        armMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);


    }

    public void gameStart() {

        //Mapping

        armPostion = armMotor.getCurrentPosition();

        printToTablet("Initialized");

        waitForStart();
        runtime.reset();


        gamepad1.setLedColor(125, 218, 88, -1);
        gamepad1.rumbleBlips(2);

        leftWrist.setPosition(wristPosition);
        rightWrist.setPosition(1 - wristPosition);

        intakePivot.setPosition(0);
    }

    private void setArmPostion(int postion) {
        armPostion = armMotor.getCurrentPosition();
        if (armPostion - postion < 10) {

        }
        else if (armPostion < postion) {
            armMotor.setPower(armPostion-postion);
            setArmPostion(postion);
        }
        else {
            armMotor.setPower(postion-armPostion);
            setArmPostion(postion);
        }

    }
    //Utility functions
    public void printToTablet(String label, String msg) {
        telemetry.addData(label, msg);
    }
    public void printToTablet(String msg) {
        telemetry.addData("Output", msg);
    }



}
