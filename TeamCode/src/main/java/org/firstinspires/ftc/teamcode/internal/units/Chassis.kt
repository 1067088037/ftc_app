package org.firstinspires.ftc.teamcode.internal.units

import com.qualcomm.ftcrobotcontroller.R
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.util.ElapsedTime
import org.firstinspires.ftc.teamcode.internal.config.DcMotorInfo
import kotlin.math.log

/**
 * 这是底盘控制类。
 * 底盘是一台机器人最重要的组件之一，因此如你所见这个类的体积非常庞大。
 */
class Chassis: Unit() {

    /**
     * 底盘马达硬件
     */
    var frontLeftMotor: DcMotor = DcMotorInfo.buildMotor(config().FRONT_LEFT_MOTOR)//左前方
    var rearLeftMotor: DcMotor = DcMotorInfo.buildMotor(config().REAR_LEFT_MOTOR)//左后方
    var frontRightMotor: DcMotor = DcMotorInfo.buildMotor(config().FRONT_RIGHT_MOTOR)//右前方
    var rearRightMotor: DcMotor =  DcMotorInfo.buildMotor(config().REAR_RIGHT_MOTOR)//右后方

    //陀螺仪的PID
    private val pidIMU = PID(config().CHASSIS_ROTATE_PID, config().CHASSIS_ROTATE_BLIND)
    private var lastAngleAim = 0.0
    //TF的PID
    private val pidTF = PID(config().CHASSIS_TF_PID, config().CHASSIS_TF_BLIND)
    //编码器的PID
    private val pidEncoder = PID(config().CHASSIS_ENCODER_PID, config().CHASSIS_ENCODER_BLIND)

    //方向是否全部改变
    var directionAllChanged = false
    private val time = ElapsedTime()

    //构造函数
    init {
        time.reset()
    }

    /**
     * 设置底盘功率
     */
    fun setPower(leftPower: Double/*左马达功率*/, rightPower: Double/*右马达功率*/, transversePower: Double = 0.0/*平移功率*/) {
        if (directionAllChanged) {//这里的判断用于进行底盘反向操作
            frontLeftMotor.power = rightPower + transversePower
            frontRightMotor.power = leftPower - transversePower
            rearLeftMotor.power = rightPower - transversePower
            rearRightMotor.power = leftPower + transversePower
        } else {
            frontLeftMotor.power = leftPower + transversePower
            frontRightMotor.power = rightPower - transversePower
            rearLeftMotor.power = leftPower - transversePower
            rearRightMotor.power = rightPower + transversePower
        }
    }

    /**
     * 逐一设置马达的功率
     */
    fun setPowerOneByOne(power: DoubleArray) {
        frontLeftMotor.power = power[0]
        frontRightMotor.power = power[1]
        rearLeftMotor.power = power[2]
        rearRightMotor.power = power[3]
    }

    /**
     * 通过手柄设置底盘功率
     */
    fun setPowerByGamepad() {
        val gamepad = gamepad1()//选择所使用的手柄
        setPower(gamepad.left_stick_x*config().CHASSIS_TURN_K - gamepad.left_stick_y.toDouble(),
                -gamepad.left_stick_x*config().CHASSIS_TURN_K - gamepad.left_stick_y.toDouble(),
                -gamepad.left_trigger + gamepad.right_trigger.toDouble())
    }

    /**
     * 自动寻找金矿石，在第二代的自动中没有使用
     */
    fun searchGoldMineral(): Int {
        if (!opMode().runTensorFlow) return -1
        val tensorFlow = opMode().tensorFlow
        val time = ElapsedTime()
        tensorFlow.activate()
        time.reset()
        setAngle(0.0, 1500)

        //成功识别
        fun success(): Boolean {
            val position = tensorFlow.getGoldPosition()
            val x = position[0]
            val y = position[1]
            return (y in config().ImageHeight.toDouble()*0.25..config().ImageHeight.toDouble()*0.75) &&
                    (x > config().ImageWidth.toDouble()*0.50)
        }

        return if (success()) {
            tensorFlow.deactivate()
            2//中间
        } else {
            setAngle(45.0, 2000)
            if (success()) {
                tensorFlow.deactivate()
                3//右边
            } else {
                setAngle(-45.0, 2000)
                if (success()) {
                    tensorFlow.deactivate()
                    1//左边
                } else {
                    tensorFlow.deactivate()
                    0//没有
                }
            }
        }
    }

    /**
     * 延时控制，时间万岁
     */
    fun setTargetByTime(leftPower: Double, rightPower: Double, transversePower: Double = 0.0, milliSeconds: Long) {
        val startMS = System.currentTimeMillis()
        while (opModeIsActive() && System.currentTimeMillis()-startMS < milliSeconds) {
            setPower(leftPower, rightPower, transversePower)
        }
        setPower(0.0, 0.0, 0.0)
    }

    /**
     * 用编码器控制行进
     */
    fun setStraightByEncoder(target: Int, outTime: Int = 2500) {
        setTargetByEncoder(true, target, outTime)
    }

    /**
     * 用编码器控制斜向45°前进
     */
    fun setObliqueByEncoder(target: Int, right: Boolean, outTime: Int = 3000) {
        val zeroPowerBehavior: DcMotor.ZeroPowerBehavior = frontLeftMotor.zeroPowerBehavior
        val aim = if (right) {
            target + frontLeftMotor.currentPosition
        } else {
            target + frontRightMotor.currentPosition
        }
        setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT)
        if (right) {
            frontRightMotor.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
            rearLeftMotor.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        } else {
            frontLeftMotor.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
            rearRightMotor.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        }
//        opMode().telemetryLog("实际:${frontLeftMotor.currentPosition},目标:${aim}")
        setPower(0.0, 0.0)
        var finished = 0
        val startTime = System.currentTimeMillis()
        while (opModeIsActive() && System.currentTimeMillis()-startTime<outTime) {
            if (right) {
                //向右
                var power = pidEncoder.run(frontLeftMotor.currentPosition.toDouble(), aim.toDouble())
                power = Math.signum(power) * Math.min(Math.abs(power), config().CHASSIS_MAX_POWER)
                setPowerOneByOne(doubleArrayOf(power, 0.0, 0.0, power))
            } else {
                //向左
                var power = pidEncoder.run(frontRightMotor.currentPosition.toDouble(), aim.toDouble())
                power = Math.signum(power) * Math.min(Math.abs(power), config().CHASSIS_MAX_POWER)
                setPowerOneByOne(doubleArrayOf(0.0, power, power, 0.0))
            }
            if (pidEncoder.isInErrAllowable) finished++
            else finished = 0
            if (finished >= 3) break
            Thread.sleep(10)
//            pidEncoder.debug()
//            telemetry().update()
        }
        setPower(0.0, 0.0)
        setZeroPowerBehavior(zeroPowerBehavior)
    }

    /**
     * 用编码器控制平移
     */
    fun setTransverseByEncoder(target: Int, outTime: Int = 3000) {
        setTargetByEncoder(false, target, outTime)
    }

    /**
     * 编码器控制
     */
    private fun setTargetByEncoder(straight: Boolean, target: Int, outTime: Int) {
        val zeroPowerBehavior = frontLeftMotor.zeroPowerBehavior
        setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT)
        val aim = target + frontLeftMotor.currentPosition
//        opMode().telemetryLog("实际:${frontLeftMotor.currentPosition},目标:${aim}")
        setPower(0.0, 0.0)
        var finished = 0
        val startTime = System.currentTimeMillis()
        while (opModeIsActive() && System.currentTimeMillis()-startTime<outTime) {
            var power = pidEncoder.run(frontLeftMotor.currentPosition.toDouble(), aim.toDouble())
            power = Math.signum(power) * Math.min(Math.abs(power), config().CHASSIS_MAX_POWER)
            if (straight) {
                setPower(power, power, 0.0)
            } else {
                setPower(0.0, 0.0, power)
            }
            if (pidEncoder.isInErrAllowable) finished++
            else finished = 0
            if (finished >= 3) break
            Thread.sleep(10)
//            pidEncoder.debug()
//            telemetry().update()
        }
        setPower(0.0, 0.0)
        setZeroPowerBehavior(zeroPowerBehavior)
    }

    /**
     * 用TensorFlow控制
     */
    private fun setTransverseTargetByTF(target: Double, outTime: Int = 2500) {
        setPower(0.0, 0.0)
        if (!opMode().runTensorFlow) return//直接返回
        val zeroPowerBehavior = frontLeftMotor.zeroPowerBehavior
        setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT)
        val startTime = System.currentTimeMillis()
        while (opModeIsActive() && System.currentTimeMillis()-startTime<outTime) {
            if (Math.abs(opMode().imu.getAngle(IMU.Direction.X)) >= 8) setAngle(0.0, 500)//偏差过大校准角度
            val power = pidTF.run(opMode().tensorFlow.getGoldPosition()[1], target)
            setPower(0.0, 0.0, power)
            if (pidTF.isInErrAllowable) break//进入目标范围，跳出
            Thread.sleep(10)
        }
//        setTransverseByEncoder(100)
        setAngle(0.0, 1000)//校准角度
        setZeroPowerBehavior(zeroPowerBehavior)
    }

    /**
     * 设置底盘朝向，需要用到IMU
     */
    fun setAngle(angle: Double, outTime: Int = 2500) {
        if (!opMode().runIMU) return//如果不启动IMU，则直接返回
        var pidIMUInBlind = 0
        val zeroPowerBehavior = frontLeftMotor.zeroPowerBehavior
        setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE)
        val imu = opMode().imu//返回陀螺仪的监测器
        val startTime = System.currentTimeMillis()
        while (opModeIsActive() && System.currentTimeMillis()-startTime<outTime) {
            val actual = imu.getAngleWithCycle(IMU.Direction.X)
            val target = imu.convertAngle(IMU.Direction.X, angle)
            val power = pidIMU.run(actual, target)//计算功率
            setPower(power, -power, 0.0)//给马达输出功率
//        pidIMU.debug(opMode());telemetry().update()//debug部分，可以注释
            if (pidIMU.isInErrAllowable) pidIMUInBlind++
            else pidIMUInBlind = 0
            if (pidIMUInBlind >= 3) break
            Thread.sleep(10)
        }
        setPower(0.0, 0.0)
        setZeroPowerBehavior(zeroPowerBehavior)
    }

    /**
     * 通过手柄推杆设置底盘方向
     */
    fun setAngleByGamepad() {
        if (pidIMU.isInErrAllowable) {
            setPower(getAngleByGamepad()[1], getAngleByGamepad()[1])
        } else {
            setAngle(getAngleByGamepad()[0])
        }
    }

    /**
     * 通过手柄推杆来获取方向和速度（即向量与坐标轴夹角和数量积）
     */
    private fun getAngleByGamepad(): DoubleArray {
        val gamepad = gamepad1()
        return if (gamepad.right_stick_x.equals(0) && gamepad.right_stick_y.equals(0)) {
            doubleArrayOf(lastAngleAim, 0.0)//当手柄不拨动时，保持上一次的状态
        } else if (gamepad.right_stick_x.equals(0) && !gamepad.right_stick_y.equals(0)) {
            lastAngleAim = Math.signum(-gamepad.right_stick_y)*90.0//更新上次目标
            doubleArrayOf(lastAngleAim, Math.abs(gamepad.right_stick_y).toDouble())//斜率不存在则为±90°
        } else {
            lastAngleAim = Math.atan2(-gamepad.right_stick_y.toDouble(), gamepad.right_stick_x.toDouble())/Math.PI*180//更新上次目标
            doubleArrayOf(lastAngleAim, Math.sqrt((gamepad.right_stick_x*gamepad.right_stick_x
                    + gamepad.right_stick_y*gamepad.right_stick_y).toDouble()))
            //斜率存在则代入点计算
        }
    }

    /**
     * 马达方向全部改变
     */
    fun directionAllChange() {
        directionAllChanged = !directionAllChanged
        if (directionAllChanged) opMode().sound(R.raw.high_up_model)
        else opMode().sound(R.raw.crater_model)
        frontLeftMotor.direction = DcMotorInfo.directionChange(frontLeftMotor)
        frontRightMotor.direction = DcMotorInfo.directionChange(frontRightMotor)
        rearLeftMotor.direction = DcMotorInfo.directionChange(rearLeftMotor)
        rearRightMotor.direction = DcMotorInfo.directionChange(rearRightMotor)
    }

    /**
     * 自动修改方向
     */
    fun autoDirectionChange() {
        if (time.milliseconds() >= 500 && config().chassisDirectionChange()) {
            directionAllChange()
            time.reset()
        }
    }

    /**
     * 重置底盘编码器
     */
    fun resetEncoder() {
        DcMotorInfo.resetEncoder(frontLeftMotor)
        DcMotorInfo.resetEncoder(frontRightMotor)
        DcMotorInfo.resetEncoder(rearLeftMotor)
        DcMotorInfo.resetEncoder(rearRightMotor)
    }

    /**
     * 回传位置
     */
    fun telemetryPosition() {
        telemetry().addData("fl", frontLeftMotor.currentPosition)
        telemetry().addData("fr", frontRightMotor.currentPosition)
        telemetry().addData("rl", rearLeftMotor.currentPosition)
        telemetry().addData("rr", rearRightMotor.currentPosition)
    }

    /**
     * 设置马达刹车
     */
    fun setZeroPowerBehavior(zeroPowerBehavior: DcMotor.ZeroPowerBehavior) {
        Thread.sleep(10)
        frontLeftMotor.zeroPowerBehavior = zeroPowerBehavior
        frontRightMotor.zeroPowerBehavior = zeroPowerBehavior
        rearLeftMotor.zeroPowerBehavior = zeroPowerBehavior
        rearRightMotor.zeroPowerBehavior = zeroPowerBehavior
        Thread.sleep(10)
    }

}