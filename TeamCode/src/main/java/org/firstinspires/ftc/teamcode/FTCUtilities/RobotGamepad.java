package org.firstinspires.ftc.teamcode.FTCUtilities;

import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.robotcore.internal.system.AppUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;


/**
 * Manages gamepad inputs and action mappings for the robot.
 * Handles configuration, mapping, and querying of controller inputs.
 */
public class RobotGamepad {

    // Fields
    private ArrayList<String> pressedInputs = new ArrayList<>();
    private ArrayList<String> actionQueue = new ArrayList<>();

    private ArrayList<String> actionReleaseQueue = new ArrayList<>();
    private ArrayList<String> toggledInputs = new ArrayList<>();
    private boolean initialized = false;
    private Gamepad gamepad;
    public String configName;

    public String configString;
    private final ArrayList<String> AXIAL_INPUTS = new ArrayList<>(Arrays.asList(
            "right_trigger", "left_trigger", "right_stick_x", "right_stick_y", "left_stick_x", "left_stick_y"
    ));
    private double triggerThreshold = 0.1;
    private HashMap<String, String> controllerMap = new HashMap<>();

    // Constructors
    /**
     * Initializes a new RobotGamepad with a default mapping file.
     * @implSpec MUST USE RobotGamepad.update()
     * @param gamepad Instance of FTC's Gamepad class (e.g., gamepad1).
     */
    public RobotGamepad(Gamepad gamepad) {
        this.gamepad = gamepad;
        mapString("");
    }

    /**
     * Initializes a new RobotGamepad with a custom mapping string.
     * @implNote It is recommended to load configuration from file.
     * @implSpec MUST USE RobotGamepad.update()
     * @param gamepad Instance of FTC's Gamepad class (e.g., gamepad1).
     * @param mappingString Custom mapping configuration as a string.
     */
    public RobotGamepad(Gamepad gamepad, String mappingString) {
        this.gamepad = gamepad;
        mapString(mappingString);
    }

    public static void update(RobotGamepad... gamepads) {
        for(RobotGamepad g : gamepads) {
            g.update();
        }
    }

    public void fromAsset(String assetName) {
        try {
            InputStream inputStream = AppUtil.getDefContext().getAssets().open(assetName);
            String map = new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining("\n"));
            mapString(map);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void mapFromString(String mapping) {
        mapString(mapping);
    }

    @Override
    public String toString() {
        String map = "";
        for (String a : controllerMap.keySet()) {
            map += a + " : " + controllerMap.get(a) + ",\n";
        }
        return map;
    }

    /**
     * Updates the gamepad state. Must be called in the main "while (opModeIsActive())" loop before processing inputs.
     */
    public void update() {
        initialized = true;
        actionQueue.clear();
        actionReleaseQueue.clear();

        for (String action : controllerMap.keySet()) {
            verifyInput(action);
            if (isInputDown(controllerMap.get(action))) {
                if (!pressedInputs.contains(action)) {
                    actionQueue.add(action);
                    pressedInputs.add(action);
                    if (toggledInputs.contains(action)) {
                        toggledInputs.remove(action);
                    } else {
                        toggledInputs.add(action);
                    }
                }
            } else {
                if (pressedInputs.contains(action)) {
                    pressedInputs.remove(action);
                    actionReleaseQueue.add(action);
                }
            }
        }
    }

    // Action Events
    /**
     * Checks if a specific action is currently pressed.
     * @param actionName Name of the action.
     * @return True if the action is pressed.
     */
    public boolean isActionPressed(String actionName) {
        checkInitialization();
        verifyInput(actionName);
        return pressedInputs.contains(actionName);
    }

    /**
     * Checks if a specific action is toggled.
     * @param actionName Name of the action.
     * @return True if the action is toggled.
     */
    public boolean isActionToggled(String actionName) {
        checkInitialization();
        verifyInput(actionName);
        return toggledInputs.contains(actionName);
    }
    public boolean isActionJustToggled(String actionName, boolean state) {
        checkInitialization();
        verifyInput(actionName);
        return (isActionToggled(actionName) == state) && isActionJustPressed(actionName);
    }

    /**
     * Gets the analog value of a specific action.
     * @param actionName Name of the action.
     * @return The analog value.
     */
    public double getActionAxis(String actionName) {
        checkInitialization();
        verifyInput(actionName);
        return getInputAxis(controllerMap.get(actionName));
    }

    /**
     * Checks if a specific action was just pressed.
     * @param actionName Name of the action.
     * @return True if the action was just pressed.
     */
    public boolean isActionJustPressed(String actionName) {
        checkInitialization();
        verifyInput(actionName);
        return actionQueue.contains(actionName);
    }

    /**
     * Checks if a specific action was just released.
     * @param actionName Name of the action.
     * @return True if the action was just released.
     */
    public boolean isActionJustReleased(String actionName) {
        checkInitialization();
        verifyInput(actionName);
        return actionReleaseQueue.contains(actionName);
    }

    public void overrideToggle(String actionName, boolean toggle) {
        if (toggle) {
            toggledInputs.add(actionName);
        }
        else {
            toggledInputs.remove(actionName);
        }
        update();
    }

    // Controller Mapping
    /**
     * Maps an action to a specific controller input.
     * @param actionName Action name.
     * @param controllerButton Input to trigger the action.
     */
    public void mapInput(String actionName, String controllerButton) {
        controllerMap.put(actionName, controllerButton);
    }


    /**
     * Loads a controller configuration from a file.
     * @param filePath Relative path to the configuration file. (Ex. /DefaultControllerMap.txt)
     */
    public void loadMappingFile(String filePath) {
        configName = filePath.substring(1);
        try {
            URL path = getClass().getResource(filePath);
            loadMappingFromFile(path.getFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Rumble Functions
    /**
     * Activates a brief rumble with the same strength for both motors.
     * @param strength The intensity of the rumble (0.0 to 1.0).
     */
    public void rumble(double strength) {
        gamepad.rumble(strength, strength, 200);
    }
    public void rumble(double strength, int durationMs) {
        gamepad.rumble(strength, strength, durationMs);
    }

    /**
     * Activates a brief rumble with separate strengths for the left and right motors.
     * @param left Strength for the left motor (0.0 to 1.0).
     * @param right Strength for the right motor (0.0 to 1.0).
     */
    public void rumble(double left, double right) {
        gamepad.rumble(left, right, 200);
    }
    public void rumble(double left, double right, int durationMs) {
        gamepad.rumble(left, right, durationMs);
    }

    /**
     * Activates a sequence of short, distinct rumble blips.
     * @param count The number of blips to play.
     */
    public void rumbleBlip(int count) {
        gamepad.rumbleBlips(count);
    }

    /**
     * Activates a sequence of short rumble blips with a specified strength.
     * @param strength The intensity of each blip (0.0 to 1.0).
     * @param count The total number of blips to play.
     */
    public void rumbleBlip(double strength, int count) {
        if (count <= 0) {
            return;
        }
        rumble(strength);
        EventSchedule eventSchedule = new EventSchedule(this, "rumbleBlip", strength, count - 1);
        eventSchedule.queue(0.5);
    }

    /**
     * Activates a sequence of short rumble blips with a specified strength and interval between blips.
     * @param strength The intensity of each blip (0.0 to 1.0).
     * @param count The total number of blips to play.
     * @param timeInterval The time interval between consecutive blips (in seconds).
     */
    public void rumbleBlip(double strength, int count, double timeInterval) {
        if (count <= 0) {
            return;
        }
        rumble(strength);
        EventSchedule eventSchedule = new EventSchedule(this, "rumbleBlip", strength, count - 1, timeInterval);
        eventSchedule.queue(timeInterval);
    }


    // Private Methods
    private void checkInitialization() {
        if (!initialized) {
            throw new RuntimeException("Controller not updated - use update()");
        }
    }

    private void verifyInput(String actionName) {
        if (!hasAction(actionName)) {
            throw new RuntimeException("Action '" + actionName + "' is not defined. \n" + controllerMap.toString());
        }
        if (!hasInput(controllerMap.get(actionName))) {
            throw new RuntimeException("Controller does not have input '" + controllerMap.get(actionName) + "'.");
        }
    }

    private boolean hasAction(String actionName) {
        return controllerMap.containsKey(actionName);
    }

    private boolean hasInput(String inputName) {
        if (inputName.charAt(0) == '-' || inputName.charAt(0) == '+') {
            inputName = inputName.substring(1);
        }
        try {
            gamepad.getClass().getField(inputName);
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }

    private void loadMappingFromFile(String filename) throws IOException {
        StringBuilder mappingString = new StringBuilder();
        try (BufferedReader file = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = file.readLine()) != null) {
                mappingString.append(line);
            }
        }
        mapString(mappingString.toString());
    }

    private void mapString(String mappingString) {
        mappingString = mappingString.replace("\n", "");
        String[] lines = mappingString.split(",");
        for (String l : lines) {
            try {
                l = l.trim();
                String[] map = l.split(":");
                if (map.length > 1) {
                    mapInput(map[0], map[1]);
                }
            }
            catch (RuntimeException e) {
                throw new RuntimeException("Controller Configuration File Error: " + l);
            }
        }
    }

    private boolean isInputDown(String buttonName) {
        boolean input = false;
        double axisDirection = 0.0;
        if (buttonName.charAt(0) == '-') {
            axisDirection = -1;
            buttonName = buttonName.substring(1);
        }
        else if (buttonName.charAt(0) == '+') {
            axisDirection = 1;
            buttonName = buttonName.substring(1);
        }
        try {
            Field buttonField = gamepad.getClass().getField(buttonName);
            if (!AXIAL_INPUTS.contains(buttonName)) {
                input = buttonField.getBoolean(gamepad);
            }
            else {
                double strength = buttonField.getDouble(gamepad);
                if (axisDirection < 0) {
                    input = strength < -triggerThreshold;
                }
                else {
                    input = strength > triggerThreshold;
                }

            }
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return input;

    }

    private double getInputAxis(String buttonName) {
        double input = 0.0;
        double direction = 0.0;
        if (buttonName.charAt(0) == '-') {
            buttonName = buttonName.substring(1);
            direction = -1;
        }
        else if (buttonName.charAt(0) == '+') {
            buttonName = buttonName.substring(1);
            direction = +1;
        }
        try {
            Field buttonField = gamepad.getClass().getField(buttonName);
            if (AXIAL_INPUTS.contains(buttonName)) {
                input = buttonField.getDouble(gamepad);
                if (direction != 0) {
                    if (!(direction * input > 0)) {
                        return 0;
                    }
                    else {
                        return Math.abs(input);
                    }
                }
            }
            else {
                input = buttonField.getBoolean(gamepad) ? 1.0 : 0.0 ;
            }
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return input;

    }
}


