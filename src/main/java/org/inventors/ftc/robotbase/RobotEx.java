package org.inventors.ftc.robotbase;

import static org.firstinspires.ftc.robotcore.external.BlocksOpModeCompanion.telemetry;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.acmerobotics.dashboard.FtcDashboard;
import com.arcrobotics.ftclib.command.CommandScheduler;
import com.arcrobotics.ftclib.command.InstantCommand;
import com.arcrobotics.ftclib.command.button.Trigger;
import com.arcrobotics.ftclib.gamepad.GamepadKeys;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.inventors.ftc.robotbase.controllers.HeadingControllerSubsystem;
import org.inventors.ftc.robotbase.drive.DriveConstants;
import org.inventors.ftc.robotbase.drive.MecanumDriveCommand;
import org.inventors.ftc.robotbase.drive.MecanumDriveSubsystem;
import org.inventors.ftc.robotbase.hardware.Camera;
import org.inventors.ftc.robotbase.hardware.GamepadExEx;
import org.inventors.ftc.robotbase.hardware.IMUSubsystem;

public class RobotEx {
    // enum to specify opmode type
    public enum OpModeType {
        TELEOP, AUTO
    }

    protected OpModeType opModeType;
    protected FtcDashboard dashboard;

    protected GamepadExEx driverOp;
    protected GamepadExEx toolOp;

    protected MecanumDriveSubsystem drive = null;
    protected MecanumDriveCommand driveCommand = null;

    public Camera camera;

    protected HeadingControllerSubsystem gyroFollow;
    protected HeadingControllerSubsystem cameraFollow;
    protected final Boolean initCamera;

    protected IMUSubsystem gyro;

    protected Telemetry telemetry, dashTelemetry;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public RobotEx(HardwareMap hardwareMap, DriveConstants RobotConstants, Telemetry telemetry, GamepadExEx driverOp,
                   GamepadExEx toolOp) {
        this(hardwareMap, RobotConstants, telemetry, driverOp, toolOp, OpModeType.TELEOP, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public RobotEx(HardwareMap hardwareMap, DriveConstants RobotConstants, Telemetry telemetry, GamepadExEx driverOp,
                   GamepadExEx toolOp, OpModeType type, Boolean initCamera
    ) {
        this.initCamera = initCamera;
        initCommon(hardwareMap, RobotConstants, telemetry, type);
        if (type == OpModeType.TELEOP) {
            initTele(hardwareMap, driverOp, toolOp);
            opModeType = OpModeType.TELEOP;
        } else {
            initAuto(hardwareMap);
            opModeType = OpModeType.AUTO;
        }
    }

    public void initCommon(HardwareMap hardwareMap, DriveConstants RobotConstants, Telemetry telemetry, OpModeType type) {
        //////////////////////////////////////// Telemetries ///////////////////////////////////////
        this.telemetry = telemetry;
        this.dashTelemetry = FtcDashboard.getInstance().getTelemetry();

        //////////////////////////////////////////// IMU ///////////////////////////////////////////
        gyro = new IMUSubsystem(hardwareMap);
        CommandScheduler.getInstance().registerSubsystem(gyro);

        ////////////////////////////////////////// Camera //////////////////////////////////////////

        if (this.initCamera) camera = new Camera(hardwareMap, dashboard);

        /////////////////////////////////////////// Drive //////////////////////////////////////////
        drive = new MecanumDriveSubsystem(hardwareMap, type, RobotConstants);
    }

    public void initAuto(HardwareMap hardwareMap) {
        //////////////////////////////////////// Drivetrain ////////////////////////////////////////
//        SampleMecanumDrive rrDrive = new SampleMecanumDrive(hardwareMap);

        ////////////////////////// Setup and Initialize Mechanisms Objects /////////////////////////
        initMechanismsAutonomous(hardwareMap);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void initTele(HardwareMap hardwareMap, GamepadExEx driverOp,
                         GamepadExEx toolOp) {
        ///////////////////////////////////////// Gamepads /////////////////////////////////////////
        this.driverOp = driverOp;
        this.toolOp = toolOp;

        //////////////////////////////////////// Drivetrain ////////////////////////////////////////
        driveCommand = new MecanumDriveCommand(drive, this::drivetrainForward,
                this::drivetrainStrafe, this::drivetrainTurn, gyro::getRawYaw,
                () -> driverOp.getTrigger(GamepadKeys.Trigger.RIGHT_TRIGGER), () -> driverOp.getTrigger(GamepadKeys.Trigger.LEFT_TRIGGER));

        CommandScheduler.getInstance().registerSubsystem(drive);
        drive.setDefaultCommand(driveCommand);

        /////////////////////////////////////// Gyro Follower //////////////////////////////////////
        driverOp.getGamepadButton(GamepadKeys.Button.START)
                .whenPressed(new InstantCommand(gyro::resetYawValue, gyro));

        gyroFollow = new HeadingControllerSubsystem(gyro::getYaw,
                gyro::findClosestOrientationTarget);
        new Trigger(() -> driverOp.getRightY() >= 0.8).whenActive(
                new InstantCommand(() -> gyroFollow.setGyroTarget(180), gyroFollow));
        new Trigger(() -> driverOp.getRightY() <= -0.8).whenActive(
                new InstantCommand(() -> gyroFollow.setGyroTarget(0), gyroFollow));
        new Trigger(() -> driverOp.getRightX() >= 0.8).whenActive(
                new InstantCommand(() -> gyroFollow.setGyroTarget(-90), gyroFollow));
        new Trigger(() -> driverOp.getRightX() <= -0.8).whenActive(
                new InstantCommand(() -> gyroFollow.setGyroTarget(90), gyroFollow));

        driverOp.getGamepadButton(GamepadKeys.Button.RIGHT_STICK_BUTTON)
                .whenPressed(new InstantCommand(gyroFollow::toggleState, gyroFollow));

        ////////////////////////// Setup and Initialize Mechanisms Objects /////////////////////////
        initMechanismsTeleOp(hardwareMap);
    }

    public void initMechanismsTeleOp(HardwareMap hardwareMap) {
        // should be overridden by child class
    }

    public void initMechanismsAutonomous(HardwareMap hardwareMap) {
        // should be overridden by child class
    }

    public double drivetrainStrafe() {
        return driverOp.getLeftX();
    }

    public double drivetrainForward() {
        return driverOp.getLeftY();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public double drivetrainTurn() {
        return gyroFollow.isEnabled() ? -gyroFollow.calculateTurn() : driverOp.getRightX();
    }

    public Telemetry getTelemetry() {
        return telemetry;
    }

    public Telemetry getDashboardTelemetry() {
        return dashTelemetry;
    }
}