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

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.perol.asdpl.pixivez.R
import com.perol.asdpl.pixivez.adapters.AccountChoiceAdapter
import com.perol.asdpl.pixivez.databinding.ActivityAccountBinding
import com.perol.asdpl.pixivez.repository.AppDataRepository
import com.perol.asdpl.pixivez.services.PxEZApp
import kotlinx.coroutines.runBlocking

class AccountActivity : RinkActivity() {
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
            R.id.nav_add -> {
                startActivity(Intent(this, LoginActivity::class.java))
            }
            R.id.nav_logout -> {
                MaterialAlertDialogBuilder(this).setTitle(R.string.logoutallaccount)
                    .setPositiveButton(android.R.string.ok) { i, j ->
                        runBlocking {
                            AppDataRepository.deleteAllUser()
                        }
                        startActivity(
                            Intent(this@AccountActivity, LoginActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK) // Clear task stack.
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }.setNeutralButton(android.R.string.cancel) { i, j ->
                    }.create().show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private lateinit var binding: ActivityAccountBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.recyclerviewAccount.layoutManager = LinearLayoutManager(this)
        runBlocking {
            val users = AppDataRepository.getAllUser()
            binding.recyclerviewAccount.adapter = AccountChoiceAdapter(
                R.layout.view_account_item, users
            ).apply {
                setOnItemClickListener { adapter, view, position ->
                    this.notifyItemChanged(AppDataRepository.pre.getInt("usernum"))
                    AppDataRepository.pre.setInt("usernum", position)
                    AppDataRepository.currentUser = users[position]
                    this.notifyItemChanged(position)
                    PxEZApp.ActivityCollector.recreate()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.account, menu)
        return true
    }
}
