package org.firstinspires.ftc.teamcode.internal.units

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.firstinspires.ftc.teamcode.internal.config.Config

/**
 * 这是PID控制类。
 */
class PID constructor(pid: DoubleArray, private val errAllowable: Double) {

    /**
     * PID参数
     */
    var kp: Double = pid[0]
    var ki: Double = pid[1]
    var kd: Double = pid[2]

    private var target: Double = 0.0
    private var actual: Double = 0.0
    private var err: Double = 0.0
    private var lastErr: Double = 0.0
    private var outPut: Double = 0.0
    private var integral: Double = 0.0

    var isInErrAllowable = false

    /**
     * PID控制方法
     */
    fun run(actual: Double, target: Double): Double {
        this.actual = actual
        this.target = target
        err = this.target - this.actual//计算偏差
        if (Math.abs(err) > errAllowable/*检测是否在死区*/) {
            integral += err
            outPut = kp * err + ki * integral + kd * (err - lastErr)//PID函数运算
            isInErrAllowable = false
        } else {
            outPut = 0.0
            isInErrAllowable = true
        }
        lastErr = err//更新偏差
        return outPut
    }

    /**
     * 调试方法
     */
    fun debug(opMode: LinearOpMode = Config.opMode) {
        opMode.telemetry.addLine()
                .addData("实际", "%.1f", actual)
                .addData("目标", "%.1f", target)
                .addData("偏差", "%.1f", err)
                .addData("功率", "%.4f", outPut)
        opMode.telemetry.addLine("PID :")
                .addData("P","%.5f", kp)
                .addData("I","%.5f", ki)
                .addData("D","%.5f", kd)
                .addData("允许误差", errAllowable)
    }

}