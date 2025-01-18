package org.firstinspires.ftc.teamcode.FTCUtilities.animation;

import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.FTCUtilities.EncBot;

public class RobotPose {

    private double x;
    private double y;
    private double rotation;

    private EncBot bot;

    private HardwareMap hardwareMap;

    private boolean active = false;


    /**
     * There must be three encoders defined as "enc_right", "enc_left", and "enc_x" on the bot's hardware map.
     * @param hardwareMap
     */
    public RobotPose(HardwareMap hardwareMap, String rightEncoderName, String leftEncoderName, String xEncoderName) {
        this.hardwareMap = hardwareMap;
        bot = new EncBot();
        bot.init(hardwareMap, rightEncoderName, leftEncoderName, xEncoderName);
    }

    public void update() {

        active = true;

        bot.update();

        x = bot.getX();
        y = bot.getY();
        rotation = Math.toDegrees(bot.getRotation());
    }

    private void checkActive() {
        if (!active) {
            throw new RuntimeException("Pose position called without update: Did you call Pose.update()?");
        }
    }

    public double getX() {
        checkActive();
        return x;
    }
    public double getY() {
        checkActive();
        return y;
    }
    public double getRotation() {
        checkActive();
        return rotation;
    }


}
