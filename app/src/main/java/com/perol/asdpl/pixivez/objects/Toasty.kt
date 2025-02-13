/*
 * MIT License
 *
 * Copyright (c) 2019 Perol_Notsfsssf
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE
 */

package com.perol.asdpl.pixivez.objects

import android.content.Context
import android.widget.Toast
import com.perol.asdpl.pixivez.services.PxEZApp
object Toasty {
    private val lToast: Toast =
        Toast.makeText(PxEZApp.instance.applicationContext, "", Toast.LENGTH_LONG)
    private val sToast: Toast =
        Toast.makeText(PxEZApp.instance.applicationContext, "", Toast.LENGTH_SHORT)

    fun longToast(string: String) {
        lToast.setText(string)
        lToast.show()
    }

    fun shortToast(string: String) {
        sToast.setText(string)
        sToast.show()
    }

    fun longToast(string: Int) {
        lToast.setText(string)
        lToast.show()
    }
    fun shortToast(string: Int) {
        sToast.setText(string)
        sToast.show()
    }
    fun success(context: Context, stringId: Int, length: Int = Toast.LENGTH_SHORT): Toast {
        return Toast.makeText(context, context.getText(stringId), length)
    }

    fun error(context: Context, stringId: Int): Toast {
        return Toast.makeText(context, context.getText(stringId), Toast.LENGTH_SHORT)
    }

    fun info(context: Context, stringId: Int, length: Int = Toast.LENGTH_SHORT): Toast {
        return Toast.makeText(context, context.getText(stringId), length)
    }

    fun warning(context: Context, stringId: Int, length: Int = Toast.LENGTH_SHORT): Toast {
        return Toast.makeText(context, context.getText(stringId), length)
    }

    fun normal(context: Context, stringId: Int, length: Int = Toast.LENGTH_SHORT): Toast {
        return Toast.makeText(context, context.getText(stringId), length)
    }

    fun success(context: Context, string: String, length: Int = Toast.LENGTH_SHORT): Toast {
        return Toast.makeText(context, string, length)
    }

    fun error(context: Context, string: String): Toast {
        return Toast.makeText(context, string, Toast.LENGTH_SHORT)
    }

    fun info(context: Context, string: String, length: Int = Toast.LENGTH_SHORT): Toast {
        return Toast.makeText(context, string, length)
    }

    fun warning(context: Context, string: String, length: Int = Toast.LENGTH_SHORT): Toast {
        return Toast.makeText(context, string, length)
    }

    fun normal(context: Context, string: String, length: Int = Toast.LENGTH_SHORT): Toast {
        return Toast.makeText(context, string, length)
    }
}
