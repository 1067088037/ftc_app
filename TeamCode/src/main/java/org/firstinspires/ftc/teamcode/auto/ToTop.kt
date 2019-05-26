package org.firstinspires.ftc.teamcode.auto

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.util.ElapsedTime
import org.firstinspires.ftc.teamcode.internal.BaseOpMode
import org.firstinspires.ftc.teamcode.internal.units.IMU

/**
 * 这个程序的功能是控制底盘上升。
 * 当底盘完全到顶时，程序会自动跳出防止马达持续堵转。
 * 完全理解这个程序的功能需要明白我们机器人的升降原理，即丝杆控制整个底盘升降。
 */
@Autonomous(name = "升降到顶", group = "Auto")
class ToTop: BaseOpMode(false, true, false, false,
        false, false, false) {

    override fun run() {
        waitForStart()
        if (opModeIsActive()) lift.toTop()
    }

}