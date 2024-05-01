package com.example.nikecore.ui


import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.view.View
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.nikecore.R
import com.example.nikecore.databinding.ActivityMainBinding
import com.example.nikecore.others.Constants.ACTION_SHOW_PAUSE_FRAGMENT
import com.example.nikecore.others.Constants.ACTION_SHOW_RUN_FRAGMENT
import com.example.nikecore.services.TrackingServices
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_NikeCore)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        navigateToTrackingFragmentIfNeeded(intent)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_run, R.id.navigation_useractivity, R.id.navigation_payment
            )
        )
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_run, R.id.navigation_useractivity, R.id.navigation_payment ->
                    navView.visibility = View.VISIBLE
                else -> navView.visibility = View.GONE
            }
        }

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_run, R.id.navigation_useractivity, R.id.navigation_payment ->
                    supportActionBar?.show()
                else -> supportActionBar?.hide()
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTrackingFragmentIfNeeded(intent)

    }

    private fun navigateToTrackingFragmentIfNeeded(intent: Intent?) {
        Timber.d("action ${intent?.action}")
        if (intent?.action == ACTION_SHOW_RUN_FRAGMENT) {
            Timber.d("action run main")
            nav_host_fragment_activity_main.findNavController()
                .navigate(R.id.action_global_runStartedFragment)
        } else if (intent?.action == ACTION_SHOW_PAUSE_FRAGMENT) {
            Timber.d("action pause main")
            nav_host_fragment_activity_main.findNavController()
                .navigate(R.id.action_global_runPausedFragment)
        }
    }

    fun sendCommandToService(action: String) =
        Intent(this, TrackingServices::class.java).also {
            it.action = action
            this.startService(it)
        }

    fun getBitmapDescriptorFromVector(
        context: Context,
        @DrawableRes vectorDrawableResourceId: Int
    ): BitmapDescriptor? {

        val vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId)
        val bitmap = Bitmap.createBitmap(
            vectorDrawable!!.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        vectorDrawable.draw(canvas)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }


}





