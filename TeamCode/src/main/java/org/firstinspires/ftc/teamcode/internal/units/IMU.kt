package org.firstinspires.ftc.teamcode.internal.units

import com.qualcomm.hardware.bosch.BNO055IMU
import com.qualcomm.hardware.bosch.JustLoggingAccelerationIntegrator
import org.firstinspires.ftc.robotcore.external.navigation.*

/**
 * 这是IMU（惯性测量单元）类。
 * IMU是嵌入在REV主控器中的一组芯片，内置了三轴陀螺仪、三轴加速度计、磁场传感器。
 * 合理的使用其中的陀螺仪可以在很大程度上帮助自动的实现。
 * 陀螺仪的功能不仅仅限于帮助完成转向，我们在自动阶段机器人着陆途中利用机器人倾斜角变化测控下降进度。
 */
class IMU: Unit() {

    /**
     * IMU硬件
     */
    private var imu: BNO055IMU//惯性测量单元
    private lateinit var angles: Orientation
    private lateinit var gravity: Acceleration

    private var start = false
    var cycle = intArrayOf(0, 0, 0)//圈数

    /**
     * 监测线程
     */
    val monitor = Thread {
        start = true
        var lastAngle = getAngle()
        opMode().waitForStart()
        while (opModeIsActive()) {
            while (start && opModeIsActive()) {
                val nowAngle = getAngle()//记录本次角度
                for (i in 0..2) {
                    if (Math.abs(nowAngle[i] - lastAngle[i]) >= 180) {
                        cycle[i] += Math.signum(lastAngle[i] - nowAngle[i]).toInt()//确定突变方向（顺时针圈数增加）
                    }//判断是否有突变
                }//遍历XYZ三坐标
                lastAngle = nowAngle//更新上一次角度
                Thread.sleep(10)
            }
            Thread.sleep(100)
        }
    }

    //初始化
    init {
        val parameters = BNO055IMU.Parameters()
        parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES
        parameters.accelUnit = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC
        parameters.calibrationDataFile = "BNO055IMUCalibration.json" // see the calibration sample opmode
        parameters.loggingEnabled = true
        parameters.loggingTag = "IMU"
        parameters.accelerationIntegrationAlgorithm = JustLoggingAccelerationIntegrator()

        imu = hardwareMap().get(BNO055IMU::class.java, config().IMU_GETNAME)
        imu.initialize(parameters)

        imu.startAccelerationIntegration(Position(), Velocity(), 100)

        startMonitor()//默认启动监测器
        initAngle()//初始化陀螺仪位置
    }

    /**
     * 初始化角度
     */
    fun initAngle() {
        resetCycle()
        angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES)
        config().initialAngle = doubleArrayOf(angles.firstAngle.toDouble(), 0.0, 0.0)
    }

    /**
     * 读取含有圈数的角度
     */
    fun getAngleWithCycle(direction: Direction): Double {
        return cycle[directionToArrayIndex(direction)] * 360.0 + getAngle(direction)
    }

    /**
     * 转换(-180,180]的角度为带有圈数的角度
     */
    fun convertAngle(direction: Direction, angle: Double): Double {
        val absAimReduceActul = doubleArrayOf(0.0, 0.0, 0.0)//声明及初始化数组
        val angle1 = getAngle()
        val directionIndex = directionToArrayIndex(direction)
        val cycle = cycle[directionIndex]
        for (i in 0..2) {
            absAimReduceActul[i] = Math.abs(angle1[i]-((cycle+(i-1)) * 360 + angle))//遍历数组
        }//读取相邻三个周期的值
        return if (absAimReduceActul[0] < absAimReduceActul[1]) {
            if (absAimReduceActul[0] < absAimReduceActul[2]) {
                (cycle-1) * 360 + angle//T-1区间
            } else {
                (cycle+1) * 360 + angle//T+1区间
            }
        } else {
            if (absAimReduceActul[1] < absAimReduceActul[2]) {
                (cycle+0) * 360 + angle//T区间
            } else {
                (cycle+1) * 360 + angle//T+1区间
            }
        }//判断哪个区间有最接近的解
    }

    /**
     * 启动监测
     */
    fun startMonitor() {
        start = true
        if (!monitor.isAlive) {
            monitor.start()
        }
    }

    /**
     * 停止监测
     */
    fun stopMonitor() {
        start = false
    }

    /**
     * 重置圈数
     */
    fun resetCycle() {
        cycle = intArrayOf(0, 0, 0)
    }

    /**
     * 获取角度
     */
    fun getAngle(direction: Direction): Double {
        angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES)
        return when (direction) {
            Direction.X -> -(angles.firstAngle - config().initialAngle[0])
            Direction.Y -> -(angles.secondAngle - config().initialAngle[1])
            Direction.Z -> -(angles.thirdAngle - config().initialAngle[2])
        }
    }

    /**
     * 获取三个方向角度
     */
    fun getAngle(): DoubleArray {
        angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES)
        return doubleArrayOf(-(angles.firstAngle - config().initialAngle[0]),
                -(angles.secondAngle - config().initialAngle[1]), -(angles.thirdAngle - config().initialAngle[2]))
    }

    /**
     * 获取加速度（不传参则为合加速度）
     */
    fun getAccel(direction: Direction? = null): Double {
        gravity = imu.gravity
        return when (direction) {
            Direction.X -> gravity.xAccel
            Direction.Y -> gravity.yAccel
            Direction.Z -> gravity.zAccel//xyz三个方向的加速度
            else -> Math.sqrt(gravity.xAccel * gravity.xAccel
                    + gravity.yAccel * gravity.yAccel
                    + gravity.zAccel * gravity.zAccel)//合加速度
        }
    }

    /**
     * 方向枚举
     */
    enum class Direction {
        X, Y, Z
    }

    /**
     * 单例
     */
    companion object {
        /**
         * 转换方向枚举到数组序号
         */
        fun directionToArrayIndex(direction: Direction = Direction.X): Int {
            return when (direction) {
                Direction.X -> 0
                Direction.Y -> 1
                Direction.Z -> 2
            }
        }

        /**
         * 转换数组序号到方向枚举
         */
        fun arrayIndexToDirection(index: Int = 0): Direction {
            return when (index) {
                0 -> Direction.X
                1 -> Direction.Y
                2 -> Direction.Z
                else -> Direction.X
            }
        }
    }

}