package org.firstinspires.ftc.teamcode.internal.units

import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix
import org.firstinspires.ftc.robotcore.external.navigation.*
import java.util.ArrayList

/**
 * 这是Vuforia类，用于图像识别。
 */
class Vuforia: Unit() {

//    //VUFORIA的KEY编码
//    private val VUFORIA_KEY = Config.VUFORIA_KEY

    private val mmPerInch = 25.4f
    private val mmFTCFieldWidth = 12 * 6 * mmPerInch
    private val mmTargetHeight = 6 * mmPerInch

    //摄像头方位
    private val CAMERA_CHOICE = VuforiaLocalizer.CameraDirection.BACK

    private var lastLocation: OpenGLMatrix? = null
    private var targetVisible = false

    //视觉识别实例
    private var vuforia: VuforiaLocalizer = config().vuforia!!

    private var targetsRoverRuckus: VuforiaTrackables
    private var allTrackables: ArrayList<VuforiaTrackable>

    //构造函数
    init {
//        val cameraMonitorViewId = hardwareMap().appContext.resources.getIdentifier("cameraMonitorViewId", "id",
//                hardwareMap().appContext.packageName)
//        val parameters = VuforiaLocalizer.Parameters(cameraMonitorViewId)
//        parameters.vuforiaLicenseKey = config().VUFORIA_KEY
//        parameters.cameraDirection = CAMERA_CHOICE
//        vuforia = ClassFactory.getInstance().createVuforia(parameters)

        targetsRoverRuckus = vuforia.loadTrackablesFromAsset("RoverRuckus")
        val blueRover = targetsRoverRuckus[0]
        blueRover.name = "Blue-Rover"
        val redFootprint = targetsRoverRuckus[1]
        redFootprint.name = "Red-Footprint"
        val frontCraters = targetsRoverRuckus[2]
        frontCraters.name = "Front-Craters"
        val backSpace = targetsRoverRuckus[3]
        backSpace.name = "Back-Space"

        allTrackables = ArrayList()
        allTrackables.addAll(targetsRoverRuckus)

        val blueRoverLocationOnField = OpenGLMatrix
                .translation(0f, mmFTCFieldWidth, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(AxesReference.EXTRINSIC, AxesOrder.XYZ, AngleUnit.DEGREES, 90f, 0f, 0f))
        blueRover.location = blueRoverLocationOnField

        val redFootprintLocationOnField = OpenGLMatrix
                .translation(0f, -mmFTCFieldWidth, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(AxesReference.EXTRINSIC, AxesOrder.XYZ, AngleUnit.DEGREES, 90f, 0f, 180f))
        redFootprint.location = redFootprintLocationOnField

        val frontCratersLocationOnField = OpenGLMatrix
                .translation(-mmFTCFieldWidth, 0f, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(AxesReference.EXTRINSIC, AxesOrder.XYZ, AngleUnit.DEGREES, 90f, 0f, 90f))
        frontCraters.location = frontCratersLocationOnField

        val backSpaceLocationOnField = OpenGLMatrix
                .translation(mmFTCFieldWidth, 0f, mmTargetHeight)
                .multiplied(Orientation.getRotationMatrix(AxesReference.EXTRINSIC, AxesOrder.XYZ, AngleUnit.DEGREES, 90f, 0f, -90f))
        backSpace.location = backSpaceLocationOnField

        val CAMERA_FORWARD_DISPLACEMENT = 110
        val CAMERA_VERTICAL_DISPLACEMENT = 200
        val CAMERA_LEFT_DISPLACEMENT = 0

        val phoneLocationOnRobot = OpenGLMatrix
                .translation(CAMERA_FORWARD_DISPLACEMENT.toFloat(), CAMERA_LEFT_DISPLACEMENT.toFloat(), CAMERA_VERTICAL_DISPLACEMENT.toFloat())
                .multiplied(Orientation.getRotationMatrix(AxesReference.EXTRINSIC, AxesOrder.YZX, AngleUnit.DEGREES,
                        (if (CAMERA_CHOICE == VuforiaLocalizer.CameraDirection.FRONT) 90 else -90).toFloat(), 0f, 0f))

        for (trackable in allTrackables) {
            (trackable.listener as VuforiaTrackableDefaultListener).setPhoneInformation(phoneLocationOnRobot, config().CAMERA_DIRECTION)
        }

        targetsRoverRuckus.deactivate()
    }

    /**
     * 启动
     */
    fun activate() {
        targetsRoverRuckus.activate()
    }

    /**
     * 停止
     */
    fun deactivate() {
        targetsRoverRuckus.deactivate()
    }

    /**
     * 获取图案名称
     */
    fun getResultInName(): String {
        var result: String = "none"
        targetVisible = false
        for (trackable in allTrackables) {
            if ((trackable.getListener() as VuforiaTrackableDefaultListener).isVisible) {
//                telemetry().addData("Visible Target", trackable.getName())
                result = trackable.name
                targetVisible = true

                val robotLocationTransform = (trackable.getListener() as VuforiaTrackableDefaultListener).updatedRobotLocation
                if (robotLocationTransform != null) {
                    lastLocation = robotLocationTransform
                }
                break
            }
        }
        return result
    }

    /**
     * 获取位置
     */
    fun getResultInPosition(): Array<Double> {
        targetVisible = false
        for (trackable in allTrackables) {
            if ((trackable.getListener() as VuforiaTrackableDefaultListener).isVisible) {
//                telemetry().addData("Visible Target", trackable.getName())
                targetVisible = true
                val robotLocationTransform = (trackable.getListener() as VuforiaTrackableDefaultListener).updatedRobotLocation
                if (robotLocationTransform != null) {
                    lastLocation = robotLocationTransform
                }
                break
            }
        }

        if (targetVisible) {
            val translation = lastLocation!!.getTranslation()
//            telemetry().addData("Pos (in)", "{X, Y, Z} = %.1f, %.1f, %.1f", translation.get(0) / mmPerInch, translation.get(1) / mmPerInch, translation.get(2) / mmPerInch)
            return arrayOf(translation.get(0) / mmPerInch.toDouble(),
                    translation.get(1) / mmPerInch.toDouble(), translation.get(2) / mmPerInch.toDouble())
        } else {
//            telemetry().addData("Visible Target", "none")
        }

        return arrayOf(-Double.MAX_VALUE,-Double.MAX_VALUE,-Double.MAX_VALUE)
    }

    /**
     * 获取角度
     */
    fun getResultInRotation(): Array<Double> {
        targetVisible = false
        for (trackable in allTrackables) {
            if ((trackable.getListener() as VuforiaTrackableDefaultListener).isVisible) {
//                telemetry().addData("Visible Target", trackable.getName())
                targetVisible = true

                val robotLocationTransform = (trackable.getListener() as VuforiaTrackableDefaultListener).updatedRobotLocation
                if (robotLocationTransform != null) {
                    lastLocation = robotLocationTransform
                }
                break
            }
        }

        if (targetVisible) {
            val translation = lastLocation!!.getTranslation()
//            telemetry().addData("Pos (in)", "{X, Y, Z} = %.1f, %.1f, %.1f", translation.get(0) / mmPerInch, translation.get(1) / mmPerInch, translation.get(2) / mmPerInch)
            val rotation = Orientation.getOrientation(lastLocation, AxesReference.EXTRINSIC, AxesOrder.XYZ, AngleUnit.DEGREES)
            return arrayOf(rotation.firstAngle.toDouble(), rotation.secondAngle.toDouble(), rotation.thirdAngle.toDouble())
//            telemetry().addData("Rot (deg)", "{Roll, Pitch, Heading} = %.0f, %.0f, %.0f", rotation.firstAngle, rotation.secondAngle, rotation.thirdAngle)

        } else {
//            telemetry().addData("Visible Target", "none")
        }

        return arrayOf(-Double.MAX_VALUE,-Double.MAX_VALUE,-Double.MAX_VALUE)
    }

}