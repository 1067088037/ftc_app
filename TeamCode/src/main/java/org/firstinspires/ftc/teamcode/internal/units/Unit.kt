package org.firstinspires.ftc.teamcode.internal.units

import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer
import org.firstinspires.ftc.teamcode.internal.BaseOpMode
import org.firstinspires.ftc.teamcode.internal.config.Config

/**
 * 这是单元类，是一个重要的抽象类。
 * 每个机器人组件都继承自这个类，使得一些重要的变量或方法可以无需从Config里调用。
 */
abstract class Unit {

    //获取正在运行的LinearOpMode
    fun opMode(): BaseOpMode = Config.opMode

    //opMode是否在运行
    fun opModeIsActive(): Boolean = opMode().opModeIsActive()

    //硬件库
    fun hardwareMap(): HardwareMap = Config.opMode.hardwareMap

    //返回数据
    fun telemetry(): Telemetry = Config.opMode.telemetry

    //主线程休眠
    fun sleep(mill: Long) = Config.opMode.sleep(mill)

    //游戏手柄
    fun gamepad1() = Config.opMode.gamepad1
    fun gamepad2() = Config.opMode.gamepad2

    //配置类
    fun config(): Config = Config

}