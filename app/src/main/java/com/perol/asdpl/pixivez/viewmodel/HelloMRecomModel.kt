/*
 * MIT License
 *
 * Copyright (c) 2020 ultranity
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

package com.perol.asdpl.pixivez.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.perol.asdpl.pixivez.responses.Illust
import com.perol.asdpl.pixivez.responses.RecommendResponse
import io.reactivex.Observable

class HelloMRecomModel : BaseViewModel() {
    val illusts = MutableLiveData<ArrayList<Illust>?>()
    val addillusts = MutableLiveData<ArrayList<Illust>?>()
    var nextUrl = MutableLiveData<String>()
    fun onRefreshRx(): Observable<RecommendResponse> = retrofit.getRecommend()
    fun onLoadMoreRx(nextUrl: String) = retrofit.getNextIllustRecommended(nextUrl)

    fun onLoadMorePicRequested() {
        retrofit.getNextIllustRecommended(nextUrl.value!!).subscribe({
            nextUrl.value = it.next_url
            addillusts.value = it.illusts
        }, {
            addillusts.value = null
        }, {}).add()
    }

    fun onRefreshListener() {
        Log.d("init", "gettingRecommend")
        retrofit.getRecommend().subscribe({
            Log.d("init", "getRecommend")
            nextUrl.value = it.next_url
            illusts.value = it.illusts
        }, {
            Log.d("init", "getRecommend fail $it")
            illusts.value = null
        }, {}).add()
    }
}
