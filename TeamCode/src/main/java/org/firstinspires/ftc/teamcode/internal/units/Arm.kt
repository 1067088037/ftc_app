package org.firstinspires.ftc.teamcode.internal.units

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.Servo
import com.qualcomm.robotcore.util.ElapsedTime
import org.firstinspires.ftc.teamcode.internal.config.DcMotorInfo
import org.firstinspires.ftc.teamcode.internal.config.ServoInfo

/**
 * 这是机械臂控制类。
 * 我们的机械臂在场上的任务是将矿石从矿区投掷到着陆器中。
 */
class Arm: Unit() {

    /**
     * 机械臂的硬件
     */
    var armUpMotor: DcMotor = DcMotorInfo.buildMotor(config().ARM_UP_MOTOR)
    var armDownMotor: DcMotor = DcMotorInfo.buildMotor(config().ARM_DOWN_MOTOR)
    private var ejectServo: Servo = ServoInfo.buildServo(config().ARM_EJECT)

    /**
     * 其他需要的变量
     */
    private var ejectON = false//弹出机械臂
    private var time = ElapsedTime()
    val pid = PID(config().ARM_PID, config().ARM_BLIND)
    private var change = 0.0

    /**
     * 这个线程用于实时监测机械臂旋转的速度
     * 用一个线程每隔50ms监测一次与上次的差距，即可估算大致的运行速度
     */
    private val armSpeed = Thread {
        var lastPosition = armUpMotor.currentPosition
        opMode().waitForStart()
        while (opModeIsActive()) {
            change = (armUpMotor.currentPosition - lastPosition).toDouble()
            lastPosition = armUpMotor.currentPosition
            Thread.sleep(50)
        }
    }

    init {
        time.reset()
        armSpeed.start()
//        setPosition.start()
//        getSpeed.start()
    }

    /**
     * 设置功率
     */
    fun setPower(power: Double) {
        armUpMotor.power = power
        armDownMotor.power = power
    }

    /**
     * 设置零功率行为
     */
    private fun setZeroPowerBehaviour(value: DcMotor.ZeroPowerBehavior) {
        armUpMotor.zeroPowerBehavior = value
        armDownMotor.zeroPowerBehavior = value
    }

    /**
     * 通过手柄控制功率
     */
    fun setArmByGamepad() {
        ejectDebug()
        easy()
    }

    /**
     * 最后选定的PID调速方案
     */
    private fun easy() {
        val power = if (Math.abs(gamepad1().right_stick_y) <= 0.05) {
            setZeroPowerBehaviour(DcMotor.ZeroPowerBehavior.BRAKE)
            0.0
        } else {
            setZeroPowerBehaviour(DcMotor.ZeroPowerBehavior.FLOAT)
            val target = -gamepad1().right_stick_y.toDouble()*config().speedK
            val power = pid.run(change, if (target > 0) target else target*0.75)
            Math.signum(power) * Math.min(Math.abs(power), config().ARM_MAX_POWER)
        }
        setPower(power)
    }

    /**
     * 自动时弹出机械臂
     */
    fun outForAuto() {
        setZeroPowerBehaviour(DcMotor.ZeroPowerBehavior.BRAKE)
        while (opModeIsActive()) {
            if (Math.abs(armUpMotor.currentPosition) <= config().ARM_MAX_POSITION) {
                setPower(-0.5)
            } else {
                setPower(0.0)
                break
            }
            opMode().telemetryLog(armUpMotor.currentPosition)
            Thread.sleep(10)
        }
    }

    /**
    * 抬升到中间位置
    */
    fun middleForAuto() {
        setZeroPowerBehaviour(DcMotor.ZeroPowerBehavior.BRAKE)
        while (opModeIsActive()) {
            if (Math.abs(armUpMotor.currentPosition) >= config().ARM_MIDDLE_POSITION) {
                setPower(+0.5)
            } else {
                setPower(0.0)
                break
            }
            opMode().telemetryLog(armUpMotor.currentPosition)
            Thread.sleep(10)
        }
    }

    /**
     * 设置弹出
     */
    fun setEject(position: Double) {
        ejectServo.position = position
    }

    /**
     * 弹出调试
     */
    fun ejectDebug() {
        if (ejectON) {
            setEject(config().ARM_EJECT_OUT)
        } else {
            setEject(0.0)
        }
        if (time.milliseconds() >= 250) {
            if (config().ejectDebug()) {
                ejectON = ejectON.not()
                time.reset()
            }
        }
    }

}