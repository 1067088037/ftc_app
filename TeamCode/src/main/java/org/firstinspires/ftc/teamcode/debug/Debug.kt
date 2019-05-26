package org.firstinspires.ftc.teamcode.debug

import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.firstinspires.ftc.teamcode.internal.BaseOpMode
import org.firstinspires.ftc.teamcode.internal.units.IMU

/**
 * 这是用来整体调试的程序，相比与手动程序会打印更多有价值的东西
 */
@TeleOp(name = "调试", group = "Debug")
class Debug: BaseOpMode(true, true, false, false,
        true, true, true) {

    override fun run() {
        waitForStart()
        while (opModeIsActive()) {
            chassis.setPowerByGamepad()
            chassis.autoDirectionChange()
            lift.autoSetPower()
            arm.setArmByGamepad()
            collect.autoSetAll()
            val angle = imu.getAngle()
            telemetry.addLine("IMU")
                    .addData("x", String.format("%.1f", angle[0]))
                    .addData("y", String.format("%.1f", angle[1]))
                    .addData("y_Auto", String.format("%.2f", imu.getAngle(IMU.Direction.Y)+90))
                    .addData("z", String.format("%.1f", angle[2]))
            telemetry.update()
        }
    }

}