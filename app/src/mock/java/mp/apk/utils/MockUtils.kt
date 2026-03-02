package mp.apk.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object MockUtils {

    fun getMockImageUri(context: Context, drawableId: Int, fileName: String): Uri {

        val file = File(context.cacheDir, fileName)

        if (!file.exists()) {
            val bitmap = BitmapFactory.decodeResource(context.resources, drawableId)
            bitmap?.let { btm ->
                FileOutputStream(file).use { out ->
                    btm.compress(Bitmap.CompressFormat.JPEG, 100, out)
                }
            }
        }
        return Uri.fromFile(file)
    }
}