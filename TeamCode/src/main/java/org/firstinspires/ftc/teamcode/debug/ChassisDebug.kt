package org.firstinspires.ftc.teamcode.debug

import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.util.ElapsedTime
import org.firstinspires.ftc.teamcode.internal.BaseOpMode
import org.firstinspires.ftc.teamcode.internal.units.IMU

/**
 * 这是用来单独调试底盘的程序，模块化的测试是在整体组装前十分必要的步骤。
 */
@TeleOp(name = "底盘", group = "Debug")
class ChassisDebug: BaseOpMode(true, false, false, false,
        false, false, false) {

    override fun run() {
        waitForStart()
        while (opModeIsActive()) {
            chassis.setPowerByGamepad()
            chassis.autoDirectionChange()
            chassis.telemetryPosition()
            telemetry.update()
        }
    }

}