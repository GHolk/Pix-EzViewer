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

package com.perol.asdpl.pixivez.fragments


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.tabs.TabLayout
import com.perol.asdpl.pixivez.R
import com.perol.asdpl.pixivez.activity.UserMActivity
import com.perol.asdpl.pixivez.adapters.PicItemAdapter
import com.perol.asdpl.pixivez.adapters.RankingAdapter
import com.perol.asdpl.pixivez.adapters.RecommendAdapter
import com.perol.asdpl.pixivez.dialog.TagsShowDialog
import com.perol.asdpl.pixivez.objects.AdapterRefreshEvent
import com.perol.asdpl.pixivez.objects.BaseFragment
import com.perol.asdpl.pixivez.repository.AppDataRepository
import com.perol.asdpl.pixivez.services.PxEZApp
import com.perol.asdpl.pixivez.ui.GridItemDecoration
import com.perol.asdpl.pixivez.viewmodel.UserBookMarkViewModel
import kotlinx.android.synthetic.main.fragment_user_book_mark.*
import kotlinx.coroutines.runBlocking
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [UserBookMarkFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */

class UserBookMarkFragment : BaseFragment(), TagsShowDialog.Callback {
    @SuppressLint("InflateParams")
    override fun loadData() {
        viewModel!!.first(param1!!, pub).doOnSuccess {
            if (it) {
                val view = layoutInflater.inflate(R.layout.header_bookmark, null)
                val imagebutton = view.findViewById<ImageView>(R.id.imagebutton_showtags)
                picItemAdapter.addHeaderView(view)
                imagebutton.setOnClickListener {
                    showTagDialog()
                }
            }
        }.doOnError {

        }.subscribe()
    }

    private var exitTime = 0L
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        picItemAdapter =
            if(PreferenceManager.getDefaultSharedPreferences(PxEZApp.instance).getBoolean("show_user_img_bookmarked",true)){
                RankingAdapter(
                    R.layout.view_ranking_item,
                    null,
                    isR18on,
                    blockTags,
                    singleLine = false,
                    viewActivity.viewModel.hideBookmarked.value!!,
                    viewActivity.viewModel.hideDownloaded.value!!)
            } else{
                RecommendAdapter(
                    R.layout.view_recommand_item,
                    null,
                    isR18on,
                    blockTags,
                    viewActivity.viewModel.hideBookmarked.value!!,
                    viewActivity.viewModel.hideDownloaded.value!!)
            }


        mrecyclerview.apply{
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            adapter = picItemAdapter
            addItemDecoration(GridItemDecoration())
        }
        picItemAdapter.loadMoreModule?.setOnLoadMoreListener {
            viewModel!!.onLoadMoreListener()
        }

        mrefreshlayout.setOnRefreshListener {
            viewModel!!.onRefreshListener(param1!!, pub, null)
        }
        requireActivity().findViewById<TabLayout>(R.id.mtablayout)?.getTabAt(2)
            ?.view?.setOnClickListener {
            if ((System.currentTimeMillis() - exitTime) > 3000) {

                exitTime = System.currentTimeMillis()
            } else {
                mrecyclerview?.smoothScrollToPosition(0)
            }

        }
    }

    override fun onClick(string: String, public: String) {
        viewModel!!.onRefreshListener(
            param1!!, public, if (string.isNotBlank()) {
                string
            } else {
                null
            }
        )
    }

    private fun lazyLoad() {
        viewModel = ViewModelProvider(this).get(UserBookMarkViewModel::class.java)
        this.viewActivity = activity as UserMActivity

        viewModel!!.nexturl.observe(this, Observer {
            if (it.isNullOrEmpty()) {
                picItemAdapter.loadMoreEnd()
            } else {
                picItemAdapter.loadMoreComplete()
            }
        })
        viewModel!!.data.observe(this, Observer {
            if (it != null) {
                mrefreshlayout.isRefreshing = false
                picItemAdapter.setNewData(it.toMutableList())
            }

        })
        viewModel!!.adddata.observe(this, Observer {
            if (it != null) {
                picItemAdapter.addData(it)
                picItemAdapter.loadMoreComplete()
            }
        })
        viewModel!!.tags.observe(this, Observer {

        })

    }


    var first = true

    private var param1: Long? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getLong(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        lazyLoad()

    }

    private var pub = "public"

    var viewModel: UserBookMarkViewModel? = null
    private lateinit var viewActivity: UserMActivity


    private fun showTagDialog() {
        val arrayList = ArrayList<String>()
        val arrayList1 = ArrayList<Int>()
        if (viewModel!!.tags.value != null) {
            val tagsShowDialog = TagsShowDialog()
            tagsShowDialog.callback = this
            for (i in viewModel!!.tags.value!!.bookmark_tags) {
                arrayList.add(i.name)
                arrayList1.add(i.count)
            }
            val bundle = Bundle()
            bundle.putStringArrayList("tags", arrayList)
            bundle.putIntegerArrayList("counts", arrayList1)
            bundle.putString("nexturl", viewModel!!.tags.value!!.next_url)
            bundle.putLong("id", param1!!)
            tagsShowDialog.arguments = bundle
            tagsShowDialog.show(childFragmentManager)
        }


    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: AdapterRefreshEvent) {
        runBlocking {
            val allTags = blockViewModel.getAllTags()
            blockTags = allTags.map {
                it.name
            }
            val id = AppDataRepository.getUser().userid
                picItemAdapter.hideBookmarked =
                    if (param1 != id) {
                        viewActivity.viewModel.hideBookmarked.value!!
                    } else 0
                picItemAdapter.hideDownloaded =
                    if (param1 == id) {
                        viewActivity.viewModel.hideDownloaded.value!!
                    } else false
            picItemAdapter.blockTags = blockTags
            picItemAdapter.notifyDataSetChanged()
        }
    }
    private lateinit var picItemAdapter: PicItemAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_user_book_mark, container, false)
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment UserBookMarkFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: Long, param2: String) =
            UserBookMarkFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
