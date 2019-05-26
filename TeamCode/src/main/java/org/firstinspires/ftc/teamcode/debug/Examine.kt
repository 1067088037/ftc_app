package org.firstinspires.ftc.teamcode.debug

import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.util.ElapsedTime
import org.firstinspires.ftc.teamcode.R
import org.firstinspires.ftc.teamcode.internal.BaseOpMode
import org.firstinspires.ftc.teamcode.internal.config.DcMotorInfo
import org.firstinspires.ftc.teamcode.internal.units.IMU

/**
 * 这是一个神奇的程序，用于赛前检查。
 * 程序运行时会通过语音提示的形式帮助队员检查机器人是否存在问题。
 * 检查的部分共有6个，分别是底盘、升降、TensorFlow、IMU、收集、机械臂。
 * 合理使用此程序可以检查出大部分常见问题（甚至包括结构问题）。
 */
@TeleOp(name = "赛前检查", group = "Debug")
class Examine: BaseOpMode(true, true, false, true,
        true, true, true) {

    private var runTime: ElapsedTime = ElapsedTime()
    private val size = 6
    private val success = BooleanArray(size)

    override fun run() {
        gamepadMonitor = true
        waitForStart()
        if (opModeIsActive()) {
            telemetryLog("开始赛前检查")
            sound(R.raw.pregame_inspection)
            sound(R.raw.x_and_y_keys)
            sleep(6000)
            var successTimes = 0
            for (i in 1..size) {
                if (opModeIsActive().not()) return
                if (choose(i)) successTimes++
                sleep(100)
            }
            sleep(1500)
            end(successTimes)
        }
    }

    private fun end(successTimes: Int) {
        fun successToText(i: Int): String = if (success[i]) "成功" else "失败"
        telemetry.addData("测试结果", "共${size}项，有${successTimes}项成功")
        telemetry.addData("底盘和编码器", successToText(0))
        telemetry.addData("升降和编码器", successToText(1))
        telemetry.addData("TensorFlow", successToText(2))
        telemetry.addData("IMU", successToText(3))
        telemetry.addData("收集", successToText(4))
        telemetry.addData("机械臂", successToText(5))
        telemetry.update()
        if (successTimes == size) sound(R.raw.success)
        else sound(R.raw.fault)
        waitForEnd()
    }

    //选择运行的检查
    private fun choose(index: Int): Boolean {
        return when (index) {
            1 -> testChassisEncoder()
            2 -> testLiftEncoder()
            3 -> testTF()
            4 -> testIMU()
            5 -> testCollect()
            6 -> testArm()
            else -> false
        }
    }

    //检测底盘编码器
    private fun testChassisEncoder(): Boolean {
        val name = "底盘和编码器"
        beginToTip(name, "请保证机器人置于场地中且右前方0.5米内无异物")
        sound(R.raw.test_chassis)
        while (true) {
            if (opModeIsActive().not() || gamepad1.x) {
                sleep(1000)
                return false
            }
            if (gamepad1.y) break
        }

        //编码器
        chassis.resetEncoder()
        chassis.setPower(0.5, 0.5)
        sleep(500)
        chassis.setPower(0.0, 0.0)
        fun success(motor: DcMotor): Boolean {
            return Math.abs(motor.currentPosition) >= 50
        }
        val success1 = success(chassis.frontLeftMotor) && success(chassis.frontRightMotor)
                && success(chassis.rearLeftMotor) && success(chassis.rearRightMotor)
        tellSuccess(success1)
        chassis.telemetryPosition()
        telemetry.update()
        sleep(1500)

        //前进
        chassis.resetEncoder()
        chassis.setStraightByEncoder(500, 1500)
        val success2 = Math.abs(chassis.frontLeftMotor.currentPosition-500) <= 10
        tellSuccess(success2)
        chassis.telemetryPosition()
        telemetry.update()
        sleep(1500)

        //平移
        chassis.resetEncoder()
        chassis.setTransverseByEncoder(500, 1500)
        val success3 = Math.abs(chassis.frontLeftMotor.currentPosition-500) <= 10
        tellSuccess(success3)
        chassis.telemetryPosition()
        telemetry.update()
        sleep(2000)

        success[0] = success1 && success2 && success3
        return success[0]
    }

    //检测升降
    private fun testLiftEncoder(): Boolean {
        val name = "升降和编码器"
        beginToTip(name, "请保证电池不会被卡住")
        sound(R.raw.test_lift)
        while (true) {
            if (opModeIsActive().not() || gamepad1.x) {
                sleep(1000)
                return false
            }
            if (gamepad1.y) break
        }

        //升降
        DcMotorInfo.resetEncoder(lift.leftLiftMotor)
        DcMotorInfo.resetEncoder(lift.rightLiftMotor)
        lift.setPower(1.0)
        sleep(800)
        lift.setPower(0.0)
        success[1] = (Math.abs(lift.leftLiftMotor.currentPosition) >= 100) && (Math.abs(lift.rightLiftMotor.currentPosition) >= 100)
        tellSuccess(success[1])
        telemetry.addData("左编码器数值", lift.leftLiftMotor.currentPosition)
        telemetry.addData("右编码器数值", lift.rightLiftMotor.currentPosition)
        telemetry.update()
        if (success[1]) {
            lift.toBottom()
        } else {
            lift.setPower(-1.0)
            sleep(1000)
            lift.correctON = false
            lift.setPower(0.0)
        }
        sleep(2000)
        return success[1]
    }

    //测试TF
    private fun testTF(): Boolean {
        val name = "TensorFlow"
        beginToTip(name, "请保证摄像头权限开启，并把金矿石放在摄像头前")
        sound(R.raw.test_tensor_flow)
        while (true) {
            if (opModeIsActive().not() || gamepad1.x) {
                sleep(1000)
                return false
            }
            if (gamepad1.y) break
        }

        //TF
        tensorFlow.activate()
        runTime.reset()
        var position = DoubleArray(2)
        while (opModeIsActive() && runTime.seconds() <= 10) {
            position = tensorFlow.getGoldPosition()
            if (position[0] > 0.0) {
                success[2] = true
                break
            } else success[2] = false
            Thread.sleep(100)
            telemetryLog("剩余时间:${String.format("%.0f", 10-runTime.seconds())}秒")
        }
        tellSuccess(success[2])
        telemetry.addData("位置:", "x ${position[0]},y ${position[1]}")
        telemetry.update()
        tensorFlow.deactivate()
        Thread.sleep(2000)
        return success[2]
    }

    //陀螺仪
    private fun testIMU(): Boolean {
        val name = "IMU"
        beginToTip(name, "即将旋转底盘，请小心")
        sound(R.raw.test_imu)
        while (true) {
            if (opModeIsActive().not() || gamepad1.x) {
                sleep(1000)
                return false
            }
            if (gamepad1.y) break
        }

        //陀螺仪
        val startAngle = imu.getAngle(IMU.Direction.X)
        chassis.setAngle(90.0, 2000)
        success[3] = Math.abs(imu.getAngle(IMU.Direction.X) - startAngle - 90) <= 3
        tellSuccess(success[3])
        telemetry.update()
        Thread.sleep(2000)
        return success[3]
    }

    //测试收集
    private fun testCollect(): Boolean {
        val name = "收集"
        beginToTip(name, "请手动测试")
        sound(R.raw.test_collect)
        while (true) {
            if (opModeIsActive().not() || gamepad1.x) {
                success[4] = false
                sleep(1000)
                return false
            }
            if (gamepad1.y) break
        }

        //收集
        while (opModeIsActive()) {
            telemetryLog("按b键停止，默认成功")
            if (gamepad1.b) break
            collect.autoSetAll()
        }
        success[4] = true
        return success[4]
    }

    //测试机械臂
    private fun testArm(): Boolean {
        val name = "机械臂"
        beginToTip(name, "请保证机械臂没有卡住")
        sound(R.raw.test_arm)
        while (true) {
            if (opModeIsActive().not() || gamepad1.x) {
                sleep(1000)
                return false
            }
            if (gamepad1.y) break
        }

        //机械臂
        DcMotorInfo.resetEncoder(arm.armUpMotor)
        arm.setPower(-0.5)
        Thread.sleep(750)
        arm.setPower(0.0)
        success[5] = Math.abs(arm.armUpMotor.currentPosition) >= 100
        tellSuccess(success[5])
        telemetry.addData("编码器数值", arm.armUpMotor.currentPosition)
        telemetry.update()
        Thread.sleep(2000)
        return success[5]
    }

    //告知是否成功
    private fun tellSuccess(success: Boolean) {
        if (success) {
            telemetry.addData("状态", "成功")
            sound(R.raw.success)
        } else {
            telemetry.addData("状态", "失败")
            sound(R.raw.fault)
        }
    }

    //开始提示
    private fun beginToTip(name: String, warning: String = "请注意安全") {
        telemetryLog("按[Y]键开始[" + name + "]检查" +
                "\n按[X]键跳过此项检查" +
                "\n提示:" + warning)
    }

}