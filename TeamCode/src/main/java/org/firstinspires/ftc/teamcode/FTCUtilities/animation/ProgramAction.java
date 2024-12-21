package org.firstinspires.ftc.teamcode.FTCUtilities.animation;

import com.google.common.primitives.Primitives;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ProgramAction extends Action implements Runnable {

    private Method method;

    private Object[] args;

    private String actionString;

    public ProgramAction(LinearOpMode opMode, String methodName, Object... args) {
        this.opMode = opMode;
        try {
            Class[] paramTypes = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                Class type = args[i].getClass();
                if(Primitives.isWrapperType(type)) {
                    type = Primitives.unwrap(type);
                }

                paramTypes[i] = type;
            }

            this.method = opMode.getClass().getMethod(methodName, paramTypes);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        this.args = args;
        actionString = methodName + "(";
        for (Object arg : args) {
            actionString += String.valueOf(arg) + ", ";
        }
        actionString = actionString.substring(0,actionString.length() - 2);
        actionString += ")";

    }

    @Override
    public void run() {
        super.run();

        System.out.println(method.getName());

        try {
            method.invoke(opMode, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

    }


}
