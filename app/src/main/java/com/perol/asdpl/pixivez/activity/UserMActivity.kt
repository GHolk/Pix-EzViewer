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

package com.perol.asdpl.pixivez.activity

import android.app.Activity
import android.content.*
import android.graphics.Color
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.perol.asdpl.pixivez.R
import com.perol.asdpl.pixivez.adapters.viewpager.UserMPagerAdapter
import com.perol.asdpl.pixivez.databinding.ActivityUserMBinding
import com.perol.asdpl.pixivez.fragments.UserMessageFragment
import com.perol.asdpl.pixivez.objects.AdapterRefreshEvent
import com.perol.asdpl.pixivez.objects.Toasty
import com.perol.asdpl.pixivez.services.GlideApp
import com.perol.asdpl.pixivez.services.PxEZApp
import com.perol.asdpl.pixivez.viewmodel.UserMViewModel
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_user_m.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import java.io.File

class UserMActivity : RinkActivity() {
    companion object {
        const val HIDE_BOOKMARKED_ITEM = "hide_bookmark_item2"
        const val HIDE_DOWNLOADED_ITEM = "hide_downloaded_item"
        const val HIDE_BOOKMARK_ITEM_IN_SEARCH = "hide_bookmark_item_in_search2"
        lateinit var menuD:Menu
        fun start(context: Context, id: Long) {
            val intent = Intent(context, UserMActivity::class.java)
            intent.putExtra("data", id)
            context.startActivity(intent)
        }
    }

    var id: Long = 0
    private val SELECT_IMAGE = 2
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELECT_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImage = data.data
            val filePathColumns = arrayOf(MediaStore.Images.Media.DATA)
            val c = contentResolver.query(selectedImage!!, filePathColumns, null, null, null)
            c!!.moveToFirst()
            val columnIndex = c.getColumnIndex(filePathColumns[0])
            val imagePath = c.getString(columnIndex)
            Toasty.info(this, getString(R.string.uploading), Toast.LENGTH_SHORT).show()
            disposables.add(viewModel.tryToChangeProfile(imagePath).subscribe({
                Toasty.info(this, getString(R.string.upload_success), Toast.LENGTH_SHORT)
                    .show()
            }, {
                it.printStackTrace()
            }, {}))
            c.close()
        }
    }

    val disposables = CompositeDisposable()
    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }

    lateinit var viewModel: UserMViewModel
    lateinit var pre: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding =
            DataBindingUtil.setContentView<ActivityUserMBinding>(this, R.layout.activity_user_m)
                .apply {
                    lifecycleOwner = this@UserMActivity
                }

        binding.lifecycleOwner = this
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT

        id = intent.getLongExtra("data", 1)
        viewModel = ViewModelProvider(this).get(UserMViewModel::class.java)
        pre = PreferenceManager.getDefaultSharedPreferences(this)
        viewModel.getData(id)
        viewModel.hideBookmarked.value = pre.getInt(HIDE_BOOKMARKED_ITEM, 0)
        viewModel.hideDownloaded.value = pre.getBoolean(HIDE_DOWNLOADED_ITEM, false)
        viewModel.userDetail.observe(this, Observer {
            if (it != null) {
                fab.show()
                disposables.add(viewModel.isuser(id).subscribe({
                    if (it) { //用户自己
                        fab.hide()
                        viewModel.hideBookmarked.value = 0
                        mviewpager.currentItem = 2
                        menuD.getItem(1).isVisible = false
                        menuD.getItem(2).isVisible = true
                        menuD.getItem(2).isEnabled = true
                    }
                }, {}))

                binding.user = it


                mviewpager.adapter = UserMPagerAdapter(
                    this, supportFragmentManager,
                    id, UserMessageFragment.newInstance(it)
                )
                mtablayout.setupWithViewPager(mviewpager)
            }
        })
        viewModel.isfollow.observe(this, Observer {
            if (it != null) {
                if (it) {
                    fab.setImageResource(R.drawable.ic_check_white_24dp)
                } else
                    fab.setImageResource(R.drawable.ic_add_white_24dp)
            }


        })
        setSupportActionBar(toolbar)
        supportActionBar!!.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
        }

        fab.setOnClickListener {
            viewModel.onFabclick(id)
        }
        fab.setOnLongClickListener {
            Toasty.info(applicationContext, "Private....", Toast.LENGTH_SHORT).show()
            viewModel.onFabLongClick(id)
            true
        }
        val shareLink = "https://www.pixiv.net/member.php?id=$id"
        imageview_userimage.setOnClickListener {
            disposables.add(viewModel.isuser(id).subscribe({
                var array = resources.getStringArray(R.array.user_profile)
                if (!it) {
                    array = array.copyOfRange(0, 2)
                }
                MaterialAlertDialogBuilder(this).setItems(array) { i, which ->
                    when (which) {
                        0 -> {
                            val clipboard =
                                getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip: ClipData = ClipData.newPlainText("simple text", shareLink)
                            clipboard.setPrimaryClip(clip)
                            Toasty.info(this@UserMActivity, getString(R.string.copied), Toast.LENGTH_SHORT).show()
                        }
                        1 -> {
                            runBlocking {
                                var file: File
                                withContext(Dispatchers.IO) {
                                    val f = GlideApp.with(this@UserMActivity).asFile()
                                        .load(viewModel.userDetail.value!!.user.profile_image_urls.medium)
                                        .submit()
                                    file = f.get()
                                    val target = File(
                                        PxEZApp.storepath,
                                        "user_${viewModel.userDetail.value!!.user.id}.png"
                                    )
                                    file.copyTo(target, overwrite = true)
                                    MediaScannerConnection.scanFile(
                                        PxEZApp.instance, arrayOf(target.path), arrayOf(
                                            MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                                                target.extension
                                            )
                                        )
                                    ) { _, _ ->

                                    }
                                }

                                Toasty.info(this@UserMActivity, getString(R.string.saved), Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                        else -> {
                            val intent = Intent(
                                Intent.ACTION_PICK,
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                            )
                            startActivityForResult(intent, SELECT_IMAGE)
                        }
                    }
                }.create().show()
            }, {}))


        }
        //supportPostponeEnterTransition ();
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_userx, menu)
        menu.getItem(1).isChecked = pre.getInt(HIDE_BOOKMARKED_ITEM, 0)%2 != 0
        if(viewModel.hideBookmarked.value!! > 1) {
                menu.getItem(1).title = getString(R.string.only_bookmarked)
        }
        menuD = menu
        menu.getItem(2).isVisible = false
        menu.getItem(2).isEnabled = false
        menu.getItem(2).isChecked = pre.getBoolean(
            HIDE_DOWNLOADED_ITEM, false
        )
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finishAfterTransition()
            R.id.action_share -> share()
            R.id.action_hideBookmarked -> {
                val pre = PreferenceManager.getDefaultSharedPreferences(this)
                when(viewModel.hideBookmarked.value) {
                    3->{
                        item.title = getString(R.string.hide_bookmarked)
                    }
                    1->{
                        item.title = getString(R.string.only_bookmarked)
                    }
                }
                viewModel.hideBookmarked.value = (viewModel.hideBookmarked.value!!+1)%4
                item.isChecked = !item.isChecked
                pre.edit().putInt( HIDE_BOOKMARKED_ITEM, (viewModel.hideBookmarked.value!!)).apply()
                EventBus.getDefault().post(AdapterRefreshEvent())
            }
            R.id.action_hideDownloaded -> {
                viewModel.hideDownloaded.value = !item.isChecked
                item.isChecked = !item.isChecked
                PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(
                    HIDE_DOWNLOADED_ITEM, item.isChecked
                ).apply()
                EventBus.getDefault().post(AdapterRefreshEvent())
            }
            R.id.action_download -> {
//                val intent =Intent(this,WorkActivity::class.java)
//                intent.putExtra("id",id)
//                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun share() {
        val textIntent = Intent(Intent.ACTION_SEND)
        textIntent.type = "text/plain"
        textIntent.putExtra(Intent.EXTRA_TEXT, "https://www.pixiv.net/member.php?id=$id")
        startActivity(Intent.createChooser(textIntent, getString(R.string.share)))
    }
}
