package com.example.nikecore.ui.run

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.nikecore.R
import com.example.nikecore.databinding.FragmentRunBinding
import com.example.nikecore.others.Constants
import com.example.nikecore.others.Constants.REQUEST_CODE_LOCATION_PERMISSION
import com.example.nikecore.others.TrackingUtilities
import com.example.nikecore.ui.MainActivity
import com.example.nikecore.ui.ar.ARViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_run.*
import kotlinx.android.synthetic.main.run_paused_fragment.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber
import www.sanju.motiontoast.MotionToast
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@AndroidEntryPoint
class RunFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    private var gpsStatus: Boolean = false
    private val runViewModel: RunViewModel by activityViewModels()
    private val arViewModel: ARViewModel by activityViewModels()
    private var isBtnClickable = false


    private lateinit var currentLocation: Location
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val permissionCode = 101


    private var _binding: FragmentRunBinding? = null
    private var map: GoogleMap? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentRunBinding.inflate(inflater, container, false)
        val root: View = binding.root
        requestPermissions()
        requireActivity().actionBar?.show()
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
        return root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("onViewCreated")
        map?.clear()
        Timber.d("RunFrag", "We exist!!!")
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync {
            map = it
            fetchLocation(it)

        }
        locationEnabled()
        if (!gpsStatus) {
            MotionToast.darkToast(
                requireActivity(),
                getString(R.string.info),
                getString(R.string.location_run_frag),
                MotionToast.TOAST_ERROR,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION,
                ResourcesCompat.getFont(requireContext(), R.font.helvetica_regular)
            )
        }
        runViewModel.selectedCoordinates.observe(viewLifecycleOwner, {
            Timber.d("selectedCoordx:${it}")
            isBtnClickable = it != null
            when (isBtnClickable) {
                true -> startRunBtn.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.yellow
                    )
                )
                false -> startRunBtn.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.light_grey
                    )
                )

            }
        })

        startRunBtn.setOnClickListener {
            if (isBtnClickable) {
                locationEnabled()
                if (gpsStatus) {
                    findNavController().navigate(R.id.action_navigation_run_to_countingFragment)
                    (activity as MainActivity).sendCommandToService(Constants.ACTION_START_OR_RESUME_SERVICE)
                } else {
                    MotionToast.darkToast(
                        requireActivity(),
                        getString(R.string.info),
                        getString(R.string.location_services),
                        MotionToast.TOAST_ERROR,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.SHORT_DURATION,
                        ResourcesCompat.getFont(requireContext(), R.font.helvetica_regular)
                    )

                }
            } else {
                MotionToast.darkToast(
                    requireActivity(),
                    getString(R.string.info),
                    "Please select a ticket first",
                    MotionToast.TOAST_ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.SHORT_DURATION,
                    ResourcesCompat.getFont(requireContext(), R.font.helvetica_regular)
                )
            }


        }
        settingsBtn.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_run_to_settingsFragment)
        }
        statsBtn.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_run_to_statisticsFragment)
        }
        reloadLocationBtn.setOnClickListener {
            val sharedPref = requireContext().getSharedPreferences("locationdata", 0)
            sharedPref.edit().remove("location").apply()
            map?.let { it1 -> saveLocationIntoData(it1) }
        }

    }

    private fun requestPermissions() {
        if (TrackingUtilities.hasLocationPermissions(requireContext())) {
            return
        }
        EasyPermissions.requestPermissions(
            this,
            "You need to accept location permissions to use this app.",
            REQUEST_CODE_LOCATION_PERMISSION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            requestPermissions()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        fetchLocation(map!!)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }


    override fun onResume() {
        super.onResume()
        mapView?.onResume()
        Timber.d("onResume")
        if (map != null) { //prevent crashing if the map doesn't exist yet (eg. on starting activity)
            map?.clear()
            fetchLocation(map!!)
        }
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
        Timber.d("onStart")


    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
        Timber.d("onStop")

    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
        Timber.d("onPause")

    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
        Timber.d("onLowMemory")

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
        Timber.d("onSaveInstanceState")

    }

    private fun locationEnabled() {
        val locationManager =
            activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        gpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun fetchLocation(map: GoogleMap) {
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) !=
            PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), permissionCode
            )
            return
        }
        val task = fusedLocationProviderClient.lastLocation
        task.addOnSuccessListener { location ->
            if (location != null) {
                currentLocation = location
                map.addMarker(
                    MarkerOptions()
                        .position(LatLng(currentLocation.latitude, currentLocation.longitude))
                        .title("Your Location")

                )

                map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(currentLocation.latitude, currentLocation.longitude),
                        Constants.MAP_ZOOM_MAIN

                    )
                )

                map.addCircle(
                    CircleOptions()
                        .center(LatLng(currentLocation.latitude, currentLocation.longitude))
                        .radius(90.0)
                )
                map.setOnMarkerClickListener {

                    Timber.d("markerClicked ${it.position}")
                    if (LatLng(
                            currentLocation.latitude,
                            currentLocation.longitude
                        ) != it.position
                    ) {
                        runViewModel.postSelectedCoordinates(it.position)
                    } else {
                        MotionToast.darkToast(
                            requireActivity(),
                            getString(R.string.info),
                            "Please select a ticket",
                            MotionToast.TOAST_ERROR,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.SHORT_DURATION,
                            ResourcesCompat.getFont(requireContext(), R.font.helvetica_regular)
                        )
                    }
                    true
                }
                saveLocationIntoData(map)
            }
        }

    }


    private fun getRandomLocation(x0: Double, y0: Double, radius: Int): LatLng {
        val random = Random()

        // Convert radius from meters to degrees
        val radiusInDegrees = (radius / 111000f).toDouble()
        val u = random.nextDouble()
        val v = random.nextDouble()
        val w = radiusInDegrees * sqrt(u)
        val t = 2 * Math.PI * v
        val x = w * cos(t)
        val y = w * sin(t)

        // Adjust the x-coordinate for the shrinking of the east-west distances
        val new_x = x / cos(Math.toRadians(y0))
        val foundLongitude = new_x + x0
        val foundLatitude = y + y0
        Timber.d("Longitude: $foundLongitude  Latitude: $foundLatitude")

        return LatLng(foundLongitude, foundLatitude)
    }

    private fun setMarkerOnRandomLocations(map: GoogleMap) {
        val locationList = ArrayList<String>()
        for (i in 0..5) {

            val randomLocation = getRandomLocation(
                currentLocation.latitude,
                currentLocation.longitude,
                50
            ).latitude.toString() + "," + getRandomLocation(
                currentLocation.latitude,
                currentLocation.longitude,
                50
            ).longitude.toString()//store this to sharedpreference

            locationList.add(randomLocation)


            //Set the values


            map.addMarker(
                MarkerOptions().position(
                    getRandomLocation(
                        currentLocation.latitude,
                        currentLocation.longitude,
                        50
                    )
                )
                    .title("run")
            )?.setIcon(
                (activity as MainActivity).getBitmapDescriptorFromVector(
                    requireContext(),
                    R.drawable.ic_ticket_location_icon
                )

            )
        }
        val sharedPref = requireContext().getSharedPreferences("locationdata", 0)
        val gson = Gson()
        val jsonText = gson.toJson(locationList)
        Timber.d("jsonTim: $jsonText")

        sharedPref.edit().putString("location", jsonText).apply()
    }

    private fun saveLocationIntoData(map: GoogleMap) {
        val sharedPref = requireContext().getSharedPreferences("locationdata", 0)
        if (sharedPref.contains("location")) {


            val gson = Gson()
            val jsonText: String? = sharedPref.getString("location", null)
            val text = gson.fromJson(
                jsonText,
                Array<String>::class.java
            )

            arViewModel.collectedCoordinates.observe(viewLifecycleOwner, {
                if (text.isNotEmpty()) {
                    //val s = text.indexOf()
                    Timber.d("s value1: ${text[0]}")
                    Timber.d("s value2: ${text.size}")

                    if (it != null) {
                        val setList = text.toMutableList()
                        if (setList.isNotEmpty()) {
                            val x = it.toString().replace("lat/lng: (", "")
                            val y = x.replace(")", "")
                            val z = y.replace(" ", "")
                            Timber.d("replace: $z")

                            Timber.d("indexOf: ${setList.indexOf(z)}")

                            setList.removeAt(setList.indexOf(z))


                            Timber.d("s value3: ${setList.size}")

                            val jsonSetText = gson.toJson(setList)
                            sharedPref.edit().putString("location", jsonSetText).apply()
                            arViewModel.collectedCoordinates.postValue(null)
                        }

                    }
                }
            })
            val newText = gson.fromJson(
                jsonText,
                Array<String>::class.java
            )
            Timber.d("newText: ${newText.size}")


            //EDIT: gso to gson
            for (item in newText) {
                val latlong = item.split(",").toTypedArray()
                val latitude = latlong[0].toDouble()
                val longitude = latlong[1].toDouble()

                map.addMarker(
                    MarkerOptions().position(LatLng(latitude, longitude))
                        .title("run")
                )?.setIcon(
                    (activity as MainActivity).getBitmapDescriptorFromVector(
                        requireContext(),
                        R.drawable.ic_ticket_location_icon
                    )

                )

            }
        } else setMarkerOnRandomLocations(map)
    }

}