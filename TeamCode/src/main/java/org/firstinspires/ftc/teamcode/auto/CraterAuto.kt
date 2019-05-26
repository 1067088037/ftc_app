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
 * 以下自动程序适配的位置是矿坑，即大家所说的主位。
 */
@Autonomous(name = "<<<自动-矿坑>>>", group = "Auto")
class CraterAuto: BaseOpMode(true, true, false, true,
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
                chassis.setAngle(0.0, 1000)
                when (goldMineralPosition) {
                    TensorFlow.GoldMineralPosition.Left -> {
                        chassis.setTransverseByEncoder(-300, 1500)
                        chassis.setAngle(0.0, 1500)
                        chassis.setObliqueByEncoder(2200, false)
                    }
                    TensorFlow.GoldMineralPosition.Unknown, TensorFlow.GoldMineralPosition.Center -> {
                        chassis.setTransverseByEncoder(-100, 1000)
                        chassis.setAngle(0.0, 1500)
                        Thread.sleep(500)
                        chassis.setStraightByEncoder(1200)
                    }
                    TensorFlow.GoldMineralPosition.Right -> {
                        chassis.setTransverseByEncoder(300, 1500)
                        chassis.setAngle(0.0, 1500)
                        chassis.setObliqueByEncoder(2200, true)
                    }
                }//根据已经判断完成的左中右撞击金矿石
                chassis.setAngle(0.0, 750)//校准方向
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

        //放回并准备放置吉祥物
        if (opModeIsActive()) when (goldMineralPosition) {
            TensorFlow.GoldMineralPosition.Left -> {
                chassis.setStraightByEncoder(-450)
                chassis.setTransverseByEncoder(-1000, 1500)
            }
            TensorFlow.GoldMineralPosition.Unknown, TensorFlow.GoldMineralPosition.Center -> {
                chassis.setStraightByEncoder(-500)
                chassis.setTransverseByEncoder(-1700, 2500)
            }
            TensorFlow.GoldMineralPosition.Right -> {
                chassis.setStraightByEncoder(-450)
                chassis.setTransverseByEncoder(-2700, 3000)
            }
        }

        //放置吉祥物和停靠
        if (opModeIsActive()) {
            chassis.setAngle(45.0, 1500)//转向
            chassis.setTargetByTime(0.0, 0.0, -1.0,1500)
            chassis.setAngle(45.0, 500)
            chassis.setTransverseByEncoder(250, 1000)
            Thread {
                arm.outForAuto()//弹出机械臂
            }.start()
            chassis.setAngle(45.0, 500)
            chassis.setStraightByEncoder(-1300, 2500)
            collect.outForAuto()//释放吉祥物
            Thread {
                arm.middleForAuto()
            }.start()//举起机械臂
            chassis.setAngle(45.0, 750)
            chassis.setStraightByEncoder(2500, 4500)//停靠矿坑
        }

        tensorFlowStatus = false//再次确认关闭TF
    }

}