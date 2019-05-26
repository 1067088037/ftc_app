package org.firstinspires.ftc.teamcode.internal.units

import com.qualcomm.robotcore.hardware.Servo
import com.qualcomm.robotcore.hardware.ServoController
import com.qualcomm.robotcore.util.ElapsedTime
import org.firstinspires.ftc.teamcode.internal.config.ServoInfo

/**
 * 这是收集类。
 * 收集的作用是将矿石从矿坑中卷入收集斗，再讲矿石倒入着陆器。
 */
class Collect: Unit() {

    /**
     * 收集硬件
     */
    private var collectTakeIn: Servo = ServoInfo.buildServo(config().COLLECT_TAKE_IN)
    private var collectFilpPlate: Servo = ServoInfo.buildServo(config().COLLECT_FILP_PLATE)

    private var filpPlateON = false
    private val time = ElapsedTime()

    init {
        time.reset()
    }

    /**
     * 自动控制
     */
    fun autoSetAll() {
        if (time.seconds() >= 0.5 && config().setFilpPlate()) {
            filpPlateON = filpPlateON.not()
            time.reset()
        }
        if (filpPlateON) {
            setFilpPlate(config().COLLECT_FILP_PLATE_ON)
        } else {
            setFilpPlate(config().COLLECT_FILP_PLATE_OFF)
        }
        when {
            config().collectOut() -> {
                setTakeIn(config().COLLECT_TAKE_IN_OUT)
            }
            config().collectInto() -> {
                setTakeIn(config().COLLECT_TAKE_IN_INTO)
            }
            else -> {
                setTakeIn(config().COLLECT_TAKE_IN_STOP)
            }
        }
//        opMode().log("收集:${collectTakeIn.position}")
    }

    /**
     * 自动阶段吐出吉祥物
     */
    fun outForAuto() {
        setTakeIn(config().COLLECT_TAKE_IN_OUT)
        Thread.sleep(1500)
        setTakeIn(config().COLLECT_TAKE_IN_INTO)
    }

    /**
     * 吸入
     */
    fun setTakeIn(position: Double) {
        collectTakeIn.position = position
    }

    /**
     * 翻板
     */
    fun setFilpPlate(position: Double) {
        collectFilpPlate.position = position
    }

}