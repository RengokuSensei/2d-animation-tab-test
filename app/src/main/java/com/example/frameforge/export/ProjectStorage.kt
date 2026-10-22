package com.example.frameforge.export

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import com.example.frameforge.model.AnimationLayer
import com.example.frameforge.model.AnimationProject
import com.example.frameforge.model.Bone
import com.example.frameforge.model.BoneKeyframe
import com.example.frameforge.model.BonePose
import com.example.frameforge.model.DrawingCel
import com.example.frameforge.model.DrawingStroke
import com.example.frameforge.model.SkeletonRig
import com.example.frameforge.model.StrokePoint
import com.example.frameforge.model.ToolType
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

class ProjectStorage(private val context: Context) {

    private val projectsDir = File(context.filesDir, "frameforge_projects").apply { mkdirs() }
    private val exportDir = File(context.cacheDir, "frameforge_exports").apply { mkdirs() }

    fun listSavedProjects(): List<Pair<String, String>> {
        val files = projectsDir.listFiles { _, name -> name.endsWith(".ffproj") } ?: return emptyList()
        return files.map { file ->
            val id = file.nameWithoutExtension
            val name = try {
                val json = JSONObject(file.readText())
                json.optString("name", "Untitled")
            } catch (e: Exception) {
                file.nameWithoutExtension
            }
            Pair(id, name)
        }
    }

    fun saveProject(project: AnimationProject): Boolean {
        return try {
            val file = File(projectsDir, "${project.id}.ffproj")
            val json = serializeProject(project)
            file.writeText(json.toString())
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun loadProject(projectId: String): AnimationProject? {
        return try {
            val file = File(projectsDir, "$projectId.ffproj")
            if (!file.exists()) return null
            val json = JSONObject(file.readText())
            deserializeProject(json)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun deleteProject(projectId: String): Boolean {
        val file = File(projectsDir, "$projectId.ffproj")
        return if (file.exists()) file.delete() else true
    }

    fun exportGif(frames: List<Bitmap>, fps: Int, loop: Boolean = true): File? {
        if (frames.isEmpty()) return null
        return try {
            val first = frames[0]
            val encoder = GifEncoder()
            encoder.start(first.width, first.height, fps, loop)
            for (f in frames) {
                encoder.addFrame(f)
            }
            val gifBytes = encoder.finish()
            val outFile = File(exportDir, "animation_${System.currentTimeMillis()}.gif")
            FileOutputStream(outFile).use { it.write(gifBytes) }
            outFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun exportMp4(frames: List<Bitmap>, fps: Int): File? {
        if (frames.isEmpty()) return null
        return try {
            val outFile = File(exportDir, "animation_${System.currentTimeMillis()}.mp4")
            val encoder = Mp4Encoder()
            val success = encoder.encodeFramesToMp4(frames, outFile, fps)
            if (success && outFile.exists() && outFile.length() > 0) outFile else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun exportMp4Streaming(
        width: Int,
        height: Int,
        fps: Int,
        totalFrames: Int,
        frameProvider: (frameIndex: Int) -> Bitmap?
    ): File? {
        if (totalFrames <= 0) return null
        return try {
            val outFile = File(exportDir, "animation_${System.currentTimeMillis()}.mp4")
            val encoder = Mp4Encoder()
            val success = encoder.encodeStreamingMp4(
                outputFile = outFile,
                width = width,
                height = height,
                totalFrames = totalFrames,
                fps = fps,
                frameProvider = frameProvider
            )
            if (success && outFile.exists() && outFile.length() > 0) outFile else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun exportPngSequence(frames: List<Bitmap>): List<File> {
        val result = mutableListOf<File>()
        val batchDir = File(exportDir, "seq_${System.currentTimeMillis()}").apply { mkdirs() }
        frames.forEachIndexed { index, bitmap ->
            val f = File(batchDir, "frame_%04d.png".format(index + 1))
            FileOutputStream(f).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            result.add(f)
        }
        return result
    }

    fun exportPngSequenceStreaming(
        totalFrames: Int,
        frameProvider: (frameIndex: Int) -> Bitmap?
    ): List<File> {
        val result = mutableListOf<File>()
        val batchDir = File(exportDir, "seq_${System.currentTimeMillis()}").apply { mkdirs() }
        for (index in 0 until totalFrames) {
            val bitmap = frameProvider(index) ?: continue
            val f = File(batchDir, "frame_%04d.png".format(index + 1))
            FileOutputStream(f).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            bitmap.recycle()
            result.add(f)
        }
        return result
    }

    // --- JSON Serialization ---

    private fun serializeProject(p: AnimationProject): JSONObject {
        val root = JSONObject()
        root.put("id", p.id)
        root.put("name", p.name)
        root.put("width", p.canvasWidth)
        root.put("height", p.canvasHeight)
        root.put("fps", p.fps)
        root.put("totalFrames", p.totalFrames)
        root.put("createdAt", p.createdAt)
        root.put("updatedAt", System.currentTimeMillis())

        // Layers
        val layersArr = JSONArray()
        p.layers.forEach { layer ->
            val lObj = JSONObject()
            lObj.put("id", layer.id)
            lObj.put("name", layer.name)
            lObj.put("isVisible", layer.isVisible)
            lObj.put("isLocked", layer.isLocked)
            lObj.put("isAlphaLocked", layer.isAlphaLocked)
            lObj.put("opacity", layer.opacity.toDouble())
            layersArr.put(lObj)
        }
        root.put("layers", layersArr)

        // Cels
        val celsArr = JSONArray()
        p.cels.forEach { cel ->
            val cObj = JSONObject()
            cObj.put("id", cel.id)
            cObj.put("frameIndex", cel.frameIndex)
            cObj.put("layerId", cel.layerId)
            cObj.put("holdDuration", cel.holdDuration)
            cObj.put("isKeyframe", cel.isKeyframe)

            val strokesArr = JSONArray()
            cel.strokes.forEach { stroke ->
                val sObj = JSONObject()
                sObj.put("id", stroke.id)
                sObj.put("color", stroke.colorArgb)
                sObj.put("tool", stroke.toolType.name)
                sObj.put("size", stroke.size.toDouble())
                sObj.put("opacity", stroke.opacity.toDouble())
                sObj.put("smoothing", stroke.smoothing.toDouble())

                val ptsArr = JSONArray()
                stroke.points.forEach { pt ->
                    val pObj = JSONObject()
                    pObj.put("x", pt.x.toDouble())
                    pObj.put("y", pt.y.toDouble())
                    pObj.put("p", pt.pressure.toDouble())
                    ptsArr.put(pObj)
                }
                sObj.put("points", ptsArr)
                strokesArr.put(sObj)
            }
            cObj.put("strokes", strokesArr)
            celsArr.put(cObj)
        }
        root.put("cels", celsArr)

        // Skeleton
        val rigObj = JSONObject()
        rigObj.put("id", p.boneRig.id)
        rigObj.put("rootId", p.boneRig.rootBoneId)
        rigObj.put("rootX", p.boneRig.rootBaseX.toDouble())
        rigObj.put("rootY", p.boneRig.rootBaseY.toDouble())

        val bonesArr = JSONArray()
        p.boneRig.bones.forEach { b ->
            val bObj = JSONObject()
            bObj.put("id", b.id)
            bObj.put("name", b.name)
            bObj.put("parentId", b.parentId ?: "")
            bObj.put("length", b.length.toDouble())
            bObj.put("angle", b.localAngleDeg.toDouble())
            bObj.put("color", b.colorArgb)
            bonesArr.put(bObj)
        }
        rigObj.put("bones", bonesArr)
        root.put("boneRig", rigObj)

        // Bone keyframes
        val kfArr = JSONArray()
        p.boneKeyframes.forEach { kf ->
            val kObj = JSONObject()
            kObj.put("frame", kf.frameIndex)
            val anglesObj = JSONObject()
            kf.pose.boneAnglesDeg.forEach { (k, v) -> anglesObj.put(k, v.toDouble()) }
            kObj.put("angles", anglesObj)
            kObj.put("rootX", kf.pose.rootX.toDouble())
            kObj.put("rootY", kf.pose.rootY.toDouble())
            kfArr.put(kObj)
        }
        root.put("boneKeyframes", kfArr)

        return root
    }

    private fun deserializeProject(json: JSONObject): AnimationProject {
        val id = json.getString("id")
        val name = json.optString("name", "Untitled")
        val width = json.optInt("width", 1280)
        val height = json.optInt("height", 720)
        val fps = json.optInt("fps", 24)
        val totalFrames = json.optInt("totalFrames", 24)
        val createdAt = json.optLong("createdAt", System.currentTimeMillis())

        val layers = mutableListOf<AnimationLayer>()
        val layersArr = json.optJSONArray("layers")
        if (layersArr != null) {
            for (i in 0 until layersArr.length()) {
                val l = layersArr.getJSONObject(i)
                layers.add(
                    AnimationLayer(
                        id = l.getString("id"),
                        name = l.getString("name"),
                        isVisible = l.optBoolean("isVisible", true),
                        isLocked = l.optBoolean("isLocked", false),
                        isAlphaLocked = l.optBoolean("isAlphaLocked", false),
                        opacity = l.optDouble("opacity", 1.0).toFloat()
                    )
                )
            }
        }

        val cels = mutableListOf<DrawingCel>()
        val celsArr = json.optJSONArray("cels")
        if (celsArr != null) {
            for (i in 0 until celsArr.length()) {
                val c = celsArr.getJSONObject(i)
                val strokes = mutableListOf<DrawingStroke>()
                val sArr = c.optJSONArray("strokes")
                if (sArr != null) {
                    for (j in 0 until sArr.length()) {
                        val s = sArr.getJSONObject(j)
                        val toolName = s.optString("tool", "PEN")
                        val tool = try { ToolType.valueOf(toolName) } catch (e: Exception) { ToolType.PEN }

                        val pts = mutableListOf<StrokePoint>()
                        val pArr = s.optJSONArray("points")
                        if (pArr != null) {
                            for (k in 0 until pArr.length()) {
                                val p = pArr.getJSONObject(k)
                                pts.add(
                                    StrokePoint(
                                        x = p.getDouble("x").toFloat(),
                                        y = p.getDouble("y").toFloat(),
                                        pressure = p.optDouble("p", 1.0).toFloat()
                                    )
                                )
                            }
                        }

                        strokes.add(
                            DrawingStroke(
                                id = s.optString("id", java.util.UUID.randomUUID().toString()),
                                points = pts,
                                colorArgb = s.getInt("color"),
                                toolType = tool,
                                size = s.getDouble("size").toFloat(),
                                opacity = s.optDouble("opacity", 1.0).toFloat(),
                                smoothing = s.optDouble("smoothing", 0.5).toFloat()
                            )
                        )
                    }
                }

                cels.add(
                    DrawingCel(
                        id = c.getString("id"),
                        frameIndex = c.getInt("frameIndex"),
                        layerId = c.getString("layerId"),
                        strokes = strokes,
                        holdDuration = c.optInt("holdDuration", 1),
                        isKeyframe = c.optBoolean("isKeyframe", true)
                    )
                )
            }
        }

        // Rig
        val rig = if (json.has("boneRig")) {
            val rObj = json.getJSONObject("boneRig")
            val bones = mutableListOf<Bone>()
            val bArr = rObj.optJSONArray("bones")
            if (bArr != null) {
                for (i in 0 until bArr.length()) {
                    val b = bArr.getJSONObject(i)
                    val parent = b.optString("parentId")
                    bones.add(
                        Bone(
                            id = b.getString("id"),
                            name = b.getString("name"),
                            parentId = if (parent.isEmpty()) null else parent,
                            length = b.getDouble("length").toFloat(),
                            localAngleDeg = b.optDouble("angle", 0.0).toFloat(),
                            colorArgb = b.optInt("color", 0xFFFF9800.toInt())
                        )
                    )
                }
            }
            SkeletonRig(
                id = rObj.optString("id", java.util.UUID.randomUUID().toString()),
                rootBoneId = rObj.optString("rootId", "bone_hips"),
                bones = if (bones.isNotEmpty()) bones else SkeletonRig.createDefaultBiped().bones,
                rootBaseX = rObj.optDouble("rootX", 640.0).toFloat(),
                rootBaseY = rObj.optDouble("rootY", 360.0).toFloat()
            )
        } else {
            SkeletonRig.createDefaultBiped()
        }

        val boneKeyframes = mutableListOf<BoneKeyframe>()
        val kfArr = json.optJSONArray("boneKeyframes")
        if (kfArr != null) {
            for (i in 0 until kfArr.length()) {
                val kObj = kfArr.getJSONObject(i)
                val frame = kObj.getInt("frame")
                val angles = mutableMapOf<String, Float>()
                val aObj = kObj.optJSONObject("angles")
                if (aObj != null) {
                    val keys = aObj.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        angles[key] = aObj.getDouble(key).toFloat()
                    }
                }
                boneKeyframes.add(
                    BoneKeyframe(
                        frameIndex = frame,
                        pose = BonePose(
                            boneAnglesDeg = angles,
                            rootX = kObj.optDouble("rootX", 0.0).toFloat(),
                            rootY = kObj.optDouble("rootY", 0.0).toFloat()
                        )
                    )
                )
            }
        }

        return AnimationProject(
            id = id,
            name = name,
            canvasWidth = width,
            canvasHeight = height,
            fps = fps,
            totalFrames = totalFrames,
            layers = if (layers.isNotEmpty()) layers else AnimationProject().layers,
            cels = cels,
            boneRig = rig,
            boneKeyframes = boneKeyframes,
            createdAt = createdAt,
            updatedAt = System.currentTimeMillis()
        )
    }
}
