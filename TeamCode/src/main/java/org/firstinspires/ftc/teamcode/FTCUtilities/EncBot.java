package org.firstinspires.ftc.teamcode.FTCUtilities;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.tejasmehta.OdometryCore.OdometryCore;
import com.tejasmehta.OdometryCore.localization.EncoderPositions;
import com.tejasmehta.OdometryCore.localization.OdometryPosition;


/**
 * Utility class that represents a robot with mecanum drive wheels and three "dead-wheel" encoders.
 */
public class EncBot {

    public final double WHEEL_DIAMETER = 1.91;
    private final double ENCODER_TICKS_PER_REVOLUTION = 8192;
    private final double LEFT_OFFSET = 7/2;
    private final double RIGHT_OFFSET = 7/2;
    private final double BACK_OFFSET = 4;

    private DcMotor leftEncoder;
    private DcMotor rightEncoder;
    private DcMotor backEncoder;

    private OdometryPosition position;

    public void init(HardwareMap hwMap, String rightEncoderName, String leftEncoderName, String xEncoderName) {
        OdometryCore.initialize(ENCODER_TICKS_PER_REVOLUTION, WHEEL_DIAMETER, LEFT_OFFSET, RIGHT_OFFSET, BACK_OFFSET);
        rightEncoder = hwMap.get(DcMotor.class, rightEncoderName);
        leftEncoder = hwMap.get(DcMotor.class, leftEncoderName);
        backEncoder = hwMap.get(DcMotor.class, xEncoderName);

        update();

    }

    public void update() {
        double leftPosition = leftEncoder.getCurrentPosition();
        double rightPosition = rightEncoder.getCurrentPosition();
        double frontBackPosition = backEncoder.getCurrentPosition();
        EncoderPositions encoderPositions = new EncoderPositions(leftPosition, rightPosition, frontBackPosition);

        position = OdometryCore.getInstance().getCurrentPosition(encoderPositions);
    }

    public double getX() {
        return position.getX();
    }
    public double getY() {
        return position.getY();
    }
    public double getRotation() {
        return Math.toDegrees(position.getHeadingRadians());
    }
}
