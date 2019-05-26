package org.firstinspires.ftc.teamcode.debug

import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.util.ElapsedTime
import org.firstinspires.ftc.teamcode.internal.BaseOpMode
import org.firstinspires.ftc.teamcode.internal.units.IMU

/**
 * 这是用来单独调试机械臂的程序，模块化的测试是在整体组装前十分必要的步骤。
 */
@TeleOp(name = "机械臂", group = "Debug")
class ArmDebug: BaseOpMode(false, false, false, false,
        false, false, true) {

    override fun run() {
        waitForStart()
        while (opModeIsActive()) {
            arm.setArmByGamepad()
            arm.pid.debug()
            telemetry.addData("up", arm.armUpMotor.currentPosition)
            telemetry.addData("down", arm.armDownMotor.currentPosition)
            telemetry.update()
        }
    }

}