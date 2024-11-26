package org.firstinspires.ftc.teamcode.FTCUtilities;

import com.qualcomm.robotcore.hardware.Gamepad;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.Scanner; // Import the Scanner class to read text files

public class RobotGamepad {


    private ArrayList<String> pressedInputs;

    private ArrayList<String> toggledInputs;

    private Gamepad gamepad;

    final private String[] axialInputs = new String[] {
            "right_trigger",
            "left_trigger",
            "right_stick_x",
            "right_stick_y",
            "left_stick_x",
            "left_stick_y",
    };

    private HashMap<String, String> controllerMap = new HashMap<String, String>();
    private HashMap<String, Double> activationThresholds = new HashMap<String, Double>();


    public RobotGamepad(Gamepad gamepad) {
        loadMappingFromFile("DefaultControllerConfig");
    }

    public RobotGamepad(Gamepad gamepad, String mappingString) {
        mapString(mappingString);
    }

    public void mapInput(String actionName, String controllerButton) {
        controllerMap.put(actionName, controllerButton);
    }

    public void loadMappingFromFile(String filename) {
        String mappingString = "";
        File file = new File(filename);
        Scanner fileReader = null;
        try {
            fileReader = new Scanner(file);
            while (fileReader.hasNextLine()) {
                mappingString += fileReader.nextLine();
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        mapString(mappingString);

    }

    private void mapString(String mappingString) {
        String[] lines = mappingString.split(",");
        for (String l: lines) {
            String[] map = l.split(":");
            mapInput(map[0], map[1]);
        }
    }

    private boolean isInputDown(String buttonName) {
        boolean input = false;
        try {
            Field buttonField = gamepad.getClass().getField(buttonName);
            if (buttonField.getGenericType().getTypeName() == "boolean") {
                input = buttonField.getBoolean(gamepad);
            }
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return input;
    }

    private void update() {


        for (String action: controllerMap.keySet()) {
            if (isInputDown(controllerMap.get(action))) {
                if ()
            }
        }

    }



   /* public boolean isActionPressed(String action) {

        String controllerAction = controllerMap.get(action);

        if (controllerAction.equals("a") && a) {
            return true;
        }
        if (controllerAction.equals("b") && b) {
            return true;
        }
        if (controllerAction.equals("x") && x) {
            return true;
        }
        if (controllerAction.equals("y") && y) {
            return true;
        }
        if (controllerAction.equals("right_trigger") && right_trigger > triggerPressThreshold) {
            return true;
        }
        if (controllerAction.equals("left_trigger") && left_trigger > triggerPressThreshold) {
            return true;
        }
        if (controllerAction.equals("right_bumper") && right_bumper) {
            return true;
        }
        if (controllerAction.equals("left_bumper") && left_bumper) {
            return true;
        }
        if (controllerAction.equals("left_stick_button") && left_stick_button) {
            return true;
        }
        if (controllerAction.equals("right_stick_button") && right_stick_button) {
            return true;
        }
        if (controllerAction.equals("dpad_up") && dpad_up) {
            return true;
        }
        if (controllerAction.equals("dpad_down") && dpad_down) {
            return true;
        }
        if (controllerAction.equals("dpad_right") && dpad_right) {
            return true;
        }
        if (controllerAction.equals("dpad_left") && dpad_left) {
            return true;
        }


        return false;
    }*/

}
