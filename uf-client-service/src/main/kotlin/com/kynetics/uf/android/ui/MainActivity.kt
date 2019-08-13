/*
 *
 *  Copyright © 2017-2019  Kynetics  LLC
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 */

package com.kynetics.uf.android.ui

import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.kynetics.uf.android.R
import com.kynetics.uf.android.UpdateFactoryService
import com.kynetics.uf.android.ui.fragment.AuthorizationDialogFragment
import com.kynetics.uf.android.ui.fragment.AuthorizationDialogFragment.OnAuthorization
import com.kynetics.uf.android.ui.fragment.UFPreferenceFragment

/**
 * @author Daniele Sergio
 */
class MainActivity : AppCompatActivity(), OnAuthorization {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.update_factory_label)
        val intent = intent
        when (val type = intent.getIntExtra(INTENT_TYPE_EXTRA_VARIABLE, 0)) {
            INTENT_TYPE_EXTRA_VALUE_SETTINGS -> {
                setTheme(R.style.AppTheme)
                setContentView(R.layout.activity_main)
                val actionBar = supportActionBar
                actionBar!!.setDisplayHomeAsUpEnabled(true)
                changePage(UFPreferenceFragment.newInstance())
            }
            INTENT_TYPE_EXTRA_VALUE_DOWNLOAD, INTENT_TYPE_EXTRA_VALUE_REBOOT -> {
                setTheme(R.style.AppTransparentTheme)
                showAuthorizationDialog(type)
            }
            else -> throw IllegalArgumentException("")
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            UpdateFactoryService.ufServiceCommand!!.configureService()
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showAuthorizationDialog(type: Int) {
        val newFragment = AuthorizationDialogFragment.newInstance(
            getString(
                if (type == INTENT_TYPE_EXTRA_VALUE_DOWNLOAD)
                    R.string.update_download_title
                else
                    R.string.update_started_title
            ),
            getString(
                if (type == INTENT_TYPE_EXTRA_VALUE_DOWNLOAD)
                    R.string.update_download_content
                else
                    R.string.update_started_content
            ),
            getString(android.R.string.ok),
            getString(android.R.string.cancel)
        )
        newFragment.show(supportFragmentManager, "authorization")
    }

    private fun changePage(fragment: Fragment) {
        val tx = supportFragmentManager.beginTransaction()
        tx.replace(R.id.frame_layout, fragment)
        tx.commit()
    }

    override fun onPause() {
        super.onPause()
        overridePendingTransition(0, 0)
    }

    override fun onAuthorizationGrant() {
        UpdateFactoryService.ufServiceCommand!!.authorizationGranted()
        finishActivity()
    }

    override fun onAuthorizationDenied() {
        UpdateFactoryService.ufServiceCommand!!.authorizationDenied()
        finishActivity()
    }

    private fun finishActivity() {
        Handler().postDelayed({ finish() }, 500)
    }

    companion object {
        const val INTENT_TYPE_EXTRA_VARIABLE = "EXTRA_TYPE"
        const val INTENT_TYPE_EXTRA_VALUE_SETTINGS = 0
        const val INTENT_TYPE_EXTRA_VALUE_DOWNLOAD = 1
        const val INTENT_TYPE_EXTRA_VALUE_REBOOT = 2
    }
}
