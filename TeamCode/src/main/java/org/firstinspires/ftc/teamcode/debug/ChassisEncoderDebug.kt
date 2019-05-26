package org.firstinspires.ftc.teamcode.debug

import com.qualcomm.robotcore.eventloop.opmode.Disabled
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.util.ElapsedTime
import org.firstinspires.ftc.teamcode.internal.BaseOpMode
import org.firstinspires.ftc.teamcode.internal.units.IMU

/**
 * 这是用来单独调试底盘编码器的程序，模块化的测试是在整体组装前十分必要的步骤。
 * 在这个程序中，机器人会完成前进、平移等一系列动作，由精确的PID算法控制，详见底盘组件。
 */
@Disabled
@TeleOp(name = "底盘编码器", group = "Debug")
class ChassisEncoderDebug: BaseOpMode(true, false, false, false,
        false, false, false) {

    override fun run() {
        waitForStart()
        if (opModeIsActive()) {
            Thread {
                while (opModeIsActive()) {
                    telemetryLog(chassis.frontLeftMotor.currentPosition)
                }
            }.start()
            chassis.setStraightByEncoder(500)
            Thread.sleep(1000)
            chassis.setTransverseByEncoder(500)
            Thread.sleep(2000)
        }
    }

}