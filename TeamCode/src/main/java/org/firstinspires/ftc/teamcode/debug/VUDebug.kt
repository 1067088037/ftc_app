package org.firstinspires.ftc.teamcode.debug

import com.qualcomm.robotcore.eventloop.opmode.Disabled
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.firstinspires.ftc.teamcode.internal.BaseOpMode

/**
 * 这是用来单独调试Vuforia的程序，模块化的测试是在整体组装前十分必要的步骤。
 */
@Disabled
@TeleOp(name = "Vuforia", group = "Debug")
class VUDebug: BaseOpMode(false, false, true, false,
        false, false, false) {

    override fun run() {
        waitForStart()
        vuforia.activate()
        while (opModeIsActive()) {
            val position = vuforia.getResultInPosition()
            val rotation = vuforia.getResultInRotation()
            telemetry.addData("name", vuforia.getResultInName())
            telemetry.addData("position", "x:${String.format("%.1f",position[0])}, " +
                    "y:${String.format("%.1f",position[1])}, z:${String.format("%.1f",position[2])}")
            telemetry.addData("rotation", "x:${String.format("%.1f",rotation[0])}, " +
                    "y:${String.format("%.1f",rotation[1])}, z:${String.format("%.1f",rotation[2])}")
            telemetry.update()
            sleep(100)
        }
    }

}