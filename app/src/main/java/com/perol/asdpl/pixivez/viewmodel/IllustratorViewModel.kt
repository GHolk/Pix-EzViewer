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

package com.perol.asdpl.pixivez.viewmodel

import androidx.lifecycle.MutableLiveData
import com.perol.asdpl.pixivez.repository.RetrofitRepository
import com.perol.asdpl.pixivez.responses.SearchUserResponse

class IllustratorViewModel : BaseViewModel() {
    val retrofitRepository = RetrofitRepository.getInstance()
    val userpreviews = MutableLiveData<ArrayList<SearchUserResponse.UserPreviewsBean>>()
    val adduserpreviews = MutableLiveData<ArrayList<SearchUserResponse.UserPreviewsBean>?>()
    val nextUrl = MutableLiveData<String>()
    val isRefreshing = MutableLiveData(false)

    fun onLoadMore(string: String) {
        retrofitRepository.getNextUser(string).subscribe({
            adduserpreviews.value = it.user_previews as ArrayList<SearchUserResponse.UserPreviewsBean>?
            nextUrl.value = it.next_url
        }, {
            adduserpreviews.value = null
        }, {}).add()
    }

    fun onRefresh(user_id: Long, restrict: String, get_following: Boolean) {
        isRefreshing.value = true
        if (get_following) {
            retrofitRepository.getUserFollowing(user_id, restrict).subscribe({
                userpreviews.value = it.user_previews as ArrayList<SearchUserResponse.UserPreviewsBean>?
                nextUrl.value = it.next_url
            }, {}, { isRefreshing.value = false }).add()
        }
        else {
            retrofitRepository.getUserFollower(user_id).subscribe({
                userpreviews.value = it.user_previews as ArrayList<SearchUserResponse.UserPreviewsBean>?
                nextUrl.value = it.next_url
            }, {}, { isRefreshing.value = false }).add()
        }
    }
}
