package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.maps.android.ktx.addMarker
import com.google.maps.android.ktx.awaitMap
import com.google.maps.android.ktx.awaitMapLoad
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.isLocationPermissionsGranted
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import timber.log.Timber

class SelectLocationFragment : BaseFragment() {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private var cancellationSource: CancellationTokenSource? = null
    private lateinit var googleMap: GoogleMap

    private var selectedPoi: PointOfInterest? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

//        TODO: add the map setup implementation
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            // Get map
            googleMap = mapFragment.awaitMap()

            // add style to the map
            setMapStyle()

            // put a marker to location that the user selected
            setMapClick()

            // Wait for map to finish loading
            googleMap.awaitMapLoad()

            enableMyLocation()

        }

        binding.confirmButton.setOnClickListener {
            // call this function after the user confirms on the selected location
            if (selectedPoi != null) {
                onLocationSelected()
            } else {
                _viewModel.showSnackBarInt.value = R.string.err_select_location
            }
        }

        return binding.root
    }

    private fun onLocationSelected() {
        // When the user confirms on the selected location,
        // send back the selected location details to the view model
        // and navigate back to the previous fragment to save the reminder and add the geofence
        _viewModel.selectedPOI.value = selectedPoi!!
        _viewModel.navigationCommand.value = NavigationCommand.Back
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // Change the map type based on the user's selection.
        R.id.normal_map -> {
            googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            googleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    @SuppressLint("MissingPermission")
    val locationPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            if (results.all { it.value }) {
                // Permission is granted. Continue the action or workflow in your app.
                enableMyLocation()
            } else {
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
                _viewModel.showErrorMessage.value =
                    "Location Access Denied & can't access current location"
            }
        }


    // first ask the user's location access permission to show his/her current location on the map.
    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (requireContext().isLocationPermissionsGranted()) {
            googleMap.isMyLocationEnabled = true
            cancellationSource = CancellationTokenSource()
            // navigate to current location only when no selection.
            // Get the last known location
            val fusedLocationClient: FusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(requireContext())
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY, cancellationSource!!.token
            ).addOnSuccessListener { location: Location? ->
                // Got current location. In some rare situations this can be null.
                // zoom to the user location after taking his permission
                location?.apply {
                    googleMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), 15f)
                    )
                }
            }

        } else {
            locationPermissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    override fun onStop() {
        super.onStop()
        cancellationSource?.cancel()
    }

    // Allows map styling and theming to be customized.
    private fun setMapStyle() {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style)
            )

            if (!success) {
                Timber.d("Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Timber.e(e, "Can't find style.")
        }
    }

    private fun setMapClick() {
        googleMap.setOnPoiClickListener { poi ->
            selectedPoi = poi
            showMarkerOfSelected()
        }

        googleMap.setOnMapClickListener { latLng ->
            selectedPoi = PointOfInterest(latLng, "", getString(R.string.dropped_pin))
            showMarkerOfSelected()
        }
    }

    private var marker: Marker? = null
    private fun showMarkerOfSelected() {
        selectedPoi?.run {
            val snippet = getString(R.string.lat_long_snippet, latLng.latitude, latLng.longitude)
            // A Snippet is Additional text that's displayed below the title.
            marker?.remove()
            marker = googleMap.addMarker {
                position(latLng)
                title(name)
                snippet(snippet)
            }
            marker!!.showInfoWindow()
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        }
    }
}
