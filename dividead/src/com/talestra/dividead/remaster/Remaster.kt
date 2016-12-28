package com.talestra.dividead.remaster

import com.soywiz.korio.async.asyncFun
import com.soywiz.korio.async.sync
import com.soywiz.korio.vfs.LocalVfs
import com.soywiz.korio.vfs.VfsFile
import com.talestra.dividead.LZ
import com.talestra.dividead.openAsDL1

object Remaster {
	val waifu2x_caffe_cui = "C:/projects/waifu2x-caffe/waifu2x-caffe-cui.exe"
	val ffmpeg = "ffmpeg"

	suspend fun waifu2x(input: VfsFile, output: VfsFile): Int = asyncFun {
		input.parent.passthru(
			waifu2x_caffe_cui,
			"--gpu", "0",
			"-s", "2.0",
			"-i", input.absolutePath,
			"-o", output.absolutePath
		)
	}

	suspend fun ffmpegExtractImages(input: VfsFile, output: VfsFile): Int = asyncFun {
		output.mkdirs()
		input.parent.passthru(
			ffmpeg,
			"-y",
			"-i", input.absolutePath,
			"-f", "image2",
			"${output.absolutePath}/%06d.png"
		)
	}

	suspend fun ffmpegExtractAudio(input: VfsFile, output: VfsFile): Int = asyncFun {
		input.parent.passthru(
			ffmpeg,
			"-y",
			"-i", input.absolutePath,
			output.absolutePath
		)
	}

	/*
	class VideoInfo(
		val fps: Int
	)
	*/

	suspend fun ffmpegGetVideoInfo(input: VfsFile) = asyncFun {
		val result = input.parent.execToString(ffmpeg, "-i", input.absolutePath)
		println(result)
	}

	suspend fun ffmpegPackVideo(fps: Int, inputImages: VfsFile, inputAudio: VfsFile, output: VfsFile): Int = asyncFun {
		inputImages.passthru(
			ffmpeg,
			"-y",
			"-framerate", "$fps",
			"-i", "${inputImages.absolutePath}/%06d.png",
			"-i", inputAudio.absolutePath,

			"-c:v", "libx264",
			//"-c:a", "aac",
			//"-b:a", "192k",

			"-r", "$fps",
			"-vf", "fps=$fps",

			//"-strict", "experimental",
			"-pix_fmt", "yuv420p",
			"-shortest",
			output.absolutePath
		)
	}

	suspend fun convertVideo(fps: Int, input: VfsFile, output: VfsFile) = asyncFun {
		val wav = input.appendExtension("wav")
		val images1x = input.appendExtension("images1x")
		val images2x = input.appendExtension("images2x")
		ffmpegExtractAudio(input, wav)
		ffmpegExtractImages(input, images1x)

		waifu2x(images1x, images2x)
		/*
		for (file in images.listFiles()) {
			println(file)
			waifu2x(file, file)
		}
		*/

		ffmpegPackVideo(30, images2x, wav, output)
	}

	suspend fun convertVideoLastStep(fps: Int, input: VfsFile, output: VfsFile) = asyncFun {
		val wav = input.appendExtension("wav")
		val images2x = input.appendExtension("image2x")
		ffmpegPackVideo(fps, images2x, wav, output)
	}

	suspend fun extractDl1(dl1: VfsFile, out: VfsFile) = asyncFun {
		out.mkdirs()
		val files = dl1.openAsDL1()
		for (file in files.listRecursive()) {
			val compressed = file.read()
			val uncompressed = if (LZ.isCompressed(compressed)) LZ.uncompress(compressed) else compressed
			println(file.fullname)
			out[file.fullname] = uncompressed
		}
	}

	@JvmStatic fun main(args: Array<String>) = sync {
		val base = LocalVfs("D:/juegos/dividead")
		if (!base["CS_ROGO.AVI.2x.mp4"].exists()) convertVideo(30, base["CS_ROGO.AVI"], base["CS_ROGO.AVI.2x.mp4"])
		if (!base["OPEN.AVI.2x.mp4"].exists()) convertVideo(15, base["OPEN.AVI"], base["OPEN.AVI.2x.mp4"])
		if (!base["SG.DL1.d"].exists()) extractDl1(base["SG.DL1"], base["SG.DL1.d"])
		if (!base["SG.DL1.2x.d"].exists()) waifu2x(base["SG.DL1.d"], base["SG.DL1.2x.d"])
	}
}