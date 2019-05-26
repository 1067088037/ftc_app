package org.firstinspires.ftc.teamcode.debug

import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.util.ElapsedTime
import org.firstinspires.ftc.teamcode.internal.BaseOpMode
import org.firstinspires.ftc.teamcode.internal.units.IMU

/**
 * 这是用来单独调试TensorFlow的程序，模块化的测试是在整体组装前十分必要的步骤。
 */
@TeleOp(name = "TensorFlow", group = "Debug")
class TFDebug: BaseOpMode(false, false, false, true,
        false, false, false) {

    override fun run() {
        waitForStart()
        tensorFlow.activate()
        while (opModeIsActive()) {
            val result = tensorFlow.getGoldPositionNew()
            val position = tensorFlow.getGoldPosition()
            telemetry.addData("目标", result)
            telemetry.addData("x", position[0])
            telemetry.addData("y", position[1])
            telemetry.addData("imageW", config().ImageWidth)
            telemetry.addData("imgaeH", config().ImageHeight)
            telemetry.update()
            sleep(100)
        }
    }

}