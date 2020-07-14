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

package com.perol.asdpl.pixivez.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Pair
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.target.ImageViewTarget
import com.bumptech.glide.request.transition.Transition
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.google.android.material.button.MaterialButton
import com.perol.asdpl.pixivez.R
import com.perol.asdpl.pixivez.activity.PictureActivity
import com.perol.asdpl.pixivez.activity.UserMActivity
import com.perol.asdpl.pixivez.objects.DataHolder
import com.perol.asdpl.pixivez.repository.RetrofitRepository
import com.perol.asdpl.pixivez.responses.Illust
import com.perol.asdpl.pixivez.services.GlideApp
import com.perol.asdpl.pixivez.services.PxEZApp
import com.perol.asdpl.pixivez.services.Works
import com.shehuan.niv.NiceImageView
import java.util.ArrayList

// simple Adapter for image item with user imageView
//TODO: rename
class RankingAdapter(
    layoutResId: Int,
    data: List<Illust>?,
    private val R18on: Boolean,
    override var blockTags: List<String>,
    var singleLine: Boolean = false,
    override var hideBookmarked: Boolean = false
) :
    PicItemAdapter(layoutResId, data?.toMutableList()), LoadMoreModule {

    init {
        this.setOnItemClickListener { adapter, view, position ->
            val bundle = Bundle()
            //bundle.putLong("illustid", this.data[position].id)
            //val illustlist = LongArray(this.data.count())
            //for (i in this.data.indices) {
            //    illustlist[i] = this.data[i].id
            //}
            //bundle.putParcelable("illust", this.data[position])
            bundle.putInt("position", position)
            DataHolder.setIllustsList(this.data as ArrayList<Illust>)
            //  bundle.putParcelable(this.data[position].id.toString(), this.data[position])
            val intent = Intent(context, PictureActivity::class.java)
            intent.putExtras(bundle)
            if (PxEZApp.animationEnable) {
                val mainimage = view.findViewById<View>(R.id.item_img)
                val title = view.findViewById<TextView>(R.id.textview_title)
                //if (singleLine) title.maxLines = 1
                val userImage = view.findViewById<View>(R.id.imageview_user)

                val options = ActivityOptions.makeSceneTransitionAnimation(
                    context as Activity,
                    Pair.create(
                        mainimage,
                        "mainimage"
                    ),
                    Pair.create(title, "title"),
                    Pair.create(userImage, "userimage")
                )
                ContextCompat.startActivity(context, intent, options.toBundle())
            } else
                ContextCompat.startActivity(context, intent, null)
        }
    }

    @SuppressLint("InflateParams")
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        addFooterView(LayoutInflater.from(context).inflate(R.layout.foot_list, null))
        animationEnable = true
        setAnimationWithDefault(AnimationType.ScaleIn)
        this.loadMoreModule?.preLoadNumber = 12
    }
    override fun convert(helper: BaseViewHolder, item: Illust) {
        if (hideBookmarked && item.is_bookmarked){
            helper.itemView.visibility = View.GONE
            helper.itemView.layoutParams.apply {
                height = 0
                width = 0
            }
            return
        }
        val tags = item.tags.map {
            it.name
        }
        var needBlock = false
        for (i in blockTags) {
            if (tags.contains(i)) {
                needBlock = true
                break
            }
        }
        if (blockTags.isNotEmpty() && tags.isNotEmpty() && needBlock) {
            helper.itemView.visibility = View.GONE
            helper.itemView.layoutParams.apply {
                height = 0
                width = 0
            }
            return
        } else {
            helper.itemView.visibility = View.VISIBLE
            helper.itemView.layoutParams.apply {
                height = LinearLayout.LayoutParams.WRAP_CONTENT
                width = LinearLayout.LayoutParams.MATCH_PARENT
            }
        }
        val typedValue = TypedValue()
        context.theme.resolveAttribute(R.attr.colorPrimary, typedValue, true)
        val colorPrimary = typedValue.resourceId
        context.theme.resolveAttribute(R.attr.badgeTextColor, typedValue, true)
        val badgeTextColor = typedValue.resourceId
        helper.getView<MaterialButton>(R.id.save).setOnClickListener {
            Works.imageDownloadAll(item)
        }
        helper.setText(R.id.textview_title, item.title)
        if (!singleLine) helper.setText(R.id.textview_context, item.user.name)
        //helper.setTextColor(R.id.textview_context, ContextCompat.getColor(context, colorPrimary))
        helper.setTextColor(
            R.id.like, if (item.is_bookmarked) {
                ContextCompat.getColor(context, badgeTextColor)
            } else {
                ContextCompat.getColor(context, colorPrimary)
            }
        )

        helper.getView<MaterialButton>((R.id.like)).setOnClickListener { v ->
            val textView = v as Button
            val retrofit = RetrofitRepository.getInstance()
            if (item.is_bookmarked) {
                retrofit.postUnlikeIllust(item.id).subscribe({
                    textView.setTextColor(ContextCompat.getColor(context, colorPrimary))
                    item.is_bookmarked = false
                }, {}, {})
            } else {
                retrofit.postLikeIllust(item.id)!!.subscribe({
                    textView.setTextColor(
                        ContextCompat.getColor(context, badgeTextColor)
                    )
                    item.is_bookmarked = true
                }, {}, {})
            }
        }

        val constraintLayout =
            helper.itemView.findViewById<ConstraintLayout>(R.id.constraintLayout_num)
        when (item.type) {
            "illust" -> if (item.meta_pages.isEmpty()) {
                constraintLayout.visibility = View.INVISIBLE
            } else if (item.meta_pages.isNotEmpty()) {
                constraintLayout.visibility = View.VISIBLE
                helper.setText(R.id.textview_num, item.meta_pages.size.toString())
            }
            "ugoira" -> {
                constraintLayout.visibility = View.VISIBLE
                helper.setText(R.id.textview_num, "Gif")
            }
            else -> {
                constraintLayout.visibility = View.VISIBLE
                helper.setText(R.id.textview_num, "CoM")
            }
        }
        val mainimage = helper.getView<ImageView>(R.id.item_img)
        mainimage.setTag(R.id.tag_first, item.image_urls.medium)
        val imageViewUser = helper.getView<NiceImageView>(R.id.imageview_user)
        if (item.user.is_followed)
            imageViewUser.setBorderColor(ContextCompat.getColor(context, badgeTextColor)) // Color.YELLOW
        else
            imageViewUser.setBorderColor(ContextCompat.getColor(context, colorPrimary))
        imageViewUser.setOnClickListener {
            val intent = Intent(context, UserMActivity::class.java)
            intent.putExtra("data", item.user.id)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            if (PxEZApp.animationEnable) {
                val options = ActivityOptions.makeSceneTransitionAnimation(
                    context as Activity,
                    Pair.create(imageViewUser, "UserImage")
                )
                context.startActivity(intent, options.toBundle())
            } else
                context.startActivity(intent)
        }
        imageViewUser.setOnLongClickListener {
            val id = item.user.id
            val retrofitRespository: RetrofitRepository = RetrofitRepository.getInstance()
            if (!item.user.is_followed) {
                retrofitRespository.postfollowUser(id, "public").subscribe({
                    item.user.is_followed = true
                    imageViewUser.setBorderColor(ContextCompat.getColor(context, badgeTextColor)) // Color.YELLOW
                }, {}, {})
            } else {
                retrofitRespository.postunfollowUser(id).subscribe({
                    item.user.is_followed = false
                    imageViewUser.setBorderColor(ContextCompat.getColor(context, colorPrimary))
                }, {}, {}
                )
            }
            true
        }
        imageViewUser.setTag(R.id.tag_first, item.user.profile_image_urls.medium)

        GlideApp.with(imageViewUser.context).load(item.user.profile_image_urls.medium).circleCrop().diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(object : ImageViewTarget<Drawable>(imageViewUser) {
                override fun setResource(resource: Drawable?) {
                    imageViewUser.setImageDrawable(resource)
                }

                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?
                ) {
                    if (item.user.profile_image_urls.medium === imageViewUser.getTag(R.id.tag_first)) {
                        super.onResourceReady(resource, transition)

                    }
                }
            })


        val needSmall = item.height > 1500 || item.height > 1500
        val loadUrl = if (needSmall) {
            item.image_urls.square_medium
        } else {
            item.image_urls.medium
        }

        if (!R18on) {
            val isr18 = tags.contains("R-18") || tags.contains("R-18G")
            if (isr18) {
                GlideApp.with(mainimage.context)
                    .load(R.drawable.h)
                    .into(mainimage)
            } else {
                GlideApp.with(mainimage.context).load(loadUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .transition(withCrossFade()).placeholder(R.color.halftrans)
                    .into(object : ImageViewTarget<Drawable>(mainimage) {
                        override fun setResource(resource: Drawable?) {
                            mainimage.setImageDrawable(resource)
                        }

                        override fun onResourceReady(
                            resource: Drawable,
                            transition: Transition<in Drawable>?
                        ) {
                            if (mainimage.getTag(R.id.tag_first) === item.image_urls.medium) {
                                super.onResourceReady(resource, transition)
                            }
                        }
                    })
            }
        } else {
            GlideApp.with(mainimage.context).load(loadUrl).transition(withCrossFade())
                .placeholder(R.color.halftrans)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(ContextCompat.getDrawable(mainimage.context, R.drawable.ai))
                .into(object : ImageViewTarget<Drawable>(mainimage) {
                    override fun setResource(resource: Drawable?) {
                        mainimage.setImageDrawable(resource)
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        transition: Transition<in Drawable>?
                    ) {
                        if (mainimage.getTag(R.id.tag_first) === item.image_urls.medium) {
                            super.onResourceReady(resource, transition)
                        }

                    }
                })
        }
    }
}
