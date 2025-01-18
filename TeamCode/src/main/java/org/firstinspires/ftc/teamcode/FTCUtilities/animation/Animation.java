package org.firstinspires.ftc.teamcode.FTCUtilities.animation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.teamcode.FTCUtilities.EventSchedule;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Animation implements Runnable {

    private ArrayList<Keyframe> keyframes;

    private Thread animationThread;

    public String animationName;

    public String animationString = "";

    private LinearOpMode opMode;

    ElapsedTime elapsedTime = new ElapsedTime();

    private boolean isPlaying = false;

    private EventSchedule completeEvent = new EventSchedule();

    public static boolean isPlaying(Animation... anims) {
        for (Animation a : anims) {
            if (a.isPlaying) {
                return true;
            }
        }
        return false;

    }


    public Animation(Keyframe[] keyframes) {
        this.keyframes = new ArrayList<>();
        for (Keyframe k : keyframes) {
            addKeyframe(k);
        }
    }

    public Animation(LinearOpMode opMode) {
        this.keyframes = new ArrayList<>();
        this.opMode = opMode;
    }

    public void fromFile(String filename) {
        try {
            loadAnimationFromFile(filename);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void fromAsset(String assetName) {
        try {
            InputStream inputStream = AppUtil.getDefContext().getAssets().open(assetName);
            String string = new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining("\n"));
            loadAnimationFromString(string);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public Animation(LinearOpMode opMode, String filePath) {
        this.keyframes = new ArrayList<>();
        this.opMode = opMode;
        animationName = filePath.substring(1);
        try {
            URL path = getClass().getResource(filePath);
            loadAnimationFromFile(path.getFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void addKeyframe(Keyframe keyframe) {
        keyframes.add(keyframe);
    }

    public void loadAnimationFromString(String animation) {
        this.animationString = animation;
        String[] keyframesStrings = animation.split("keyframe:\n");
        keyframesStrings = Arrays.copyOfRange(keyframesStrings, 1, keyframesStrings.length);


        for (int i = 0; i < keyframesStrings.length; i++) {
            keyframesStrings[i] = keyframesStrings[i].trim();
        }

        for (String key : keyframesStrings) {

            Keyframe keyframe = new Keyframe();

            String[] actionStrings = key.split("\n");
            for (int i = 0; i < actionStrings.length; i++) {
                actionStrings[i] = actionStrings[i].trim();
            }

            for (String act : actionStrings) {

                Action action = null;
                String[] actionStructure = act.split(":", 2);


                if (actionStructure[0].equals("program")) {

                    actionStructure[1] = actionStructure[1].replace(" .", "0.");

                    String[] actionArgs = actionStructure[1].split(",");

                    ObjectMapper objectMapper = new ObjectMapper();
                    Object[] argArray = null;
                    try {
                        argArray = objectMapper.readValue("[" + actionStructure[1] + "]", Object[].class);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }


                    String methodName = (String) argArray[0];
                    argArray = Arrays.copyOfRange(argArray, 1, argArray.length);

                    action = new ProgramAction(opMode, methodName, argArray);
                }
                if (actionStructure[0].equals("move")) {

                    String[] actionArgs = actionStructure[1].split(",");

                    action = new MoveAction(opMode, Double.parseDouble(actionArgs[1]), Double.parseDouble(actionArgs[2]), Double.parseDouble(actionArgs[3]));
                }
                if (actionStructure[0].equals("wait")) {
                    action = new WaitAction(Double.parseDouble(actionStructure[1]));
                }


                keyframe.addAction(action);

            }

            addKeyframe(keyframe);
        }

    }

    public void loadAnimationFromFile(String filename) throws IOException {
        StringBuilder mappingString = new StringBuilder();
        try (BufferedReader file = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = file.readLine()) != null) {
                mappingString.append(line + "\n");
            }
        }

        String animationString = mappingString.toString();

        loadAnimationFromString(animationString);

    }

    public boolean isPlaying() {
        return isPlaying;
    }



    public void play() {
        animationThread = new Thread(this);
        animationThread.start();
        elapsedTime.reset();
        elapsedTime.startTime();
        isPlaying = true;
    }

    public void stop() {
        for (Keyframe keyframe : keyframes) {
            keyframe.cancel();
        }
        isPlaying = false;

    }

    public void waitForCompleted() {
        try {
            animationThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void waitForCompleted(double timeout) {
        while (animationThread.isAlive() && elapsedTime.seconds() < timeout) {

        }
    }

    public void onComplete(EventSchedule eventSchedule) {
        completeEvent = eventSchedule;

    }

    @Override
    public void run() {
        for (Keyframe keyframe : keyframes) {
            if (isPlaying) {
                keyframe.run();
            }
            else {
                return;
            }
        }
        isPlaying = false;
    }

}
