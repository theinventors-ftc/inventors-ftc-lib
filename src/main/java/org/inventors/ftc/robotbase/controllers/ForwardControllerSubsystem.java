package org.inventors.ftc.robotbase.controllers;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.arcrobotics.ftclib.command.SubsystemBase;
import com.arcrobotics.ftclib.controller.PIDController;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;

public class ForwardControllerSubsystem extends SubsystemBase {
    private DoubleSupplier distValue;

    private double target;

    private boolean enabled = false;

    private final double kP = 0.012;
    private final double kI = 0;
    private final double kD = 0 ;

    PIDController controller;

    private Telemetry telemetry;

    public ForwardControllerSubsystem(DoubleSupplier distValue, double target,
                                      Telemetry telemetry) {
        controller = new PIDController(kP, kI, kD);
        controller.setSetPoint(target);
        this.distValue = distValue;
        this.target = target;
        this.telemetry = telemetry;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void periodic() {
        telemetry.addData("Distance: ", distValue.getAsDouble());
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    public double calculateOutput() {
        return controller.calculate(distValue.getAsDouble());
    }


    public boolean isEnabled() {
        return enabled;
    }

    public void setGyroTarget(double target) {
        controller.setSetPoint(target);
    }

    public double getTarget() {
        return target;
    }

    public void enable() {
        enabled = true;
    }

    public void disable() {
        enabled = false;
    }

    public void toggleState() {
        enabled = !enabled;
    }
}