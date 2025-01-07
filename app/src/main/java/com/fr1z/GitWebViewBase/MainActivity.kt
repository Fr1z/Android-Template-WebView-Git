package com.fr1z.GitWebViewBase

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import java.io.File

class MainActivity : AppCompatActivity() {

    // Repository remote and branch
    private val repoURL = "https://github.com/yourusername/yourrepo.git"
    private val repoBranch = "main" // Edit with your  branch
    private val localWebHostName = "localstack.com"
    private val entryPoint = "index.html" //replace with your index.html relative path


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val localRepoDir = "${applicationContext.filesDir}/localRepo"

        // WebView setup
        val webView: WebView = findViewById(R.id.webView)
        webView.webViewClient = CustomWebClient(this, webView, localWebHostName)

        //Log WebViewVersion
        val webViewPackageInfo =  WebView.getCurrentWebViewPackage()?.versionName ?: "Not available"
        Log.d("WebView", "WebView version: $webViewPackageInfo")

        // Initialize local repo if not exists, or just pull updates
        initializeGitRepo(localRepoDir)

        // Load local HTML files (Entrypoint)
        webView.loadUrl("https://$localWebHostName/$entryPoint")
    }

    private fun initializeGitRepo(repoPath: String) {
        val repoDir = File(repoPath)

        try {
            if (repoDir.exists() && File(repoDir, ".git").exists()) {
                // If repo exists, do a pull
                Log.d("Git", "Local repository founded. pulling...")
                val git = Git.open(repoDir)
                git.pull()
                    .setRemote("origin")
                    .setCredentialsProvider(getCredentials())
                    .call()
            } else {
                // If repo do not exists, clone it from origin
                Log.d("Git", "Local repository not found. cloning new one...")
                Git.cloneRepository()
                    .setURI(repoURL)
                    .setDirectory(repoDir)
                    .setBranch(repoBranch)
                    //.setCredentialsProvider(getCredentials()) // Un-Comment if repo need user authentication
                    .call()
            }
        } catch (e: GitAPIException) {
            Log.e("Git", "Error accessing to repository Git: ${e.message}")
        } catch (e: Exception) {
            Log.e("Git", "Error: ${e.message}")
        }
    }

    private fun getCredentials(): UsernamePasswordCredentialsProvider? {
        // Replace credential if auth needed
        val username = "your_username" // Edit with your git username
        val password = "your_password" // Edit with your password/token
        return UsernamePasswordCredentialsProvider(username, password)
    }
}
