/* Copyright 2018 Pascal Fuhrmann

    This file is part of substitution_schedule.

    substitution_schedule is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    substitution_schedule is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with substitution_schedule.  If not, see <http://www.gnu.org/licenses/>.
  */
package de.pascalfuhrmann.schedule.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.jesusm.kfingerprintmanager.KFingerprintManager
import de.pascalfuhrmann.schedule.authentication.requestData
import de.pascalfuhrmann.schedule.scraper.AsyncDataReceiverTask
import de.pascalfuhrmann.schedule.utility.PreferenceManager
import de.pascalfuhrmann.schedule.R


/**
 * A login screen that offers login via username/password.
 */
class LoginActivity : AppCompatActivity() {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private var mAuthTask: UserLoginTask? = null
    private var mFingerPrintHandler: FingerprintHandler? = null
    private var mUsernameView: AutoCompleteTextView? = null
    private var mPasswordView: AutoCompleteTextView? = null
    private var mProgressView: View? = null
    private var mLoginFormView: View? = null
    private var mUserSignInButton: Button? = null
    private var mFingerprintSet: ImageView? = null
    private var mFingerprintUse: ImageView? = null

    /**
     * Creates the GUI for the login as well as handling the events of the created
     * views and the button.
     *
     * @param savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val preferences = PreferenceManager.instance
        preferences.initialize(this)

        // Set up login form
        mUsernameView = findViewById<View>(R.id.user) as AutoCompleteTextView
        mLoginFormView = findViewById(R.id.login_form)
        mProgressView = findViewById(R.id.login_progress)
        mUserSignInButton = findViewById<View>(R.id.user_sign_in_button) as Button
        mUserSignInButton!!.setOnClickListener { attemptLogin() }
        mPasswordView = findViewById<View>(R.id.password) as AutoCompleteTextView
        mPasswordView!!.setOnEditorActionListener(TextView.OnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                attemptLogin()
                return@OnEditorActionListener true
            }
            false
        })

        mFingerPrintHandler = FingerprintHandler()

        //setup fingerprint register
        mFingerprintSet = findViewById(R.id.fingerprint_set)
        mFingerprintSet!!.setOnClickListener {
            val username = mUsernameView!!.text.toString()
            val password = mPasswordView!!.text.toString()

            if (username.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Please enter your username and password first.", Toast.LENGTH_LONG).show()
            } else {
                mFingerPrintHandler!!.fingerprintEncryption( "$username:$password")
            }
        }

        //setup fingerprint login
        mFingerprintUse = findViewById(R.id.fingerprint_use)
        mFingerprintUse!!.setOnClickListener {attemptFingerprintLogin()}

        //if there is user data just log in
        if (!PreferenceManager.instance.userData.isNullOrBlank()) {
            attemptFingerprintLogin()
        }
    }

    private fun attemptFingerprintLogin() {
        if (mFingerPrintHandler == null) {
            return
        }

        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection.", Toast.LENGTH_LONG).show()
            return
        }

        if (PreferenceManager.instance.userData.isNullOrBlank()) {
            Toast.makeText(this, "Consider registering a fingerprint first.", Toast.LENGTH_LONG).show()
            return
        }


        mFingerPrintHandler!!.fingerprintDecryption(PreferenceManager.instance.userData!!)
    }

    /**
     * Launches the UserLoginTask which handles the whole login
     * and opens the MainActivity if everything succeeded.
     *
     * @see MainActivity
     *
     * @see UserLoginTask
     */
    private fun attemptLogin() {
        if (mAuthTask != null) {
            return
        }

        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection.", Toast.LENGTH_LONG).show()
            return
        }

        // Reset errors.
        mUsernameView!!.error = null
        mPasswordView!!.error = null

        // Store values at the time of the login attempt.
        val username = mUsernameView!!.text.toString()
        val password = mPasswordView!!.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView!!.error = getString(R.string.error_invalid_password)
            focusView = mPasswordView
            cancel = true
        }

        // Check for an username
        if (TextUtils.isEmpty(username)) {
            mUsernameView!!.error = getString(R.string.error_field_required)
            focusView = mUsernameView
            cancel = true
        }

        //Check for an password
        if (TextUtils.isEmpty(password)) {
            mPasswordView!!.error = getString(R.string.error_field_required)
            focusView = mPasswordView
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView!!.requestFocus()
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true)
            mAuthTask = UserLoginTask(username, password)
            mAuthTask!!.execute(null as Void?)
        }
    }

    /**
     * Checks whether there is an internet connection or not
     */
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.isDefaultNetworkActive
    }

    /**
     * Checks whether the password is valid or not.
     *
     * @param password
     * @return
     */
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 3
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private fun showProgress(show: Boolean) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime)

        mLoginFormView!!.visibility = if (show) View.GONE else View.VISIBLE
        mLoginFormView!!.animate().setDuration(shortAnimTime.toLong()).alpha(
                (if (show) 0 else 1).toFloat()).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                mLoginFormView!!.visibility = if (show) View.GONE else View.VISIBLE
            }
        })

        mProgressView!!.visibility = if (show) View.VISIBLE else View.GONE
        mProgressView!!.animate().setDuration(shortAnimTime.toLong()).alpha(
                (if (show) 1 else 0).toFloat()).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                mProgressView!!.visibility = if (show) View.VISIBLE else View.GONE
            }
        })
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    private inner class UserLoginTask(val username: String, val password: String)
        : AsyncDataReceiverTask(this@LoginActivity, Intent(this@LoginActivity, MainActivity::class.java)) {

        override fun doInBackground(vararg params: Void?): Pair<Boolean, String> {
            return requestData(username, password)
        }

        /**
         * After the HTMLHandler has done his job the returned htmlContent
         * will be checked for any errors the either the user will be
         * prompted to login again or the MainActivity is being opened with
         * the htmlContent.
         *
         * @param result
         */
        override fun onPostExecute(result: Pair<Boolean, String>) {
            super.onPostExecute(result)
            mAuthTask = null
            showProgress(false)

            if (result.first) {
                openMainActivity()
            } else {
                //close keyboard
                val imm = this@LoginActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(mUsernameView!!.windowToken, 0)

                //set error at the username input field and focus it
                mUsernameView!!.error = getString(R.string.error_sign_in_failed)
                mUsernameView!!.requestFocus()
            }
        }

        private fun openMainActivity() {
            val imm = this@LoginActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(mUsernameView!!.windowToken, 0)
            finish()
        }

        override fun onCancelled() {
            mAuthTask = null
            showProgress(false)
        }
    }

    private inner class FingerprintHandler {
        fun fingerprintEncryption(messageToEncrypt: String) {
            createFingerprintManagerInstance(this@LoginActivity).encrypt(messageToEncrypt, object : KFingerprintManager.EncryptionCallback {
                override fun onFingerprintNotRecognized() {
                    Toast.makeText(this@LoginActivity,
                            "Fingerprint not recognized.",
                            Toast.LENGTH_LONG).show()
                    fingerprintEncryption(messageToEncrypt)
                }

                override fun onAuthenticationFailedWithHelp(help: String?) {
                    Toast.makeText(this@LoginActivity,
                            "Authentication failed.",
                            Toast.LENGTH_LONG).show()
                }

                override fun onFingerprintNotAvailable() {
                    Toast.makeText(this@LoginActivity,
                            "Fingerprint is not available on your device.",
                            Toast.LENGTH_LONG).show()
                }

                override fun onEncryptionSuccess(messageEncrypted: String) {
                    PreferenceManager.instance.userData = messageEncrypted
                    PreferenceManager.instance.updateValues(this@LoginActivity)

                    Toast.makeText(this@LoginActivity,
                            "Encryption succeeded.",
                            Toast.LENGTH_LONG).show()

                    if (!isNetworkAvailable()) {
                        Toast.makeText(this@LoginActivity, "No internet connection.", Toast.LENGTH_LONG).show()
                        return
                    }

                    val temp = messageToEncrypt.split(":")
                    mAuthTask = UserLoginTask(temp[0], temp[1])
                    mAuthTask!!.execute(null as Void?)
                }

                override fun onEncryptionFailed() {
                    Toast.makeText(this@LoginActivity,
                            "Encryption failed.",
                            Toast.LENGTH_LONG).show()
                }

                override fun onCancelled() {
                    Toast.makeText(this@LoginActivity,
                            "Fingerprint authentication cancelled.",
                            Toast.LENGTH_LONG).show()
                }
            }, this@LoginActivity.supportFragmentManager)
        }

        fun fingerprintDecryption(encryptedUserData: String) {
            createFingerprintManagerInstance(this@LoginActivity).decrypt(encryptedUserData, object : KFingerprintManager.DecryptionCallback {
                override fun onDecryptionSuccess(messageDecrypted: String) {
                    Toast.makeText(this@LoginActivity,
                            "Authorization successful.",
                            Toast.LENGTH_LONG).show()
                    val temp = messageDecrypted.split(":")
                    mAuthTask = UserLoginTask(temp[0], temp[1])
                    mAuthTask!!.execute(null as Void?)
                }

                override fun onDecryptionFailed() {
                    Toast.makeText(this@LoginActivity,
                            "User data decryption failed.",
                            Toast.LENGTH_LONG).show()
                }

                override fun onFingerprintNotRecognized() {
                    Toast.makeText(this@LoginActivity,
                            "Fingerprint not recognized.",
                            Toast.LENGTH_LONG).show()
                    fingerprintDecryption(encryptedUserData)
                }

                override fun onAuthenticationFailedWithHelp(help: String?) {
                    Toast.makeText(this@LoginActivity,
                            "Authentication failed with help.",
                            Toast.LENGTH_LONG).show()
                }

                override fun onFingerprintNotAvailable() {
                    Toast.makeText(this@LoginActivity,
                            "Fingerprint is not available on your device.",
                            Toast.LENGTH_LONG).show()
                }

                override fun onCancelled() {
                    Toast.makeText(this@LoginActivity,
                            "Fingerprint authorization cancelled.",
                            Toast.LENGTH_LONG).show()
                }
            }, this@LoginActivity.supportFragmentManager)
        }

        fun createFingerprintManagerInstance(context: Context): KFingerprintManager {
            //fingerprintManager.setAuthenticationDialogStyle(dialogTheme)
            return KFingerprintManager(context, PreferenceManager.KEY_STORAGE)
        }
    }
}