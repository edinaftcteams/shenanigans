package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.ElapsedTime;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.Gyroscope;
import com.qualcomm.robotcore.hardware.Blinker;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.vuforia.Vuforia;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer.CameraDirection;
//import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer.CameraDevice;
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector;
import org.firstinspires.ftc.robotcore.external.tfod.Recognition;

import java.util.List;

/**
 * This file illustrates the concept of driving a path based on encoder counts.
 * It uses the common Pushbot hardware class to define the drive on the robot.
 * The code is structured as a LinearOpMode
 *
 * The code REQUIRES that you DO have encoders on the wheels,
 *   otherwise you would use: PushbotAutoDriveByTime;
 *
 *  This code ALSO requires that the drive Motors have been configured such that a positive
 *  power command moves them forwards, and causes the encoders to count UP.
 *
 *   The desired path in this example is:
 *   - Drive forward for 48 inches
 *   - Spin right for 12 Inches
 *   - Drive Backwards for 24 inches
 *   - Stop and close the claw.
 *
 *  The code is written using a method called: encoderDrive(speed, leftInches, rightInches, timeoutS)
 *  that performs the actual movement.
 *  This methods assumes that each movement is relative to the last stopping place.
 *  There are other ways to perform encoder based moves, but this method is probably the simplest.
 *  This code uses the RUN_TO_POSITION mode to enable the Motor controllers to generate the run profile
 *
 * Use Android Studios to Copy this Class, and Paste it into your team's code folder with a new name.
 * Remove or comment out the @Disabled line to add this opmode to the Driver Station OpMode list
 */





@Autonomous(name="Depot_Side_Corner_Pause2", group="Pushbot")
//@Disabled
public class Auto4WithVisionTest extends LinearOpMode {

    /* Declare OpMode members. */
    HardwarePushbot robot = new HardwarePushbot();   // Use a Pushbot's hardware
    private ElapsedTime runtime = new ElapsedTime();

    static final double COUNTS_PER_MOTOR_REV = 1120;    // eg: TETRIX Motor Encoder
    static final double DRIVE_GEAR_REDUCTION = 1.0;     // This is < 1.0 if geared UP
    static final double WHEEL_DIAMETER_INCHES = 4.0;     // For figuring circumference
    static final double GEAR_DIAMETER_INCHES = 1.0;
    static final double COUNTS_PER_INCH = (COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION) /
            (WHEEL_DIAMETER_INCHES * 3.1415);
    static final double COUNTS_PER_INCH_GEAR = (COUNTS_PER_MOTOR_REV * DRIVE_GEAR_REDUCTION) /
            (GEAR_DIAMETER_INCHES * 3.1415);
    static final double DRIVE_SPEED = 0.5;
    static final double TURN_SPEED = 0.4;
    static final double LIFT_SPEED = 0.40;

    private static final String TFOD_MODEL_ASSET = "RoverRuckus.tflite";
    private static final String LABEL_GOLD_MINERAL = "Gold Mineral";
    private static final String LABEL_SILVER_MINERAL = "Silver Mineral";


    private static final String VUFORIA_KEY = "ASrn8DD/////AAABmeHX9UmzA0K8jvr/RZ7JqB4pi8ngJmsjNWVon4unwUy2dBaSgWTdnpK7O9vUO/UR5iYeOLqOWX5iqIv9UKZPv+zSO8rYBglYG0KBLNnFIJYn1SjkwrLNw5/wqi6svyKZrIFK5QPL6o3wtsj2DyDQsG7+tTYBhhQ5oyioo1HH8QZSt5nmbM+BpG5iFLZrvMIxmL281IVLKAK5z2P4p0FMJwGVAM5hlL1I6ZUnNPNK9V/jrTAWs+skVz2FAWL6jaHn24DMW5QUj3U4z8LAKa5qontkdEKsFiohvqHAg7zuzC03zPkhdieRDFP9CC7/2u40BCwDifvdvk9ApbE66BsiKWDOZirOc0e+Go0cpgPIq2Wh";

    /**
     * {@link #vuforia} is the variable we will use to store our instance of the Vuforia
     * localization engine.
     */
    private VuforiaLocalizer vuforia;

    /**
     * {@link #tfod} is the variable we will use to store our instance of the Tensor Flow Object
     * Detection engine.
     */
    private TFObjectDetector tfod;

    @Override
    public void runOpMode() {

        /*
         * Initialize the drive system variables.
         * The init() method of the hardware class does all the work here
         */
        robot.init(hardwareMap);

        // Send telemetry message to signify robot waiting;
        telemetry.addData("Status", "Resetting Encoders");    //
        telemetry.update();
        robot.frontRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.frontLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.rearRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.rearLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        robot.liftMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        robot.frontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.frontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.rearRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.rearLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        robot.liftMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER); //Changed this to test

        // Send telemetry message to indicate successful Encoder reset
        telemetry.addData("Path0", "Starting at %7d :%7d",
                robot.frontRight.getCurrentPosition(),
                robot.frontLeft.getCurrentPosition(), robot.liftMotor.getCurrentPosition());
        telemetry.update();

        robot.liftMotor.setPower(-.2);
        // Send telemetry message indicating that the motor has initiated
        telemetry.addData("Path0", "Starting at %7d :%7d",
                robot.frontRight.getCurrentPosition(),
                robot.frontLeft.getCurrentPosition(), robot.liftMotor.getCurrentPosition());
        telemetry.update();

        // Wait for the game to start (driver presses PLAY)
        while(!opModeIsActive()&&!isStopRequested()){
            telemetry.addData("Status", "Waiting in Init");
            telemetry.update();
        }

        robot.liftMotor.setPower(0.0);
        sleep(2000);

        // Step through each leg of the path,
        // Note: Reverse movement is obtained by setting a negative distance (not speed)
        encoderLiftDrive(LIFT_SPEED, 4.0, 3.0);                                 // S1: Up 3 Inches with a 3 Sec timeout
        encoderDrive(DRIVE_SPEED, -6.0, -6.0, 4.0);               // S2: Back 6 Inches with 4 Sec timeout
        encoderLiftDrive(LIFT_SPEED, -10.0, 3.0);                                // S3: Down 7 Inches with a 3 Sec timeout
        //
        // Look for gold jewel location
        // Method jewelLocation return 0 for fail / timeout
        // 1 for left location, 2 for right location and 3 for center location
        //

        if (jewelLocation() == 1) {
            // move to left jewel
            telemetry.addData("Gold Mineral Position", "Left");
        } else if (jewelLocation() == 2) {
            // move to right jewel
            telemetry.addData("Gold Mineral Position", "Right");
        } else if (jewelLocation() == 2) {
            // move to right jewel
            telemetry.addData("Gold Mineral Position", "Center");
        } else {
            // Jewel not detected - proceed to next step
            telemetry.addData("Gold Mineral Position", "Jewel not detected");
        }

        sleep(1000);
        encoderDrive(DRIVE_SPEED, -50.8, -50.8, 4.0);             // S4: Reverse 25.5 Inches with 4 Sec timeout
        encoderDrive(DRIVE_SPEED, -6.0, 6.0, 4.0);                // S5: Turn 4 Inches Left with 4 Sec timeout
        robot.markerDrop.setPosition(.52);                                                       // S6: Drop the marker
        sleep(1000);
        encoderDrive(DRIVE_SPEED, 9.0, -9.0, 4.0);                 // S7: Turn right 4 inches
        encoderDrive(DRIVE_SPEED, 30.0, 30.0, 4.0);                // S8: Move forwards 24 inches


        //sleep(1000);     // pause for servos to move
    }

    /*
     *  Method to perform a relative move, based on encoder counts.
     *  Encoders are not reset as the move is based on the current position.
     *  Move will stop if any of three conditions occur:
     *  1) Move gets to the desired position
     *  2) Move runs out of time
     *  3) Driver stops the opmode running.
     */

    public int jewelLocation() {

        initVuforia();

        if (ClassFactory.getInstance().canCreateTFObjectDetector()) {
            initTfod();
        } else {
            telemetry.addData("Sorry!", "This device is not compatible with TFOD");
            return 0;
        }

        long start = System.currentTimeMillis();
        //CameraDevice.Instance.SetFlashTorchMode(true);

        while ((System.currentTimeMillis()/1000F-start)<10.0) {
            if (tfod != null) {
                // getUpdatedRecognitions() will return null if no new information is available since
                // the last time that call was made.
                List<Recognition> updatedRecognitions = tfod.getUpdatedRecognitions();
                if (updatedRecognitions != null) {
                    telemetry.addData("# Object Detected", updatedRecognitions.size());
                    if (updatedRecognitions.size() == 3) {
                        int goldMineralX = -1;
                        int silverMineral1X = -1;
                        int silverMineral2X = -1;
                        for (Recognition recognition : updatedRecognitions) {
                            if (recognition.getLabel().equals(LABEL_GOLD_MINERAL)) {
                                goldMineralX = (int) recognition.getLeft();
                            } else if (silverMineral1X == -1) {
                                silverMineral1X = (int) recognition.getLeft();
                            } else {
                                silverMineral2X = (int) recognition.getLeft();
                            }
                        }
                        if (goldMineralX != -1 && silverMineral1X != -1 && silverMineral2X != -1) {
                            if (goldMineralX < silverMineral1X && goldMineralX < silverMineral2X) {
                                telemetry.addData("Gold Mineral Position", "Left");
                                telemetry.update();
                                return 1;
                            } else if (goldMineralX > silverMineral1X && goldMineralX > silverMineral2X) {
                                telemetry.addData("Gold Mineral Position", "Right");
                                telemetry.update();
                                return 2;
                            } else {
                                telemetry.addData("Gold Mineral Position", "Center");
                                telemetry.update();
                                return 3;
                            }
                        }
                    }
                    telemetry.update();
                }
            }

        }
        return 0;
    }




    public void encoderLiftDrive( double speed, double inches, double timeoutS) {
        //this is for how high off origin the lift arm is
        int newHeightTarget;

        //ensures opMode is active
        if (opModeIsActive()) {

            // Determine new target position, and pass to motor controller
            newHeightTarget = robot.liftMotor.getCurrentPosition() + (int) (inches * COUNTS_PER_INCH_GEAR);
            robot.liftMotor.setTargetPosition(newHeightTarget);

            // Turn On RUN_TO_POSITION
            robot.liftMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);

            // reset the timeout time and start motion.
            runtime.reset();
            robot.liftMotor.setPower(Math.abs(speed));

            // keep looping while we are still active, and there is time left, and both motors are running.
            // Note: We use (isBusy() && isBusy()) in the loop test, which means that when EITHER motor hits
            // its target position, the motion will stop.  This is "safer" in the event that the robot will
            // always end the motion as soon as possible.
            // However, if you require that BOTH motors have finished their moves before the robot continues
            // onto the next step, use (isBusy() || isBusy()) in the loop test.
            while (opModeIsActive() && (runtime.seconds() < timeoutS) &&
                    (robot.liftMotor.isBusy())) {

                // Display it for the driver.
                telemetry.addData("Path1", "Running to %7d", newHeightTarget);
                telemetry.addData("Path2", "Running at %7d",
                        robot.liftMotor.getCurrentPosition());
                telemetry.update();
            }

            // Stop all motion;
            robot.liftMotor.setPower(0);


            // Turn off RUN_TO_POSITION
            robot.liftMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

            //  sleep(250);   // optional pause after each move
        }
    }


    public void encoderDrive( double speedster, double leftInches, double rightInches, double timeoutS1) {
        int newLeftTarget;
        int newRightTarget;
        int newRearLeftTarget;
        int newRearRightTarget;

        // Ensure that the opmode is still active
        if (opModeIsActive()) {

            // Determine new target position, and pass to motor controller
            newLeftTarget = robot.frontLeft.getCurrentPosition() + (int) (leftInches * COUNTS_PER_INCH);
            newRightTarget = robot.frontRight.getCurrentPosition() + (int) (rightInches * COUNTS_PER_INCH);
            newRearLeftTarget = robot.rearLeft.getCurrentPosition() + (int) (leftInches * COUNTS_PER_INCH);
            newRearRightTarget = robot.rearRight.getCurrentPosition() + (int) (rightInches * COUNTS_PER_INCH);
            robot.frontLeft.setTargetPosition(newLeftTarget);
            robot.rearLeft.setTargetPosition(newRearLeftTarget);
            robot.frontRight.setTargetPosition(newRightTarget);
            robot.rearRight.setTargetPosition(newRearRightTarget);

            // Turn On RUN_TO_POSITION
            robot.frontLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            robot.rearLeft.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            robot.frontRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);
            robot.rearRight.setMode(DcMotor.RunMode.RUN_TO_POSITION);

            telemetry.addData("Path1", "Running to %7d :%7d", newLeftTarget, newRightTarget);
            telemetry.addData("Path2", "Running at %7d :%7d",
                    robot.frontLeft.getCurrentPosition(),
                    robot.frontRight.getCurrentPosition());
            telemetry.addData("Path3", "Running to %7d :%7d", newRearLeftTarget, newRearRightTarget);
            telemetry.addData("Path4", "Running at %7d :%7d",
                    robot.rearLeft.getCurrentPosition(),
                    robot.rearRight.getCurrentPosition());
            telemetry.update();

            // reset the timeout time and start motion.
            runtime.reset();
            robot.frontLeft.setPower(Math.abs(speedster));
            robot.frontRight.setPower(Math.abs(speedster));
            robot.rearLeft.setPower(Math.abs(speedster));
            robot.rearRight.setPower(Math.abs(speedster));

            // keep looping while we are still active, and there is time left, and both motors are running.
            // Note: We use (isBusy() && isBusy()) in the loop test, which means that when EITHER motor hits
            // its target position, the motion will stop.  This is "safer" in the event that the robot will
            // always end the motion as soon as possible.
            // However, if you require that BOTH motors have finished their moves before the robot continues
            // onto the next step, use (isBusy() || isBusy()) in the loop test.

            while (opModeIsActive() &&
                    (runtime.seconds() < timeoutS1) &&
                    (robot.frontLeft.isBusy() && robot.frontRight.isBusy() && robot.rearLeft.isBusy() && robot.rearRight.isBusy())) {

                // Display it for the driver.
                telemetry.addData("Path1", "Running to %7d :%7d", newLeftTarget, newRightTarget);
                telemetry.addData("Path2", "Running at %7d :%7d",
                        robot.frontLeft.getCurrentPosition(),
                        robot.frontRight.getCurrentPosition());
                telemetry.update();
            }

            // Stop all motion;
            robot.frontLeft.setPower(0);
            robot.frontRight.setPower(0);
            robot.rearLeft.setPower(0);
            robot.rearRight.setPower(0);

            // Turn off RUN_TO_POSITION
            robot.frontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            robot.frontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            robot.rearRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            robot.rearLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

            sleep(250);   // optional pause after each move
        }
    }

    /**
            * Initialize the Vuforia localization engine.
     */
    private void initVuforia() {
        /*
         * Configure Vuforia by creating a Parameter object, and passing it to the Vuforia engine.
         */
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();

        parameters.vuforiaLicenseKey = VUFORIA_KEY;
        parameters.cameraDirection = CameraDirection.BACK;

        //  Instantiate the Vuforia engine
        vuforia = ClassFactory.getInstance().createVuforia(parameters);

        // Loading trackables is not necessary for the Tensor Flow Object Detection engine.
    }

    /**
     * Initialize the Tensor Flow Object Detection engine.
     */
    private void initTfod() {
        int tfodMonitorViewId = hardwareMap.appContext.getResources().getIdentifier(
                "tfodMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        TFObjectDetector.Parameters tfodParameters = new TFObjectDetector.Parameters(tfodMonitorViewId);
        tfod = ClassFactory.getInstance().createTFObjectDetector(tfodParameters, vuforia);
        tfod.loadModelFromAsset(TFOD_MODEL_ASSET, LABEL_GOLD_MINERAL, LABEL_SILVER_MINERAL);
    }

}


