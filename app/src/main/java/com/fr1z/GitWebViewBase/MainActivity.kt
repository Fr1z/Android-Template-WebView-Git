package com.fr1z.GitWebViewBase

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.errors.RepositoryNotFoundException
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import java.io.File

class MainActivity : AppCompatActivity() {

    // Repository remote and branch
    private val repoURL = "https://github.com/Fr1z/TaskBoard-JS.git"
    private val repoBranch = "webclient" // Edit with your  branch
    private val localWebHostName = "localstack.com"
    private val entryPoint = "dash.html" //replace with your index.html relative path

    private val permitOfflineUse: Boolean = true //set to false if internet is always required

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val localRepoDir = "${applicationContext.filesDir}/localRepo"

        // WebView setup
        val webView: WebView = findViewById(R.id.webView)
        webView.webViewClient = CustomWebClient(this, webView, localRepoDir, localWebHostName)

        //Log WebViewVersion
        val webViewPackageInfo =  WebView.getCurrentWebViewPackage()?.versionName ?: "Not available"
        Log.d("WebView", "WebView version: $webViewPackageInfo")

        //Start Loading Animation with local static asset
        webView.loadUrl("file:///android_asset/loading.html")

        // Initialize local repo if not exists, if exists pull updates
        CoroutineScope(Dispatchers.Main).launch {
            val success = initializeGitRepo(repoURL, repoBranch, localRepoDir)
            if (success) {
                // Load local HTML files (Entrypoint)
                webView.loadUrl("https://$localWebHostName/$entryPoint")
                Log.i("Git", "WebView loaded entrypoint")
            } else {
                // Check if local repo is available for offline use
                if (isGitRepository(localRepoDir) && permitOfflineUse ) {
                    // Load local repo in offline mode
                    webView.loadUrl("https://$localWebHostName/$entryPoint")
                } else {
                    // Show error page
                    webView.loadUrl("file:///android_asset/error.html")
                }

            }
        }

    }

    private suspend fun initializeGitRepo(repoURL: String, repoBranch: String, localRepoDir: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val repoDir = File(localRepoDir)
                if (!repoDir.exists()) {
                    Log.i("Git", "Local repository not found. Cloning new one...")
                    Git.cloneRepository()
                        .setURI(repoURL)
                        .setDirectory(repoDir)
                        .setBranch(repoBranch)
                        //.setCredentialsProvider(getCredentials()) // Un-Comment if repo need user authentication
                        .call()
                } else {
                    Log.i("Git", "Local repository found. Pulling latest changes...")
                    Git.open(repoDir).pull().call()
                }
                true // Sync succeeded
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("Git", "Error during Git operation: ${e.message}", e)
                false // Sync Failed
            }
        }
    }

    private fun isGitRepository(localRepoDir: String): Boolean {
        return try {
            val repoDir = File(localRepoDir)
            Git.open(repoDir).use { true } // true if a valid repo exists
        } catch (e: RepositoryNotFoundException) {
            false // false if not a git repository
        } catch (e: Exception) {
            Log.e("Git", "Unexpected error: ${e.message}", e)
            false
        }
    }
    private fun getCredentials(): UsernamePasswordCredentialsProvider {
        // Replace credential if auth needed
        val username = "your_username" // Edit with your git username
        val password = "your_password" // Edit with your password/token
        return UsernamePasswordCredentialsProvider(username, password)
    }
}
