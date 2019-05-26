package org.firstinspires.ftc.teamcode.debug

import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.util.ElapsedTime
import org.firstinspires.ftc.teamcode.internal.BaseOpMode
import org.firstinspires.ftc.teamcode.internal.units.IMU

/**
 * 这是用来单独调试升降的程序，模块化的测试是在整体组装前十分必要的步骤。
 */
@TeleOp(name = "升降", group = "Debug")
class LiftDebug: BaseOpMode(false, true, false, false,
        false, false, false) {

    override fun run() {
        waitForStart()
        while (opModeIsActive()) {
            if (gamepad1.left_stick_y in -0.01..0.01 && gamepad1.right_stick_y in -0.01..0.01) {
                if (lift.autoSetPower()) {
                    lift.correctON = true
                }
            } else {
                lift.correctON = false
                lift.leftLiftMotor.power = -gamepad1.left_stick_y.toDouble()
                lift.rightLiftMotor.power = -gamepad1.right_stick_y.toDouble()
            }
            telemetry.addData("left", lift.leftLiftMotor.currentPosition)
            telemetry.addData("right", lift.rightLiftMotor.currentPosition)
            telemetry.update()
        }
    }

}