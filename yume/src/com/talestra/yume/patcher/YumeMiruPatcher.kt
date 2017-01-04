package com.talestra.yume.patcher

import com.soywiz.korim.format.readBitmap
import com.soywiz.korim.geom.Anchor
import com.soywiz.korim.geom.ScaleMode
import com.soywiz.korio.async.EventLoop
import com.soywiz.korio.vfs.ResourcesVfs
import com.soywiz.korui.Application
import com.soywiz.korui.frame
import com.soywiz.korui.geom.len.percent
import com.soywiz.korui.geom.len.pt
import com.soywiz.korui.style.*
import com.soywiz.korui.ui.*

object YumeMiruPatcher {
	const val VERSION = "v1.0"

	@JvmStatic fun main(args: Array<String>) = EventLoop.main {
		val bmp = ResourcesVfs["data/bg.jpg"].readBitmap()
		val frameIcon = ResourcesVfs["patcher_ico.png"].readBitmap()

		val buttonStyle = Style {
			minWidth = 80.pt
			minHeight = 32.pt
			maxHeight = 80.pt
			width = 20.percent
			height = 10.percent
		}

		Application().frame("Yume Miru Kusuri en español - $VERSION", 640, 480, icon = frameIcon) {
			layers {
				layersKeepAspectRatio(anchor = Anchor.TOP_CENTER, scaleMode = ScaleMode.COVER) {
					image(bmp)
				}
				relative {
					val patch = button("Parchear...") {
						alert("Patching!")
					}.apply {
						classStyle = buttonStyle
						right = 32.pt
						bottom = 32.pt
					}

					val website = button("Página web...") {
						openURL("http://yume.tales-tra.com/")
					}.apply {
						relativeTo = patch
						classStyle = buttonStyle
						right = 16.pt
					}
				}
			}
		}
	}
}
