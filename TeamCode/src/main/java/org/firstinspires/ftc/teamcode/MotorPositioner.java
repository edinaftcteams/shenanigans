package org.firstinspires.ftc.teamcode.TeleOP.Tools;

import com.qualcomm.robotcore.hardware.DcMotor;

public class MotorPositioner {
    private DcMotor motor;
    private double finalPower, currVel, tPosition, tVel, currPosition, lastPos, addPower, maxSpeed;
    int accel, max, charge;

    //    Constructor
    public MotorPositioner(DcMotor motor, int acceleration, int maxEncoderValue, double maxSpeed, boolean reversed){

        this.motor = motor;
        this.motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        accel = acceleration;
        max = maxEncoderValue;
        this.maxSpeed = maxSpeed;

        finalPower = 0;
        currVel = 0;
        tVel = 0;
        lastPos = 0;
        addPower = 0;

        charge = reversed? -1 : 0;

    }


    //    This will determine where the motor will move to
    public void setTargetPosition(double position){
        tPosition = position;
    }

    public void move(){

        currPosition = (double) motor.getCurrentPosition() / max;
        tVel = (tPosition - currPosition);
        currVel = currPosition - lastPos;
        lastPos = currPosition;
        addPower = (tVel - currVel) * accel;
        finalPower = (tVel + addPower) * charge;

        finalPower = !(Math.abs(finalPower) > maxSpeed) ? finalPower : maxSpeed * (Math.abs(finalPower) / finalPower);

        motor.setPower(finalPower);

    }

}
