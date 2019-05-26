package org.firstinspires.ftc.teamcode.internal.config

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorSimple

/**
 * 这个类的功能是帮助实现马达信息统一储存和构造，设计的初衷还是懒而简洁。
 */
class DcMotorInfo constructor(val GET_NAME: String, val DIRECTION: DcMotorSimple.Direction, val BRAKE: DcMotor.ZeroPowerBehavior,
                              val RUN_MODE: DcMotor.RunMode) {

    companion object {
        //配置马达
        fun buildMotor(info: DcMotorInfo): DcMotor {
            val motor = Config.opMode.hardwareMap.dcMotor[info.GET_NAME]
            motor.direction = info.DIRECTION
            motor.zeroPowerBehavior = info.BRAKE
            motor.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER//清空编码器
            Thread.sleep(10)
            motor.mode = info.RUN_MODE//设定编码器模式
            return motor
        }

        //改变方向
        fun directionChange(motor: DcMotor): DcMotorSimple.Direction {
            return if (motor.direction == DcMotorSimple.Direction.FORWARD) {
                DcMotorSimple.Direction.REVERSE
            } else {
                DcMotorSimple.Direction.FORWARD
            }
        }

        //重置编码器
        fun resetEncoder(motor: DcMotor) {
            val mode = motor.mode
            motor.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
            Thread.sleep(10)
            motor.mode = mode
        }
    }

}