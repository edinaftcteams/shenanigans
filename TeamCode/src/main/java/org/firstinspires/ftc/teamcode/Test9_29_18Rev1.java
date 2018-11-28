/*
Copyright 2018 FIRST Tech Challenge Team 15133

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
associated documentation files (the "Software"), to deal in the Software without restriction,
including without limitation the rights to use, copy, modify, merge, publish, distribute,
sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial
portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.Gyroscope;
import com.qualcomm.robotcore.hardware.Blinker;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.ElapsedTime;

/**
 * This file contains an minimal example of a Linear "OpMode". An OpMode is a 'program' that runs in either
 * the autonomous or the teleop period of an FTC match. The names of OpModes appear on the menu
 * of the FTC Driver Station. When an selection is made from the menu, the corresponding OpMode
 * class is instantiated on the Robot Controller and executed.
 *
 * This particular OpMode just executes a basic Tank Drive Teleop for a PushBot
 * It includes all the skeletal structure that all linear OpModes contain.
 *
 * Remove a @Disabled the on the next line or two (if present) to add this opmode to the Driver Station OpMode list,
 * or add a @Disabled annotation to prevent this OpMode from being added to the Driver Station
 */
@TeleOp(name = "POV Drive Code")


public class Test9_29_18Rev1 extends LinearOpMode {
    private Gyroscope imu;
    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor rearLeft;
    private DcMotor rearRight;
    private DcMotor liftMotor;
    private DcMotor sweepMotor;
    private Servo markerDrop;
    private Blinker expansion_Hub_2;
    private Blinker expansion_Hub_3;




    @Override
    public void runOpMode() {
        imu = hardwareMap.get(Gyroscope.class, "imu");
        
        //***Motors***
        frontLeft = hardwareMap.get(DcMotor.class, "Front Left");
        frontRight = hardwareMap.get(DcMotor.class, "Front Right");
        rearLeft = hardwareMap.get(DcMotor.class, "Rear Left");
        rearRight = hardwareMap.get(DcMotor.class, "Rear Right");
        sweepMotor = hardwareMap.get(DcMotor.class, "Sweep Motor");
        liftMotor = hardwareMap.get(DcMotor.class, "Lift Motor");
        
        //***Servos***
        markerDrop = hardwareMap.get(Servo.class, "Marker Drop");
        
        //***Controllers***
        expansion_Hub_2 = hardwareMap.get(Blinker.class, "Expansion Hub 2");
        expansion_Hub_3 = hardwareMap.get(Blinker.class, "Expansion Hub 3");
        
        //***Motor Direction***       
        // Calibrated based on Official Robot 1
        frontLeft.setDirection(DcMotor.Direction.FORWARD);
        frontRight.setDirection(DcMotor.Direction.REVERSE);
        rearLeft.setDirection(DcMotor.Direction.FORWARD);
        rearRight.setDirection(DcMotor.Direction.REVERSE);
        sweepMotor.setDirection(DcMotor.Direction.FORWARD);
        liftMotor.setDirection(DcMotor.Direction.FORWARD);
        
        //***Reset Encoders***
        frontLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rearLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rearRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        frontLeft.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        frontRight.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        rearLeft.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        rearRight.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        
        //***Enable Brake Mode***
     
        //Should enable brakemode on the elevator motor
        liftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        telemetry.addData("Status", "Initialized");
        telemetry.update();
        // Wait for the game to start (driver presses PLAY)
        waitForStart();

        liftMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        liftMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);


        // mechanical people need a higher gear ratio
        // run until the end of the match (driver presses STOP)

        double liftPower = 0.5;
        double turn = 0.0;
        boolean liftUp = false;
        boolean liftDown = false;
    
        int liftPosition = 0;
        int frontLeftPosition = 0;
        int frontRightPosition = 0;
        int rearLeftPosition = 0;
        int rearRightPosition = 0;

        double forward;
        double left;
        double right;
        double max = 1.0;
        
        while (opModeIsActive()) {


            left = -gamepad1.left_stick_y;
            right = -gamepad1.right_stick_y;

            forward = -gamepad2.left_stick_y;

            /*Code for aracade drive
            drive = -gamepad1.left_stick_y;
            turn  =  gamepad1.right_stick_x;

            // Combine drive and turn for blended motion.
            left  = drive + turn;
            right = drive - turn;*/

            // Normalize the values so neither exceed +/- 1.0
            //commented out to see if it helps with the lag
           /* max = Math.max(Math.abs(left), Math.abs(right));
            if (max > 1.0)
            {
                left /= max;
                right /= max;
            }*/


            liftUp = this.gamepad2.left_bumper;
            liftDown = this.gamepad2.right_bumper;
            if(liftUp){
                liftMotor.setPower(-liftPower);
            } else if(liftDown) {
                liftMotor.setPower(liftPower);
            } else {
                liftMotor.setPower(0.0);
            }
            
            if(gamepad1.b){
                markerDrop.setPosition(0);
            }
            
            if(gamepad1.y){
                markerDrop.setPosition(0.55);
            }
            //programs chassis
            if(gamepad1.left_bumper || gamepad1.right_bumper){
                frontRight.setPower(right/2);
                frontLeft.setPower(left/2);
                rearRight.setPower(right/2);
                rearLeft.setPower(left/2);
            }
            else{
                frontRight.setPower(right);
                frontLeft.setPower(left);
                rearRight.setPower(right);
                rearLeft.setPower(left);
            }
            //programs sweep arm movement
            if(gamepad2.left_stick_y>0.1) {
                sweepMotor.setPower(forward);
            }
            else{
                sweepMotor.setPower(0);
            }

            
            liftPosition = liftMotor.getCurrentPosition();
            
            //Drivetrain encoders
            frontLeftPosition = frontLeft.getCurrentPosition();    
            frontRightPosition = frontRight.getCurrentPosition();
            rearLeftPosition = rearRight.getCurrentPosition();
            rearRightPosition = rearRight.getCurrentPosition();
            
            
            
            
            //telemetry.addData("Target Power 2", tgtPower2);
            //telemetry.addData("Target Power", tgtPower);
            //telemetry.addData("Motor Power 2", frontLeft.getPower());
            //telemetry.addData("Motor Power", frontRight.getPower());
            //telemetry.addData("Motor Power 2", rearLeft.getPower());
            //telemetry.addData("Motor Power", rearRight.getPower());
            //telemetry.addData("Lift Position ", liftPosition);
            //telemetry.addData("F-L ", frontLeftPosition);
            //telemetry.addData("F-R ", frontRightPosition);
            //telemetry.addData("R-L ", rearLeftPosition);
            //telemetry.addData("R-R", rearRightPosition);
            //telemetry.addData("Status", "Running");
            telemetry.addData("lift Status - up", liftUp);
            telemetry.addData("lift Status - down", liftDown);
            telemetry.update();
    }



        // run until the end of the match (driver presses STOP)
        //why was this commented?
        /*while (opModeIsActive()) {
            telemetry.addData("Status", "Running");
            telemetry.update();

        }*/

    }
}
