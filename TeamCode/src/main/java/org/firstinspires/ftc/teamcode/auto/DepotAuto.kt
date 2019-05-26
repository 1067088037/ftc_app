package org.firstinspires.ftc.teamcode.auto

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.util.ElapsedTime
import org.firstinspires.ftc.teamcode.internal.BaseOpMode
import org.firstinspires.ftc.teamcode.internal.units.TensorFlow

/**
 * 这是机器人的自动程序。
 * 今年的自动程序编写如果配上REV新出的 2M Distance Sensor 将会变得非常容易，各位队伍可能尝试使用。
 * 但是作为一个勤俭节约的队伍（穷），我们没有使用任何外置传感器，而是将就现有的装备完成艰苦卓绝的奋斗。
 * 自动的原理大致是：REV主控器自带的陀螺仪+PID算法控制转向，编码器+PID算法控制行进，TensorFlow识别金矿石。
 * 以下自动程序适配的位置是营地，即大家所说的辅位。
 */
@Autonomous(name = "<<<自动-营地>>>", group = "Auto")
class DepotAuto: BaseOpMode(true, true, false, true,
        true, true, true) {

    override fun run() {
        var goldMineralPosition = TensorFlow.GoldMineralPosition.Unknown//用来记录金矿石位置
        var tensorFlowStatus = true//TF状态
        var closeTo = false//接近金矿石

        //获取金矿石位置
        Thread {
            tensorFlow.activate()//启动TF
            while (tensorFlowStatus) {
                goldMineralPosition = tensorFlow.getGoldPositionNew()//识别位置
                if (goldMineralPosition != TensorFlow.GoldMineralPosition.Unknown && opModeIsActive()) break//找到跳出
                telemetryLog("矿石:$goldMineralPosition")//回传位置
                Thread.sleep(100)
            }
            tensorFlowStatus = false
            tensorFlow.deactivate()//关闭TF
            telemetryLog("矿石:$goldMineralPosition")//回传识别到的位置
        }.start()

        waitForStart()
        welcome()//播放欢迎提示音

        //脱离挂钩
        while (opModeIsActive()) {
            lift.toTopForAuto()//升起底盘
            chassis.setTransverseByEncoder(200, 1000)//脱钩
            //撞击矿石
            Thread {
                chassis.setAngle(0.0, 1000)//校准方向
                chassis.setStraightByEncoder(150, 1000)//编码器行进
                chassis.setAngle(0.0, 1500)
                when (goldMineralPosition) {
                    TensorFlow.GoldMineralPosition.Left -> {
                        chassis.setTransverseByEncoder(-400, 2000)
                        chassis.setAngle(0.0, 1000)
                        chassis.setObliqueByEncoder(2200, false)
                    }
                    TensorFlow.GoldMineralPosition.Unknown, TensorFlow.GoldMineralPosition.Center -> {
                        chassis.setTransverseByEncoder(-100, 1000)
                        chassis.setAngle(0.0, 1000)
                        Thread.sleep(500)
                        chassis.setStraightByEncoder(1250)
                    }
                    TensorFlow.GoldMineralPosition.Right -> {
                        chassis.setTransverseByEncoder(300, 1000)
                        chassis.setAngle(0.0, 1000)
                        chassis.setObliqueByEncoder(2200, true)
                    }
                }//根据已经判断完成的左中右撞击金矿石
                chassis.setAngle(0.0, 750)//校准角度
                closeTo = true//已经完成接近
            }.start()
            val success = lift.toBottomForAuto()//降下底盘
            chassis.setAngle(0.0, 750)//修正角度
            if (success) break//成功跳出
        }

        tensorFlowStatus = false
        telemetryLog("矿石:$goldMineralPosition")
        while (!closeTo && opModeIsActive()) {
            Thread.sleep(10)
        }//反正没有撞击完成金矿石就提前返航

        when (goldMineralPosition) {
            TensorFlow.GoldMineralPosition.Left -> {
                chassis.setStraightByEncoder(-750)
                chassis.setTransverseByEncoder(-800, 1000)
            }
            TensorFlow.GoldMineralPosition.Unknown, TensorFlow.GoldMineralPosition.Center -> {
                chassis.setStraightByEncoder(-600)
                chassis.setTransverseByEncoder(-1500, 2000)
            }
            TensorFlow.GoldMineralPosition.Right -> {
                chassis.setStraightByEncoder(-750)
                chassis.setTransverseByEncoder(-2500, 2500)
            }
        }

        //放置吉祥物和停靠
        if (opModeIsActive()) {
            chassis.setAngle(-135.0+360, 2000)//转向
            chassis.setTargetByTime(0.0, 0.0, 1.0, 1750)//向左平移
            chassis.setAngle(-135.0+360, 500)//转向
            chassis.setTransverseByEncoder(-300, 1000)
            chassis.setAngle(-135.0+360, 500)//转向
            Thread {
                arm.outForAuto()//弹出机械臂
            }.start()
            chassis.setStraightByEncoder(-1300, 3000)//后退到营地
            collect.outForAuto()//释放吉祥物
            //这个位置的机器人放弃停靠矿坑的得分
//            Thread {
//                arm.middleForAuto()//中置机械臂
//            }.start()
//            chassis.setAngle(-135.0+360, 1000)//转向
//            chassis.setStraightByEncoder(3000)//停靠矿坑
        }

        tensorFlowStatus = false//再次确认关闭TF
    }

}