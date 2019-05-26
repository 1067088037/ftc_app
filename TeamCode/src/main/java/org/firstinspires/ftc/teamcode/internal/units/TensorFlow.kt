package org.firstinspires.ftc.teamcode.internal.units

import org.firstinspires.ftc.robotcore.external.ClassFactory
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer
import org.firstinspires.ftc.robotcore.external.tfod.TFObjectDetector

/**
 * 这是TensorFlow类，用于识别矿石。
 */
class TensorFlow: Unit() {

    /**
     * 矿石标记
     */
    private val TFOD_MODEL_ASSET = "RoverRuckus.tflite"
    private val LABEL_GOLD_MINERAL = "Gold Mineral"
    private val LABEL_SILVER_MINERAL = "Silver Mineral"

    private lateinit var vuforia: VuforiaLocalizer

    lateinit var tfod: TFObjectDetector

    init {
        initVuforia()
        if (ClassFactory.getInstance().canCreateTFObjectDetector()) {
            initTfod()
        } else {
            telemetry().addData("Sorry!", "This device is not compatible with TFOD")
        }
        tfod.deactivate()
    }

    /**
     * 获取金矿石位置，新版
     */
    fun getGoldPositionNew(): GoldMineralPosition {
        val updatedRecognitions = tfod.updatedRecognitions
        return if (updatedRecognitions != null) {
            if (updatedRecognitions.size == 0) {
                GoldMineralPosition.Unknown//没有找到任何一个矿石
            } else {
//                telemetry().addData("# Object Detected", updatedRecognitions.size)//打印识别到的数量
                val goldMineral = doubleArrayOf(-Double.MAX_VALUE, -Double.MAX_VALUE)
                for (recognition in updatedRecognitions) {
                    config().ImageHeight = recognition.imageHeight
                    config().ImageWidth = recognition.imageWidth
                    if (recognition.label == LABEL_GOLD_MINERAL) {
                        if ((recognition.left + recognition.right) / 2 >= goldMineral[0]) {
                            goldMineral[0] = ((recognition.left + recognition.right) / 2).toDouble()
                            goldMineral[1] = ((recognition.top + recognition.bottom) / 2).toDouble()
                        }//获取最近的一个
                    }
                }
                if (goldMineral[0] < 0) {
                    GoldMineralPosition.Left//由于摄像头视角，左侧的那个无法看到，因此如果看到银矿石但却没有金矿石，默认为左边的
                } else {
                    val h = config().ImageHeight/3.0
                    when (goldMineral[1]) {
                        in h*0..h*1 -> GoldMineralPosition.Right
                        in h*1..h*2 -> GoldMineralPosition.Center
                        in h*2..h*3 -> GoldMineralPosition.Left
                        else -> GoldMineralPosition.Unknown//这怕是永远也不会发生
                    }
                }
            }
        } else GoldMineralPosition.Unknown//识别对象为空
    }

    /**
     * 获取金矿石位置，旧版
     */
    fun getGoldPosition(): DoubleArray {
        val updatedRecognitions = tfod.updatedRecognitions
        return if (updatedRecognitions != null) {
            if (updatedRecognitions.size == 0) {
                doubleArrayOf(-1.0, -1.0)
            } else {
                telemetry().addData("# Object Detected", updatedRecognitions.size)
                val goldMineral = doubleArrayOf(-Double.MAX_VALUE, -Double.MAX_VALUE)
                for (recognition in updatedRecognitions) {
                    config().ImageHeight = recognition.imageHeight
                    config().ImageWidth = recognition.imageWidth
                    if (recognition.label == LABEL_GOLD_MINERAL) {
                        if ((recognition.left + recognition.right) / 2 >= goldMineral[0]) {
                            goldMineral[0] = ((recognition.left + recognition.right) / 2).toDouble()
                            goldMineral[1] = ((recognition.top + recognition.bottom) / 2).toDouble()
                        }//获取最近的一个
                    }
                }
                if (goldMineral[0] < 0) {
                    doubleArrayOf(-1.0, -1.0)
                } else {
                    goldMineral
                }
            }
        } else doubleArrayOf(-1.0, -1.0)
    }

    /**
     * 原版运行
     */
    fun runOriginal(): GoldMineralPosition {
        val updatedRecognitions = tfod.updatedRecognitions
        if (updatedRecognitions != null) {
            telemetry().addData("# Object Detected", updatedRecognitions.size)
            if (updatedRecognitions.size == 3) {
                var goldMineralX = -1
                var silverMineral1X = -1
                var silverMineral2X = -1
                for (recognition in updatedRecognitions) {
                    when {
                        recognition.label == LABEL_GOLD_MINERAL -> goldMineralX = recognition.left.toInt()
                        silverMineral1X == -1 -> silverMineral1X = recognition.left.toInt()
                        else -> silverMineral2X = recognition.left.toInt()
                    }
                }
                if (goldMineralX != -1 && silverMineral1X != -1 && silverMineral2X != -1) {
                    return if (goldMineralX < silverMineral1X && goldMineralX < silverMineral2X) {
                        telemetry().addData("Gold Mineral Position", "Left")
                        GoldMineralPosition.Left
                    } else if (goldMineralX > silverMineral1X && goldMineralX > silverMineral2X) {
                        telemetry().addData("Gold Mineral Position", "Right")
                        GoldMineralPosition.Right
                    } else {
                        telemetry().addData("Gold Mineral Position", "Center")
                        GoldMineralPosition.Center
                    }
                }
            }
        }
        return GoldMineralPosition.Unknown
    }

    /**
     * 方位枚举
     */
    enum class GoldMineralPosition {
        Left, Right, Center, Unknown
    }

    /**
     * 启动
     */
    fun activate() {
        tfod.activate()
    }

    /**
     * 停止
     */
    fun deactivate() {
        tfod.deactivate()
    }

    /**
     * 初始化Vu
     */
    private fun initVuforia() {
//        val parameters = VuforiaLocalizer.Parameters()
//        parameters.vuforiaLicenseKey = config().VUFORIA_KEY
//        parameters.cameraDirection = VuforiaLocalizer.CameraDirection.BACK
//        vuforia = ClassFactory.getInstance().createVuforia(parameters)
        vuforia = config().vuforia!!
    }

    /**
     * 初始化TF
     */
    private fun initTfod() {
        val tfodMonitorViewId = hardwareMap().appContext.resources.getIdentifier(
                "tfodMonitorViewId", "id", hardwareMap().appContext.packageName)
        val tfodParameters = TFObjectDetector.Parameters(tfodMonitorViewId)
        tfod = ClassFactory.getInstance().createTFObjectDetector(tfodParameters, vuforia)
        tfod.loadModelFromAsset(TFOD_MODEL_ASSET, LABEL_GOLD_MINERAL, LABEL_SILVER_MINERAL)
    }
    
}