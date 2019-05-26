package org.firstinspires.ftc.teamcode.auto

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.Disabled
import com.qualcomm.robotcore.util.ElapsedTime
import org.firstinspires.ftc.teamcode.internal.BaseOpMode

/**
 * 已经弃用的自动程序
 */
@Deprecated("")
@Disabled
@Autonomous(name = "红方 - 营地", group = "Auto")
class RedDepotAuto: BaseOpMode(true, true, false, true,
        true, true, true) {

    override fun run() {
        var outStatus = -1//脱钩状态，-1暂停，0正在执行，1成功，2失败
        val time = ElapsedTime()
//        chassis.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE)//开启刹车便于控制
        waitForStart()
        welcome()
        val toBottom = Thread {
            while (opModeIsActive()) {
                if (outStatus == 0) {
                    outStatus = if (lift.toBottomForAuto()) 1 else 2
                    if (outStatus == 1) {
                        tensorFlow.activate()
                        break
                    }
                }
            }
        }
        toBottom.start()
//        tensorFlow.activate()
        //脱离挂钩
        while (opModeIsActive()) {
            lift.toTopForAuto()//升起底盘
            chassis.setTransverseByEncoder(200, 1000)//脱钩
            chassis.setAngle(0.0, 500)
            outStatus = 0//降下底盘
            chassis.setStraightByEncoder(500,2000)
            chassis.setTransverseByEncoder(-150, 1000)
            while (outStatus == 0) {
                Thread.sleep(10)
            }//等待结束
            if (outStatus == 1) {
                break
            }
            if (outStatus == 2) {
                outStatus = -1
            }
        }
        //撞金矿石
        val gold = if (opModeIsActive()) chassis.searchGoldMineral() else 0
        when (gold) {
            0,1 -> {
                chassis.setStraightByEncoder(1400, 3000)
                Thread {
                    arm.outForAuto()
                }.start()
                chassis.setAngle(-135.0+360.0, 3000)
                chassis.setStraightByEncoder(-500, 1000)
                Thread {
                    chassis.setAngle(-135.0+360.0, 1000)
                }.start()
                collect.outForAuto()
                Thread {
                    arm.middleForAuto()
                }.start()
            }//左边或没有找到
            2 -> {
                chassis.setStraightByEncoder(1200, 2000)
                Thread {
                    arm.outForAuto()
                }.start()
                chassis.setAngle(180.0, 3000)
                collect.outForAuto()
                Thread {
                    arm.middleForAuto()
                }.start()
            }//中间
            3 -> {
                chassis.setStraightByEncoder(1450, 3000)
                Thread {
                    arm.outForAuto()
                }.start()
                chassis.setAngle(135.0, 3000)
                chassis.setStraightByEncoder(-450, 1000)
                Thread {
                    chassis.setTransverseByEncoder(-250, 1500)
                }.start()
                collect.outForAuto()
                Thread {
                    arm.middleForAuto()
                }.start()
                chassis.setStraightByEncoder(3000, 5000)
            }//右边
        }
    }

}