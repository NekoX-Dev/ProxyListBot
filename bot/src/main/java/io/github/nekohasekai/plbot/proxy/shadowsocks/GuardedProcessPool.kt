/*******************************************************************************
 *                                                                             *
 *  Copyright (C) 2017 by Max Lv <max.c.lv@gmail.com>                          *
 *  Copyright (C) 2017 by Mygod Studio <contact-shadowsocks-android@mygod.be>  *
 *                                                                             *
 *  This program is free software: you can redistribute it and/or modify       *
 *  it under the terms of the GNU General Public License as published by       *
 *  the Free Software Foundation, either version 3 of the License, or          *
 *  (at your option) any later version.                                        *
 *                                                                             *
 *  This program is distributed in the hope that it will be useful,            *
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the              *
 *  GNU General Public License for more details.                               *
 *                                                                             *
 *  You should have received a copy of the GNU General Public License          *
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.       *
 *                                                                             *
 *******************************************************************************/

package io.github.nekohasekai.plbot.proxy.shadowsocks

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import java.io.File
import java.io.IOException
import java.io.InputStream
import kotlin.concurrent.thread

class GuardedProcessPool(val dir: File, private val onFatal: suspend (IOException) -> Unit) : CoroutineScope {
    companion object {
        private const val TAG = "GuardedProcessPool"
    }

    private inner class Guard(private val cmd: List<String>) {
        private lateinit var process: Process

        private fun streamLogger(input: InputStream, logger: (String) -> Unit) = try {
            input.bufferedReader().forEachLine(logger)
        } catch (_: IOException) {
        }    // ignore

        fun start() {
            process = ProcessBuilder(cmd).directory(dir).start()
        }

        suspend fun looper(onRestartCallback: (suspend () -> Unit)?) {
            var running = true
            val cmdName = File(cmd.first()).nameWithoutExtension
            val exitChannel = Channel<Int>()
            try {
                while (true) {
                    thread(name = "stderr-$cmdName") {
                        streamLogger(process.errorStream) {
                            println("[$cmdName] $it")
                        }
                    }
                    thread(name = "stdout-$cmdName") {
                        streamLogger(process.inputStream) {
                            println("[$cmdName] $it")
                        }
                        // this thread also acts as a daemon thread for waitFor
                        runBlocking { exitChannel.send(process.waitFor()) }
                    }
                    val startTime = System.currentTimeMillis()
                    val exitCode = exitChannel.receive()
                    running = false
                    when {
                        System.currentTimeMillis() - startTime < 1000 -> throw IOException(
                                "$cmdName exits too fast (exit code: $exitCode)")
                        exitCode == 128 + 9 -> System.err.println("$cmdName was killed")
                        else -> System.err.println("$cmdName unexpectedly exits with code $exitCode")
                    }
                    start()
                    running = true
                    onRestartCallback?.invoke()
                }
            } catch (e: IOException) {
                System.err.println("error occurred. stop guard: " + cmd.joinToString(" "))
                onFatal(e)
            } finally {
                if (running) withContext(NonCancellable) {
                    process.destroy()                       // kill the process
                    if (withTimeoutOrNull(1000) { exitChannel.receive() } != null) return@withContext
                    process.destroyForcibly()           // Force to kill the process if it's still alive
                    exitChannel.receive()
                }                                           // otherwise process already exited, nothing to be done
            }
        }
    }

    override val coroutineContext = Job()

    fun start(cmd: List<String>, onRestartCallback: (suspend () -> Unit)? = null) {
        Guard(cmd).apply {
            start() // if start fails, IOException will be thrown directly
            launch { looper(onRestartCallback) }
        }
    }

    fun close(scope: CoroutineScope) {
        cancel()
    }
}
