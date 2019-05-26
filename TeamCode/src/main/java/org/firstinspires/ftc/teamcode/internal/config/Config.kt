package org.firstinspires.ftc.teamcode.internal.config

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.Servo
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer
import org.firstinspires.ftc.teamcode.internal.BaseOpMode

/**
 * 这是UST程序中至关重要的一个类，他存储着UST机器人的所有配置、常量和重要实例。
 * 这个想法是2017~2018赛季LOG总结出来的，它极大的提高了程序员的查错能力，甚至可以帮助不会程序的结构组临时修改参数。
 * 维护好这个类将帮助你们的队伍获得更大的竞争力。
 */
object Config {

    /**
     * 实例区<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
     */
    //正在运行的LinearOpMode
    lateinit var opMode: BaseOpMode

    //单例Vuforia
    var vuforia: VuforiaLocalizer? = null
    //Vuforia的Key
    const val VUFORIA_KEY = "AW3BbqH/////AAAAmXvEKYeBKkQSuwntlOf9l54bTtsbZGwfTfUdVScy6zXY41HdT0Q5/RLMXDNd3jy2v4/A+fWtI8n8CGOIS///hL1WIdzmUut3jJ5XVSIBPbSMHSUxI2V+M0eYNXap743ZmLSVKCiPyWsvdiZs1VwEs8EpYxxNWJgkIT1jyXhmRrath10INBW5BCMHM4Y8YRjMo3Be1nx4nb9X0tR1Vi2lwtjqPNqML78csvzI9JsbIV/F7S+IBJXO7GHjZQdKjb6b7N0pb0Bft79IPSv08APyXduWd79LdpAY/qRBNLX45dPUVn2wBWj9ZMqkDT+fSH7c+vWKLhqeg/t2JRO1rsJMB8vXNrGJtTmltqUNpGGDx78u"
    //Vuforia摄像头方向
    val CAMERA_DIRECTION = VuforiaLocalizer.CameraDirection.FRONT
    /**
     * 实例区>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
     */


    /**
     * 硬件初始化<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
     */
    //底盘马达
    val FRONT_LEFT_MOTOR = DcMotorInfo("m0", DcMotorSimple.Direction.FORWARD, DcMotor.ZeroPowerBehavior.FLOAT, DcMotor.RunMode.RUN_USING_ENCODER)//左前方马达
    val REAR_LEFT_MOTOR = DcMotorInfo("m1", DcMotorSimple.Direction.FORWARD, DcMotor.ZeroPowerBehavior.FLOAT, DcMotor.RunMode.RUN_USING_ENCODER)//左后方马达
    val FRONT_RIGHT_MOTOR = DcMotorInfo("m2", DcMotorSimple.Direction.REVERSE, DcMotor.ZeroPowerBehavior.FLOAT, DcMotor.RunMode.RUN_USING_ENCODER)//右前方马达
    val REAR_RIGHT_MOTOR = DcMotorInfo("m3", DcMotorSimple.Direction.REVERSE, DcMotor.ZeroPowerBehavior.FLOAT, DcMotor.RunMode.RUN_USING_ENCODER)//右后方马达

    //升降马达
    val LEFT_LIFT_MOTOR = DcMotorInfo("m4", DcMotorSimple.Direction.REVERSE, DcMotor.ZeroPowerBehavior.FLOAT, DcMotor.RunMode.RUN_USING_ENCODER)//左侧升降马达
    val RIGHT_LIFT_MOTOR = DcMotorInfo("m5", DcMotorSimple.Direction.REVERSE, DcMotor.ZeroPowerBehavior.FLOAT, DcMotor.RunMode.RUN_USING_ENCODER)//右侧升降马达

    //机械臂
    val ARM_DOWN_MOTOR = DcMotorInfo("m6", DcMotorSimple.Direction.FORWARD, DcMotor.ZeroPowerBehavior.FLOAT, DcMotor.RunMode.RUN_WITHOUT_ENCODER)//下面的
    val ARM_UP_MOTOR = DcMotorInfo("m7", DcMotorSimple.Direction.REVERSE, DcMotor.ZeroPowerBehavior.FLOAT, DcMotor.RunMode.RUN_USING_ENCODER)//上面的
    val ARM_EJECT = ServoInfo("s2", Servo.Direction.FORWARD, doubleArrayOf(0.0, 1.0))//弹出机械臂

    //收集伺服
    val COLLECT_TAKE_IN = ServoInfo("s0", Servo.Direction.FORWARD, doubleArrayOf(0.0, 1.0))//吸入伺服（连续转动）
    val COLLECT_FILP_PLATE = ServoInfo("s1", Servo.Direction.FORWARD, doubleArrayOf(0.0, 1.0))//翻板伺服

    //IMU命名
    const val IMU_GETNAME = "imu"
    /**
     * 硬件初始化>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
     */


    /**
     * 常量区<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
     */
    //TF图像大小
    var ImageHeight = 1920
    var ImageWidth = 1080

    //电机编码器
    const val NeveRest40Gearmotor_ticksPerRev = 1120
    const val NeveRest60Gearmotor_ticksPerRev = 1680

    //底盘马达的转向系数
    const val CHASSIS_TURN_K = 1.0
    //转向PID和死区
    val CHASSIS_ROTATE_PID = doubleArrayOf(0.022, 0.0, 0.025)
    const val CHASSIS_ROTATE_BLIND = 1.0
    //TF的PID和死区
    val CHASSIS_TF_PID = doubleArrayOf(0.0004, 0.0, 0.0001)
    const val CHASSIS_TF_BLIND = 125.0
    //编码器的PID
    val CHASSIS_ENCODER_PID = doubleArrayOf(0.0030, 0.0, 0.005)
    const val CHASSIS_ENCODER_BLIND = 20.0
    //底盘PID控制时的最大功率
    const val CHASSIS_MAX_POWER = 1.0

    //机械臂PID和死区
    val ARM_PID = doubleArrayOf(0.005, 0.0, 0.001)
    const val ARM_BLIND = 0.0
    //速度比例常数
    const val speedK = 200.0
    //机械臂最大功率
    const val ARM_MAX_POWER = 1.0
    //机械臂最大位置
    const val ARM_MAX_POSITION = 2550
    //机械臂中间位置
    const val ARM_MIDDLE_POSITION = 1000

    //弹出伺服
    const val ARM_EJECT_OUT = 1.0

    //升降PID和死区
    val LIFT_PID = doubleArrayOf(0.0007, 0.0001, 0.008)
    const val LIFT_BLIND = 75.0
    //升降的最大校正功率
    const val LIFT_CORRECT_MAXPOWER = 0.35
    //合适的高挂位置
    const val LIFT_HIGHUP_POSITION = 10800

    //收集装置常量
    const val COLLECT_TAKE_IN_INTO = 1.0//收入
    const val COLLECT_TAKE_IN_OUT = 0.0//吐出
    const val COLLECT_TAKE_IN_STOP = 0.5//停止
    const val COLLECT_FILP_PLATE_ON = 0.40//打开
    const val COLLECT_FILP_PLATE_OFF = 0.00//关闭

    //陀螺仪初始方向
    var initialAngle = doubleArrayOf(0.0, 0.0, 0.0)
    //机器人着落时最大角度
    const val LANDING_ANGLE = 1.50
    //悬挂角度
    const val SUSPENSION_ANGLE = 3.0
    /**
     * 常量区>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
     */


    /**
     * 按键区>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
     */
    fun chassisDirectionChange(): Boolean = opMode.gamepad1.left_stick_button//底盘马达反向
    fun liftUp(): Boolean = opMode.gamepad1.y//升降向上
    fun liftDown(): Boolean = opMode.gamepad1.a && !opMode.gamepad1.start//升降下降
    fun autoToTop(): Boolean = opMode.gamepad1.dpad_up//自动到顶
    fun autoToBottom(): Boolean = opMode.gamepad1.dpad_down//自动到底
    fun ejectDebug(): Boolean = opMode.gamepad1.b//弹出伺服查错
    fun setFilpPlate(): Boolean = opMode.gamepad1.left_bumper//收集翻板
    fun collectOut(): Boolean = opMode.gamepad1.x//收集吐出
    fun collectInto(): Boolean = opMode.gamepad1.right_bumper//收集吸入
    /**
     * 按键区>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
     */

}