package org.inventors.ftc.robotbase.drive;

import static org.inventors.ftc.robotbase.RobotEx.OpModeType.TELEOP;

import com.acmerobotics.dashboard.config.Config;
import com.arcrobotics.ftclib.command.CommandScheduler;
import com.arcrobotics.ftclib.command.Subsystem;
import com.arcrobotics.ftclib.command.SubsystemBase;
import com.arcrobotics.ftclib.hardware.motors.Motor;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.hardware.VoltageSensor;

import org.inventors.ftc.robotbase.hardware.MotorExEx;
import org.inventors.ftc.robotbase.RobotEx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/*
 * Simple mecanum drive hardware implementation for REV hardware.
 */
@Config
public class MecanumDriveSubsystem extends SubsystemBase {
    static DriveConstants RobotConstants;

    public MecanumDriveSubsystem(HardwareMap hardwareMap, RobotEx.OpModeType type, DriveConstants robotConstants) {
        this.RobotConstants = robotConstants;

        batteryVoltageSensor = hardwareMap.voltageSensor.iterator().next();

        // TODO: adjust the names of the following hardware devices to match your configuration
        imu = hardwareMap.get(IMU.class, "imu");
        IMU.Parameters parameters = new IMU.Parameters(new RevHubOrientationOnRobot(
                RevHubOrientationOnRobot.LogoFacingDirection.UP,
                RevHubOrientationOnRobot.UsbFacingDirection.RIGHT
        ));
        imu.initialize(parameters);

        frontLeft = new MotorExEx(hardwareMap, "frontLeft", Motor.GoBILDA.RPM_312);
        frontRight = new MotorExEx(hardwareMap, "frontRight", Motor.GoBILDA.RPM_312);
        rearRight = new MotorExEx(hardwareMap, "rearRight", Motor.GoBILDA.RPM_312);
        rearLeft = new MotorExEx(hardwareMap, "rearLeft", Motor.GoBILDA.RPM_312);

        motors = Arrays.asList(frontLeft, frontRight, rearLeft, rearRight);

        if (RobotConstants.RUN_USING_ENCODER) setMode(MotorExEx.RunMode.VelocityControl);


        setZeroPowerBehavior(MotorExEx.ZeroPowerBehavior.BRAKE);

        if (RobotConstants.RUN_USING_ENCODER && RobotConstants.MOTOR_VELO_PID != null) {
            setPIDFCoefficients(RobotConstants.VELO_KP, RobotConstants.VELO_KI, RobotConstants.VELO_KD);

            double batteryPercentage = 12 / batteryVoltageSensor.getVoltage();

            frontLeft.setFeedforwardCoefficients(
                    RobotConstants.frontLeftFeedForward[0],
                    RobotConstants.frontLeftFeedForward[1],
                    RobotConstants.frontLeftFeedForward[2]
            );
            frontRight.setFeedforwardCoefficients(
                    RobotConstants.frontRightFeedForward[0],
                    RobotConstants.frontRightFeedForward[1],
                    RobotConstants.frontRightFeedForward[2]
            );
            rearLeft.setFeedforwardCoefficients(
                    RobotConstants.rearLeftFeedForward[0],
                    RobotConstants.rearLeftFeedForward[1],
                    RobotConstants.rearLeftFeedForward[2]
            );
            rearRight.setFeedforwardCoefficients(
                    RobotConstants.rearRightFeedForward[0],
                    RobotConstants.rearRightFeedForward[1],
                    RobotConstants.rearRightFeedForward[2]
            );
        }

        if (type == TELEOP) {
            /* ------------------------------------- TELEOP ------------------------------------- */
            CommandScheduler.getInstance().registerSubsystem(this);
            setMotorsInverted(RobotConstants.frontLeftInverted, RobotConstants.frontRightInverted, RobotConstants.rearRightInverted, RobotConstants.rearLeftInverted);
            drive = new com.arcrobotics.ftclib.drivebase.MecanumDrive(
                    frontLeft, frontRight, rearLeft, rearRight
            );
        }
    }
    /* ----------------------------------------- TELEOP ----------------------------------------- */
    private MotorExEx frontLeft, frontRight, rearRight, rearLeft;
    private IMU imu;
    private List<MotorExEx> motors;
    private VoltageSensor batteryVoltageSensor;
    protected String m_name = this.getClass().getSimpleName();
    private com.arcrobotics.ftclib.drivebase.MecanumDrive drive;

    public String getName()
    {
        return m_name;
    }
    public void setName(String name)
    {
        m_name = name;
    }
    public String getSubsystem()
    {
        return getName();
    }
    public void setSubsystem(String subsystem)
    {
        setName(subsystem);
    }
    void drive(double strafeSpeed, double forwardSpeed, double turnSpeed, double heading, double fast_input, double slow_input)
    {
        drive.setMaxSpeed(RobotConstants.DEFAULT_SPEED_PERC + fast_input * RobotConstants.FAST_SPEED_PERC - slow_input * RobotConstants.SLOW_SPEED_PERC);
        drive.driveFieldCentric(strafeSpeed, forwardSpeed, turnSpeed, heading);
    }
    public void setMotorsInverted(
            boolean leftFrontInverted, boolean rightFrontInverted,
            boolean rightRearInverted, boolean leftRearInverted
    )
    {
        frontLeft.setInverted(leftFrontInverted);
        rearLeft.setInverted(leftRearInverted);
        frontRight.setInverted(rightFrontInverted);
        rearRight.setInverted(rightRearInverted);
    }
    public void setMode(Motor.RunMode mode)
    {
        for (MotorExEx motor : motors)
            motor.setRunMode(mode);
    }
    public void setZeroPowerBehavior(MotorExEx.ZeroPowerBehavior zeroPowerBehavior)
    {
        for (MotorExEx motor : motors)
            motor.setZeroPowerBehavior(zeroPowerBehavior);
    }
    public void setPIDFCoefficients(double kP, double kI, double kD)
    {
        for (MotorExEx motor : motors) {
            motor.setIntegralBounds(RobotConstants.minIntegralBound, RobotConstants.maxIntegralBound);
            motor.setVeloCoefficients(kP, kI, kD);
        }
    }
}