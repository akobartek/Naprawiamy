package pl.sokolowskibartlomiej.naprawiamy.utils

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.TypedValue
import android.view.View
import android.widget.EditText
import androidx.annotation.AttrRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import pl.sokolowskibartlomiej.naprawiamy.R
import java.text.SimpleDateFormat
import java.util.*


fun Activity.showNoInternetDialogWithTryAgain(
    function: () -> Unit,
    functionCancel: () -> Unit
): Unit =
    AlertDialog.Builder(this)
        .setTitle(R.string.no_internet_title)
        .setMessage(R.string.no_internet_reconnect_message)
        .setCancelable(false)
        .setPositiveButton(R.string.try_again) { dialog, _ ->
            dialog.dismiss()
            if (checkNetworkConnection()) function()
            else if (!isFinishing && !isDestroyed) showNoInternetDialogWithTryAgain(
                function,
                functionCancel
            )
        }
        .setNegativeButton(R.string.cancel) { dialog, _ ->
            dialog.dismiss()
            functionCancel()
        }
        .create()
        .show()

@Suppress("DEPRECATION")
fun Activity.checkNetworkConnection(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val capabilities =
            connectivityManager?.getNetworkCapabilities(connectivityManager.activeNetwork)
        capabilities != null
    } else {
        val activeNetworkInfo = connectivityManager?.activeNetworkInfo
        activeNetworkInfo != null && activeNetworkInfo.isConnected
    }
}

fun Activity.tryToRunFunctionOnInternet(function: () -> Unit, functionCancel: () -> Unit) {
    if (checkNetworkConnection()) {
        try {
            function()
        } catch (exc: Throwable) {
            showNoInternetDialogWithTryAgain(function, functionCancel)
        }
    } else {
        showNoInternetDialogWithTryAgain(function, functionCancel)
    }
}

fun Context.getAttributeColor(@AttrRes attributeId: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(attributeId, typedValue, true)
    return if (typedValue.resourceId == 0) typedValue.data else ContextCompat.getColor(
        this,
        typedValue.resourceId
    )
}

fun EditText.enable() {
    isFocusable = true
    isEnabled = true
    isFocusableInTouchMode = true
}

fun EditText.disable() {
    isFocusable = false
    isEnabled = false
    isFocusableInTouchMode = false
}

fun StaticLayout.textWidth(): Int {
    var width = 0f
    for (i in 0 until lineCount) {
        width = width.coerceAtLeast(getLineWidth(i))
    }
    return width.toInt()
}

fun newStaticLayout(
    source: CharSequence,
    paint: TextPaint,
    width: Int,
    alignment: Layout.Alignment,
    spacingMult: Float,
    spacingAdd: Float,
    includePad: Boolean
): StaticLayout {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        StaticLayout.Builder.obtain(source, 0, source.length, paint, width).apply {
            setAlignment(alignment)
            setLineSpacing(spacingAdd, spacingMult)
            setIncludePad(includePad)
        }.build()
    } else {
        @Suppress("DEPRECATION")
        (StaticLayout(source, paint, width, alignment, spacingMult, spacingAdd, includePad))
    }
}

fun Date.format(): String {
    val simpleDateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    return simpleDateFormat.format(this)
}

fun View.isRtl() = layoutDirection == View.LAYOUT_DIRECTION_RTL

fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t