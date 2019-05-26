package org.firstinspires.ftc.teamcode.internal

import android.util.Log
import com.qualcomm.ftccommon.SoundPlayer
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.util.ElapsedTime
import org.firstinspires.ftc.robotcore.external.ClassFactory
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer
import org.firstinspires.ftc.robotcore.internal.system.AppUtil
import org.firstinspires.ftc.teamcode.R
import org.firstinspires.ftc.teamcode.internal.config.Config
import org.firstinspires.ftc.teamcode.internal.units.*

/**
 * 这是整个程序最重要的类，是一个抽象类。
 * 由于直接继承LinearOpMode需要在继承后的类里实例化你需要的组件。
 * 我们为了规避这个麻烦事，于是直接用这个类继承LinearOpMode，再在这里实例化，最后用需要启动的程序继承这个类。
 * 程序可以根据自己的需求来调整需要实例化的组件，在下方的构造方法中通过布尔量来声明。
 * 按需实例化有效的减少了了Configure Robot的时间，减少了init阶段的时间，并减少了空指针异常的可能性。
 */
abstract class BaseOpMode constructor(val runChassis: Boolean = true, val runLift: Boolean = true, val runVuforia: Boolean = true,
                                      val runTensorFlow: Boolean = true, val runIMU: Boolean = true, val runCollect: Boolean = true,
                                      val runArm: Boolean = true) : LinearOpMode() {

    private val TAG = "BaseOpMode"//用于作为Log的TAG

    /**
     * 组件的实例
     */
    lateinit var chassis: Chassis//底盘
    lateinit var lift: Lift//升降
    lateinit var vuforia: Vuforia//视觉识别
    lateinit var tensorFlow: TensorFlow//物体追踪
    lateinit var imu: IMU//惯性测量单元
    lateinit var collect: Collect//收集
    lateinit var arm: Arm//机械臂

    var gamepadMonitor = false//手柄监测
    private val welcomeTime = ElapsedTime()//计时器
    private val batterTime = ElapsedTime()//计时器

    private val createVuforia = when {
        runVuforia -> 0//二者都或仅图像识别运行
        runTensorFlow && !runVuforia -> 1//仅物体追踪运行
        else -> 2//都不运行
    }

    /**
     * 协助线程
     */
    private val assistThread = Thread {
        var batterLowTimes = 0
        Log.d(TAG, "assistThread")
        val time = ElapsedTime()
        waitForStart()
        while (opModeIsActive()) {
            if (gamepadMonitor) {
                if (gamepad1.gamepadId == -2 && time.seconds()>=1) {
                    sound(R.raw.handle_disconnect)//手柄断连提示音
                    time.reset()
                }
            }
            if (getBatteryVoltage() < 10) {
                batterLowTimes++
                if (batterTime.seconds()>=5 && batterLowTimes>=20) {
                    batterTime.reset()
                    sound(R.raw.battery_low)
                }
            } else batterLowTimes = 0
            if (gamepad1.right_stick_button) welcome()
            Thread.sleep(10)
        }
    }

    /**
     * 填写机器人代码的地方
     */
    abstract fun run()

    /**
     * 因为涉及到硬件库调用等部分对象需要在runOpMode()中调用，为了照顾初始化和主程序运行，这里final了原来的runOpMode()
     */
    final override fun runOpMode() {
        Log.d(TAG, "initRobot()")
        telemetry.addData(">", "初始化中，请勿开始")
        telemetry.update()
        initRobot()//初始化机器人
        assistThread.start()//启动协助线程
        telemetry.addData(">", "初始化完毕，可以启动->>>>>>>>>>")
        telemetry.update()
        Log.d(TAG, "run()")
        run()//运行阶段
        Log.d(TAG, "stopRobot()")
        stopRobot()
        Log.d(TAG, "End")
    }

    /**
     * 初始化机器人
     */
    private fun initRobot() {
        Config.opMode = this//将目前启动的OpMode发送到配置类

        //用于保持Vuforia的单例
        when(createVuforia) {
            0 -> {
                val cameraMonitorViewId = hardwareMap.appContext.resources.getIdentifier("cameraMonitorViewId", "id",
                        hardwareMap.appContext.packageName)
                val parameters = VuforiaLocalizer.Parameters(cameraMonitorViewId)
                parameters.vuforiaLicenseKey = config().VUFORIA_KEY
                parameters.cameraDirection = config().CAMERA_DIRECTION
                config().vuforia = ClassFactory.getInstance().createVuforia(parameters)
            }
            1 -> {
                val parameters = VuforiaLocalizer.Parameters()
                parameters.vuforiaLicenseKey = config().VUFORIA_KEY
                parameters.cameraDirection = config().CAMERA_DIRECTION
                config().vuforia = ClassFactory.getInstance().createVuforia(parameters)
            }
            2 -> {
                Config.vuforia = null
            }
        }

        //初始化组件
        if (runChassis) chassis = Chassis()
        if (runLift) lift = Lift()
        if (runVuforia) vuforia = Vuforia()
        if (runTensorFlow) tensorFlow = TensorFlow()
        if (runIMU) imu = IMU()
        if (runCollect) collect = Collect()
        if (runArm) arm = Arm()
    }

    /**
     * 程序停止
     */
    private fun stopRobot() {
        if (runVuforia) vuforia.deactivate()
        if (runTensorFlow) tensorFlow.tfod.shutdown()
        if (runIMU) imu.stopMonitor()
    }

    /**
     * 获取电池电压
     */
    fun getBatteryVoltage(): Double {
        var result = Double.POSITIVE_INFINITY
        for (sensor in hardwareMap.voltageSensor) {
            val voltage = sensor.voltage
            if (voltage > 0) {
                result = Math.min(result, voltage)
            }
        }
        return result
    }

    /**
     * 欢迎操作手
     */
    fun welcome() {
        if (welcomeTime.seconds() >= 1) {
            welcomeTime.reset()
            Thread {
//                sound(R.raw.beginning_1)
//                Thread.sleep(4000)
                sound(R.raw.beginning_2)
                Thread.sleep(2000)
                sound(R.raw.beginning_3)
            }.start()
        }
    }

    /**
     * 等待手动按下结束键，而非直接结束
     */
    fun waitForEnd() {
        while (opModeIsActive()) {
            Thread.sleep(10)
        }
    }

    /**
     * 播放声音
     */
    fun sound(i: Int) {
        SoundPlayer.getInstance().startPlaying(AppUtil.getInstance().application.applicationContext, i)
    }

    /**
     * 打印Log
     */
    fun log(msg: Any) {
        Log.d("UST", msg.toString())
    }

    /**
     * 打印telemetry
     */
    fun telemetryLog(msg: Any) {
        telemetry.addData("msg", msg)
        telemetry.update()
    }

    /**
     * 返回配置类
     */
    fun config(): Config = Config

}