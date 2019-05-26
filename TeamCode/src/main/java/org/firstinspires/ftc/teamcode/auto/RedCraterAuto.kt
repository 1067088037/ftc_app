package org.firstinspires.ftc.teamcode.auto

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.Disabled
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.util.ElapsedTime
import org.firstinspires.ftc.teamcode.internal.BaseOpMode

/**
 * 已经弃用的自动程序
 */
@Deprecated("")
@Disabled
@Autonomous(name = "红方 - 矿坑", group = "Auto")
class RedCraterAuto: BaseOpMode(true, true, false, true,
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
            chassis.setStraightByEncoder(500, 2000)
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
            0, 1 -> {
                chassis.setTransverseByEncoder(-175, 1500)
                chassis.setStraightByEncoder(1200, 2000)
                chassis.setStraightByEncoder(-1200, 2500)
                chassis.setAngle(180.0, 2500)
            }//左边或没有找到
            2 -> {
                chassis.setAngle(180.0, 3000)
                chassis.setStraightByEncoder(-1500, 2000)
            }//中间
            3 -> {
                chassis.setTransverseByEncoder(150, 1500)
                chassis.setStraightByEncoder(1200, 2000)
                chassis.setAngle(135.0, 2500)
                chassis.setStraightByEncoder(500, 1500)
                chassis.setTransverseByEncoder(-300, 1500)
                chassis.setAngle(135.0, 1000)
                chassis.setStraightByEncoder(-1500, 3000)
            }//右边
        }
    }

}