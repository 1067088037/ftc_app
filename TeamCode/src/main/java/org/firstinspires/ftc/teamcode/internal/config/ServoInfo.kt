package org.firstinspires.ftc.teamcode.internal.config

import com.qualcomm.robotcore.hardware.Servo

/**
 * 这个类的功能是帮助实现伺服信息统一储存和构造，设计的初衷还是懒而简洁。
 */
class ServoInfo constructor(val GET_NAME: String, val DIRECTION: Servo.Direction, val SCALE: DoubleArray = doubleArrayOf(0.0, 1.0)) {

    companion object {
        //配置伺服
        fun buildServo(info: ServoInfo): Servo {
            val servo = Config.opMode.hardwareMap.servo[info.GET_NAME]
            servo.direction = info.DIRECTION
            servo.scaleRange(info.SCALE[0], info.SCALE[1])
            return servo
        }
    }

}