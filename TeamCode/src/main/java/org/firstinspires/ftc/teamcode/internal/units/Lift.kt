package org.firstinspires.ftc.teamcode.internal.units

import com.qualcomm.ftcrobotcontroller.R
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.util.ElapsedTime
import org.firstinspires.ftc.teamcode.internal.config.DcMotorInfo

/**
 * 这是升降控制类。
 * UST的升降机械结构堪称是全场最特别的，也因此获得了成都赛“创新奖”，重庆赛“设计奖”。
 */
class Lift: Unit() {

    /**
     * 马达硬件
     */
    var leftLiftMotor: DcMotor = DcMotorInfo.buildMotor(config().LEFT_LIFT_MOTOR)
    var rightLiftMotor: DcMotor = DcMotorInfo.buildMotor(config().RIGHT_LIFT_MOTOR)

    private val pid = PID(config().LIFT_PID, config().LIFT_BLIND)
    var correctON = true
    var autoToPosition = false//自动到位
    private var lowestPostion = Int.MAX_VALUE
    private val liftRunTime = ElapsedTime()

    //构造函数
    init {

    }

    /**
     * 设置升降功率
     */
    fun setPower(power: Double) {
        val correct = if (correctON) {
            correct()
        } else 0.0
//        telemetry().addData("校正", correct)
        leftLiftMotor.power = power + correct
        rightLiftMotor.power = power - correct
    }

    /**
     * 自动设置功率
     */
    fun autoSetPower(): Boolean {
        lowestPostion = Math.min(lowestPostion, leftLiftMotor.currentPosition)
        return when {
            config().liftUp() -> {
                setPower(1.0)
                autoToPosition = false
                true
            }
            config().liftDown() -> {
                setPower(-1.0)
                autoToPosition = false
                true
            }
            else -> {
                if (!autoToPosition) {
                    if (config().autoToTop()) {
                        autoToPosition = true
                        Thread {
                            fun getPosition(): Int = leftLiftMotor.currentPosition - lowestPostion
                            var lastPosition = leftLiftMotor.currentPosition
                            correctON = true
                            setPower(1.0)
                            if (liftRunTime.seconds() >= 1) {
                                opMode().sound(R.raw.lift_up)
                                liftRunTime.reset()
                            }
                            while (opModeIsActive() && autoToPosition) {
                                Thread.sleep(50)
                                setPower(1.0)
                                if (Math.abs(leftLiftMotor.currentPosition-lastPosition) <= 12.5) break
                                if (getPosition() >= config().LIFT_HIGHUP_POSITION) break
                                lastPosition = leftLiftMotor.currentPosition
                                if (config().autoToBottom()) break
                            }
                            opMode().sound(R.raw.lift_end)
                            setPower(0.0)
                            autoToPosition = false
                        }.start()
                    } else if (config().autoToBottom()) {
                        autoToPosition = true
                        Thread {
                            var lastPosition = leftLiftMotor.currentPosition
                            correctON = true
                            setPower(-1.0)
                            if (liftRunTime.seconds() >= 1) {
                                opMode().sound(R.raw.lift_down)
                                liftRunTime.reset()
                            }
                            while (opModeIsActive() && autoToPosition) {
                                Thread.sleep(50)
                                setPower(-1.0)
                                if (Math.abs(leftLiftMotor.currentPosition-lastPosition) <= 12.5) break
                                lastPosition = leftLiftMotor.currentPosition
                                if (config().autoToTop()) break
                            }
                            opMode().sound(R.raw.lift_end)
                            setPower(0.0)
                            autoToPosition = false
                        }.start()
                    } else {
                        autoToPosition = false
                    }
                }
                if (!autoToPosition) setPower(0.0)
                false
            }
        }
    }

    /**
     * 查错
     */
    fun debug() {
        telemetry().addLine("升降")
                .addData("左", leftLiftMotor.currentPosition)
                .addData("右", rightLiftMotor.currentPosition)
                .addData("差", leftLiftMotor.currentPosition - rightLiftMotor.currentPosition)
    }

    /**
     * 到顶
     */
    fun toTop() {
        if (!opModeIsActive()) return
        var lastPosition = leftLiftMotor.currentPosition
        correctON = true
        setPower(1.0)
        while (opModeIsActive()) {
            Thread.sleep(50)
            setPower(1.0)
            if (Math.abs(leftLiftMotor.currentPosition-lastPosition) <= 12.5) {
                break
            }
            lastPosition = leftLiftMotor.currentPosition
        }
        correctON = false
        setPower(0.0)
    }

    /**
     * 自动阶段到顶（不完全到顶）
     */
    fun toTopForAuto() {
        if (!opModeIsActive()) return
        var lastPosition = leftLiftMotor.currentPosition
        correctON = true
        setPower(1.0)
        while (opModeIsActive()) {
            Thread.sleep(50)
            setPower(1.0)
            if (Math.abs(leftLiftMotor.currentPosition-lastPosition) <= 12.5) {
                break
            }//卡住跳出
            if (opMode().runIMU) {
                if (Math.abs(opMode().imu.getAngle(IMU.Direction.Y)+90) <= config().LANDING_ANGLE) {
                    Thread.sleep(10)
                    break
                }
            }//陀螺仪控制
            if (leftLiftMotor.currentPosition >= config().LIFT_HIGHUP_POSITION) {
                break
            }//编码器控制
            lastPosition = leftLiftMotor.currentPosition
        }
        correctON = false
        setPower(0.0)
    }

    /**
     * 到底
     */
    fun toBottom() {
        if (!opModeIsActive()) return
        var lastPosition = leftLiftMotor.currentPosition
        correctON = true
        setPower(-1.0)
        while (opModeIsActive()) {
            Thread.sleep(50)
            setPower(-1.0)
            if (Math.abs(leftLiftMotor.currentPosition-lastPosition) <= 12.5) {
                break
            }
            lastPosition = leftLiftMotor.currentPosition
        }
        correctON = false
        setPower(0.0)
    }

    /**
     * 自动到底
     */
    fun toBottomForAuto(): Boolean {
        if (!opModeIsActive()) return false
        if (!opMode().runIMU) return false
        var lastPosition = leftLiftMotor.currentPosition
        correctON = true
        setPower(-1.0)
        while (opModeIsActive()) {
            Thread.sleep(50)
            setPower(-1.0)
            if (Math.abs(opMode().imu.getAngle(IMU.Direction.Y)+90) >= config().SUSPENSION_ANGLE) {
                setPower(0.0)
                return false//升降失败
            }
            if (Math.abs(leftLiftMotor.currentPosition - lastPosition) <= 12.5) {
                correctON = false
                setPower(0.0)
                return true
            }//正确停止
            lastPosition = leftLiftMotor.currentPosition
        }
        return false
    }

    /**
     * 校正高度
     */
    private fun correct(): Double {
        val err = leftLiftMotor.currentPosition - rightLiftMotor.currentPosition
        val power = pid.run(err.toDouble(), 0.0)
//        pid.debug(opMode())
        return Math.signum(power) * Math.min(Math.abs(power), config().LIFT_CORRECT_MAXPOWER)
    }

}