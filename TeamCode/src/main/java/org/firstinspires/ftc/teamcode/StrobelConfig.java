package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;


@TeleOp(name="Strobel's Comp Config", group="Linear OpMode")

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

    private double armSpeed = 5;

    private boolean pivotControl = false;
    private boolean pivotUp = false;
    private boolean intakeControl = false;
    private boolean intakeOpen = true;
    private Servo rightWrist;
    private Servo leftWrist;

    boolean groundingInput;

    ElapsedTime groundingTimer = new ElapsedTime();

    boolean scoringInput;

    ElapsedTime scoringTimer = new ElapsedTime();

    boolean intakeSucking = false;
    boolean intakeSpitting = false;




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

            wristPosition -= (gamepad1.right_trigger - gamepad1.left_trigger) / 1000;
            wristPosition = Math.min(Math.max(wristPosition, -1),1);

            //fuck servos
            leftWrist.setPosition(wristPosition);
            rightWrist.setPosition(1 - wristPosition);



            printToTablet("left", String.valueOf(leftWrist.getPosition()));
            printToTablet("right", String.valueOf(rightWrist.getPosition()));
            printToTablet("pos", String.valueOf(wristPosition));

            armMotor.setPower((armPostion - armMotor.getCurrentPosition()) * .1);
        }
        else {
            double armPower = (armSpeed * (
                                                (Math.pow(gamepad1.right_trigger,10) * 5) - (Math.pow(gamepad1.left_trigger,10) * 5)
                                        ));
            armPostion += armPower;

            //armMotor.setPower(armPower);
            armMotor.setPower((armPostion - armMotor.getCurrentPosition()) * .1);


            printToTablet("armTarget", String.valueOf(armPostion));
            printToTablet("armPosition", String.valueOf(armMotor.getCurrentPosition()));
            printToTablet("armInput", String.valueOf(armPower));
            printToTablet("armPower", String.valueOf(armPostion - armMotor.getCurrentPosition()));
            printToTablet("Wrist Position", String.valueOf(wristPosition));

            if (armPower != 0) {
                gamepad1.rumble(-armPower / 2, armPower / 2, 2);
            }

            if (gamepad1.left_trigger > .5) {
                if (!groundingInput) {
                    if (groundingTimer.seconds() < .25) {
                        armPostion = 30;
                        wristPosition = .4;

                        leftWrist.setPosition(wristPosition);
                        rightWrist.setPosition(1 - wristPosition);

                        gamepad1.rumble(.5,.5,1000);
                    }

                }
                groundingTimer.reset();
            }
            groundingInput = gamepad1.left_trigger > .5;

            if (gamepad1.right_trigger > .5) {
                if (!scoringInput) {
                    if (scoringTimer.seconds() < .25) {
                        armPostion = 425;
                        wristPosition = .5;

                        leftWrist.setPosition(wristPosition);
                        rightWrist.setPosition(1 - wristPosition);

                        gamepad1.rumble(.5,.5,1000);
                    }

                }
                scoringTimer.reset();
            }
            scoringInput = gamepad1.right_trigger > .5;

        }




        //intake pivot toggle
        if (gamepad1.cross) {
            if (!pivotControl) {
                pivotUp = !pivotUp;
                samplePos = false;
                gamepad1.rumble(.3, .3, 50);
                if (pivotUp) {
                    //90 degree position
                    intakePivot.setPosition(.35);

                } else {
                    //zero position
                    intakePivot.setPosition(0);
                }
            }
        }
        pivotControl = gamepad1.cross;


        printToTablet(String.valueOf(pivotUp));


        if (gamepad1.triangle && !samplePosToggle) {
            samplePos = !samplePos;
        }
        samplePosToggle = gamepad1.triangle;

        if (samplePos) {
            printToTablet("Sample");
            pivotUp = true;
            intakePivot.setPosition(.35 / 2);
            armMotor.setPower(.1);
        }


        //Intake wheel control
        if (gamepad1.dpad_down) {
            if (!intakeSucking) {
                if (intakeServo.getPower() == 1) {
                    intakeServo.setPower(0);
                } else {
                    intakeServo.setPower(1);
                }
            }
        }
        intakeSucking = gamepad1.dpad_down;

        if (gamepad1.dpad_up) {
            intakeServo.setPower(-1);
            intakeSpitting = true;
        }
        else {
            if (intakeSpitting) {
                intakeServo.setPower(0);
                intakeSpitting = false;
            }
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

        printToTablet("Pivot", String.valueOf(intakePivot.getPosition()));

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
        int currentPosition = armMotor.getCurrentPosition();
        if (currentPosition - postion < 10) {

        }
        else if (currentPosition < postion) {
            armMotor.setPower(currentPosition-postion);
            setArmPostion(postion);
        }
        else {
            armMotor.setPower(postion-currentPosition);
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
