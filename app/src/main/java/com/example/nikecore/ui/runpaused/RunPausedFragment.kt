package com.example.nikecore.ui.runpaused

import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.example.nikecore.R
import com.example.nikecore.database.Run
import com.example.nikecore.others.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.nikecore.others.Constants.ACTION_STOP_SERVICE
import com.example.nikecore.others.Constants.MAP_ZOOM
import com.example.nikecore.others.Constants.POLYLINE_COLOR
import com.example.nikecore.others.Constants.POLYLINE_WIDTH
import com.example.nikecore.others.TrackingUtilities
import com.example.nikecore.services.Polyline
import com.example.nikecore.services.TrackingServices
import com.example.nikecore.ui.MainActivity
import com.example.nikecore.ui.ar.ARViewModel
import com.example.nikecore.ui.run.RunViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.run_paused_fragment.*
import timber.log.Timber
import www.sanju.motiontoast.MotionToast
import java.util.*
import javax.inject.Inject
import kotlin.math.round


@AndroidEntryPoint
class RunPausedFragment : Fragment() {
    private var pathPoints = mutableListOf<Polyline>()
    private var curTimeInMillis = 0L


    private var map: GoogleMap? = null

    @set:Inject
    var weight = 80f


    private val viewModel: RunPausedViewModel by viewModels()
    private val runViewModel: RunViewModel by activityViewModels()
    private val arViewModel: ARViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.run_paused_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("isCollected: ${arViewModel.isCollected.value}")


        mapViewRunPaused.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            // With blank your fragment BackPressed will be disabled.
            showCancelTrackingDialog()
            runViewModel.selectedCoordinates.postValue(null)
            arViewModel.isCollected.postValue(false)

        }

        mapViewRunPaused.getMapAsync {
            Timber.d("mapView $it")
            map = it
            subscribeToObservers(it)
        }
        resumeRunBtn.setOnClickListener {
            (activity as MainActivity).sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
        stopRunBtn.setOnLongClickListener {
            showCancelTrackingDialog()
            runViewModel.selectedCoordinates.postValue(null)
            arViewModel.isCollected.postValue(false)
            true
        }
        stopRunBtn.setOnClickListener {

            MotionToast.setInfoColor(R.color.yellow)
            MotionToast.darkToast(
                requireActivity(),
                getString(R.string.info),
                getString(R.string.long_press_cancel),
                MotionToast.TOAST_INFO,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.SHORT_DURATION,
                ResourcesCompat.getFont(requireContext(), R.font.helvetica_regular)
            )
        }

        finishRunBtn.setOnLongClickListener {
            zoomToSeeWholeTrack()
            endRunAndSaveToDb()
            runViewModel.selectedCoordinates.postValue(null)
            arViewModel.isCollected.postValue(false)
            true
        }
        finishRunBtn.setOnClickListener {

            MotionToast.darkToast(
                requireActivity(),
                getString(R.string.info),
                getString(R.string.long_press),
                MotionToast.TOAST_INFO,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.SHORT_DURATION,
                ResourcesCompat.getFont(requireContext(), R.font.helvetica_regular)
            )
        }
        YoYo.with(Techniques.Bounce)
            .duration(950)
            .repeat(10)
            .playOn(arCameraBtn)
        arCameraBtn.setOnClickListener {
            findNavController().navigate(R.id.action_runPausedFragment_to_ARFragment)
        }

    }

    private fun subscribeToObservers(map: GoogleMap) {

        if (view != null) {
            TrackingServices.isTracking.observe(viewLifecycleOwner, {
                if (it) {
                    findNavController().navigate(R.id.action_runPausedFragment_to_runStartedFragment)
                }
            })



            TrackingServices.pathPoints.observe(viewLifecycleOwner, {
                pathPoints = it
                Timber.d("observe pathpoint $pathPoints")
                addAllPolylines(map)
                setCurrentLocationMarker(map)
                setStartingPositionMarker(map)
                moveCameraToUser(map)
                distanceValuePausedTxt.text = distanceCovered(pathPoints)
            })

            TrackingServices.timeRunInMillis.observe(viewLifecycleOwner, {
                curTimeInMillis = it
                val formattedTime =
                    TrackingUtilities.getFormattedStopWatchTime(curTimeInMillis, true)
                timeValuePausedTxt.text = formattedTime
                speedValuePausedTxt.text = avgSpeed()

            })

            runViewModel.selectedCoordinates.observe(viewLifecycleOwner, {
                if (it != null && arViewModel.isCollected.value == false) {
                    map.addMarker(
                        MarkerOptions().position(it)
                            .title("run")
                    )?.setIcon(
                        (activity as MainActivity).getBitmapDescriptorFromVector(
                            requireContext(),
                            R.drawable.ic_ticket_location_icon
                        )
                    )


                    val currentLocation = Location("currentLocation")
                    currentLocation.latitude = pathPoints.last().last().latitude
                    currentLocation.longitude = pathPoints.last().last().longitude
                    val ticketLocation = Location("ticketLocation")
                    ticketLocation.latitude = it.latitude
                    ticketLocation.longitude = it.longitude



                    arViewModel.isCollected.observe(viewLifecycleOwner, {
                        if (arViewModel.isCollected.value == false && currentLocation.distanceTo(
                                ticketLocation
                            ) < 30
                        ) {
                            arCameraBtn.visibility = View.VISIBLE
                        } else if (arViewModel.isCollected.value == true) {
                            arCameraBtn.visibility = View.GONE
                        }
                    })


                } else if (it != null && arViewModel.isCollected.value == true) {
                    map.addMarker(
                        MarkerOptions().position(it)
                            .title("run")
                    )?.setIcon(
                        (activity as MainActivity).getBitmapDescriptorFromVector(
                            requireContext(),
                            R.drawable.ic_ticket_collected_icon
                        )
                    )
                }

            })

        }

    }


    private fun setStartingPositionMarker(map: GoogleMap) {
        if (pathPoints.isNotEmpty() && pathPoints.first().isNotEmpty()) {
            map.addMarker(
                MarkerOptions()
                    .position(pathPoints.first().first())
            )
        }
    }

    private fun moveCameraToUser(map: GoogleMap) {
        Timber.d("move camera $pathPoints")
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            map.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    MAP_ZOOM

                )
            )
        }
    }

    private fun zoomToSeeWholeTrack() {
        val bounds = LatLngBounds.Builder()
        for (polyline in pathPoints) {
            for (pos in polyline) {
                bounds.include(pos)
            }
        }

        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                mapViewRunPaused.width,
                mapViewRunPaused.height,
                (mapViewRunPaused.height * 0.05f).toInt()
            )
        )
    }

    private fun endRunAndSaveToDb() {
        map?.snapshot { bmp ->
            var distanceInMeters = 0
            for (polyline in pathPoints) {
                distanceInMeters += TrackingUtilities.calculatePolylineLength(polyline).toInt()
            }
            val avgSpeed =
                round((distanceInMeters / 1000f) / (curTimeInMillis / 1000f / 60 / 60) * 10) / 10f
            val dateTimestamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceInMeters / 1000f) * weight).toInt()
            val run =
                Run(bmp, dateTimestamp, avgSpeed, distanceInMeters, curTimeInMillis, caloriesBurned)
            viewModel.insertRun(run)

            MotionToast.darkToast(
                requireActivity(),
                getString(R.string.info),
                getString(R.string.run_saved),
                MotionToast.TOAST_SUCCESS,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.SHORT_DURATION,
                ResourcesCompat.getFont(requireContext(), R.font.helvetica_regular)
            )

            stopRun()
        }
    }


    private fun distanceCovered(pathPoints: MutableList<Polyline>): String {
        var distanceInMeters = 0F
        for (polyline in pathPoints) {
            distanceInMeters += TrackingUtilities.calculatePolylineLength(polyline)
        }
        val distanceInKm = distanceInMeters / 1000

        Timber.d("distanceInKm: $distanceInKm")

        return if (distanceInKm > 0.01F) distanceInKm.toString()
            .substring(0, 5) else getString(R.string.zero_value)

    }

    private fun avgSpeed(): String {
        var distanceInMeters = 0F
        for (polyline in pathPoints) {
            distanceInMeters += TrackingUtilities.calculatePolylineLength(polyline)
        }
        val distanceInKm = distanceInMeters / 1000


        val avgSpeed =
            round((distanceInKm) / (curTimeInMillis / 1000f / 60 / 60) * 10) / 10f
        return avgSpeed.toString()
    }


    private fun setCurrentLocationMarker(map: GoogleMap) {
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            map.addMarker(
                MarkerOptions().position(pathPoints.last().last())
                    .title("run")
            )?.setIcon(
                (activity as MainActivity).getBitmapDescriptorFromVector(
                    requireContext(),
                    R.drawable.ic_current_loaction_marker_icon
                )

            )

        }
    }

    private fun addAllPolylines(map: GoogleMap) {
        Timber.d("add allpolyline $pathPoints $map")
        for (polyline in pathPoints) {
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            map.addPolyline(polylineOptions)
        }
    }

    private fun showCancelTrackingDialog() {
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Cancel the Run?")
            .setMessage("Are you sure to cancel the current run and delete all its data?")
            .setIcon(R.drawable.ic_menu_run)
            .setPositiveButton("Yes") { _, _ ->
                (activity as MainActivity).sendCommandToService(ACTION_STOP_SERVICE)
                stopRun()
            }
            .setNegativeButton("No") { dialogInterface, _ ->
                dialogInterface.cancel()
            }
            .create()
        dialog.show()
    }

    private fun stopRun() {
        (activity as MainActivity).sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().popBackStack(R.id.navigation_run, false)
    }


    override fun onResume() {
        super.onResume()
        mapViewRunPaused?.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapViewRunPaused?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapViewRunPaused?.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapViewRunPaused?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapViewRunPaused?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapViewRunPaused?.onSaveInstanceState(outState)
    }

}