package org.firstinspires.ftc.teamcode.debug

import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.util.ElapsedTime
import org.firstinspires.ftc.teamcode.internal.BaseOpMode
import org.firstinspires.ftc.teamcode.internal.units.IMU

/**
 * 这是用来单独调试收集的程序，模块化的测试是在整体组装前十分必要的步骤。
 */
@TeleOp(name = "收集", group = "Debug")
class CollectDebug: BaseOpMode(false, false, false, false,
        false, true, false) {

    override fun run() {
        waitForStart()
        while (opModeIsActive()) {
            collect.autoSetAll()
            telemetry.update()
        }
    }

}