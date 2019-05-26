package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.firstinspires.ftc.teamcode.internal.BaseOpMode

/**
 * 这是手动程序。
 */
@TeleOp(name = "<<<手动程序>>>", group = "0")
class Manual: BaseOpMode(true, true, false, false,
        false, true, true) {

    override fun run() {
        gamepadMonitor = true
        waitForStart()
        while (opModeIsActive()) {
            chassis.setPowerByGamepad()
            chassis.autoDirectionChange()
            lift.autoSetPower()
            arm.setArmByGamepad()
            collect.autoSetAll()
            telemetry.addData("底盘方向", chassis.directionAllChanged)
            telemetry.addData("自动到顶", lift.autoToPosition)
            telemetry.addData("电池电压", String.format("%.2f", getBatteryVoltage()))
            telemetry.update()
            sleep(10)
        }
    }

}