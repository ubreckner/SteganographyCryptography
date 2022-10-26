package cryptography

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO
import kotlin.experimental.xor

fun main() {
    var input = getInput()
    while (true) {
        when (input) {
            "hide" -> {
                println("Hiding message in image.")
                hide()
            }

            "show" -> {
                println("Obtaining message from image.")
                show()
            }

            "exit" -> {
                println("Bye!")
                break
            }

            else -> println("Wrong task: $input")
        }
        input = getInput()
    }
}

fun show() {
    println("Input image file:")
    val inputName = readln()
    println("Password:")
    val password = readln().encodeToByteArray()
    val image = readImageFile(inputName)
    if (image != null) {
        var byteStr = ""
        for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                val color = Color(image.getRGB(x, y))
                val b = color.blue

                if (!".*000000000000000000000011$".toRegex().matches(byteStr)) {
                    byteStr += Integer.toBinaryString(b).last()
                }
            }
        }
        var chunks = byteStr.chunked(8)
        chunks = chunks.subList(0, chunks.size - 3)
        var byteArr = byteArrayOf()
        for (chunk in chunks) {
            byteArr += Integer.parseInt(chunk, 2).toByte()
        }
        val decryptedMessageBytes = encryptMessage(byteArr, password)
        println("Message:")
        println(decryptedMessageBytes.toString(Charsets.UTF_8))
    }
}

fun hide() {
    println("Input image file:")
    val inputName = readln()
    println("Output image file:")
    val outputName = readln()
    println("Message to hide:")
    val messageBytes = readln().encodeToByteArray()
    println("Password:")
    val password = readln().encodeToByteArray()

    var encryptedMessageBytes = encryptMessage(messageBytes, password)

    encryptedMessageBytes = encryptedMessageBytes + 0 + 0 + 3
    val byteStr = getBinaryRepresentation(encryptedMessageBytes)
    var i = 0

    val image = readImageFile(inputName)
    if (image != null) {
        if (image.width * image.height >= encryptedMessageBytes.size * 8) {
            for (y in 0 until image.height) {
                for (x in 0 until image.width) {
                    val color = Color(image.getRGB(x, y))
                    var b = color.blue
                    if (i < byteStr.length && byteStr[i] == '0') {
                        b = color.blue and 254
                    } else if (i < byteStr.length && byteStr[i] == '1') {
                        b = color.blue or 1
                    }

                    val colorNew = Color(color.red, color.green, b)
                    image.setRGB(x, y, colorNew.rgb)
                    i++
                }
            }
            saveImageFile(image, outputName)
        } else {
            println("The input image is not large enough to hold this message.")
        }
    }
}

fun encryptMessage(messageBytes: ByteArray, password: ByteArray): ByteArray {
    var res = ByteArray(0)
    for (i in messageBytes.indices) {
        res += messageBytes[i].xor(password[i % password.size])
    }
    return res
}

fun getBinaryRepresentation(message: ByteArray): String {
    var binaryStr = ""
    for (byte in message) {
        binaryStr += Integer.toBinaryString(byte.toInt()).padStart(8, '0')
    }
    return binaryStr
}

fun readImageFile(fileName: String): BufferedImage? {
    var image: BufferedImage? = null
    try {
        val imageFile = File(fileName)
        image = ImageIO.read(imageFile)
    } catch (e: IOException) {
        println("Can't read input file!")
    }
    return image
}

fun saveImageFile(image: BufferedImage, fileName: String) {
    val imageFile = File(fileName)
    ImageIO.write(image, "png", imageFile)
    println("Message saved in $fileName image.")
}

fun getInput(): String {
    println("Task (hide, show, exit):")
    return readln()
}

