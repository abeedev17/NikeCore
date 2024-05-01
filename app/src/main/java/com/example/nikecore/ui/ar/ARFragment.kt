package com.example.nikecore.ui.ar

import android.content.SharedPreferences
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.nikecore.R
import com.example.nikecore.ui.run.RunViewModel
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.a_r_fragment.*
import kotlinx.android.synthetic.main.a_r_fragment.view.*
import timber.log.Timber
import www.sanju.motiontoast.MotionToast


class ARFragment : Fragment() {

    private lateinit var arFrag: ArFragment
    private var viewRenderable: ViewRenderable? = null
    private var modelRenderable: ModelRenderable? = null
    private val arViewModel: ARViewModel by activityViewModels()
    private val runViewModel: RunViewModel by activityViewModels()

    private var userMoney = 0
    private var userTicket = 0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.a_r_fragment, container, false)


        val sharedPrefs: SharedPreferences =
            requireContext().getSharedPreferences("user_Balance", 0)
        userMoney = sharedPrefs.getInt("USER_MONEY", 0) //0 is the default value


        val ticketSharedPrefs: SharedPreferences =
            requireContext().getSharedPreferences("user_Ticket", 0)
        userTicket = ticketSharedPrefs.getInt("USER_TICKET", 0) //0 is the default value

        arFrag = childFragmentManager.findFragmentById(
            R.id.sceneform_fragment
        ) as ArFragment

        view.findViewById<Button>(R.id.showTicketBtn).setOnClickListener {


            add3dObject()
        }

        view.findViewById<Button>(R.id.goBackBtn).setOnClickListener {
            findNavController().navigate(R.id.action_ARFragment_to_runPausedFragment)
        }


        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .permitAll().build()
        )
        // (CC BY 4.0) Donated by Cesium for glTF testing.
        ModelRenderable.builder()
            .setSource(requireContext(), Uri.parse("file:///android_asset/ticket3.gltf"))
            .setIsFilamentGltf(true)
            .setAsyncLoadEnabled(true)
            .setRegistryId("ticket")
            .build()
            .thenAccept { modelRenderable = it }
            .exceptionally {
                Timber.e("something went wrong " + it.localizedMessage)
                null
            }

        arFrag.setOnTapArPlaneListener { hitResult: HitResult?, _, _ ->
            viewRenderable ?: return@setOnTapArPlaneListener
            //Creates a new anchor at the hit location
            val anchor = hitResult!!.createAnchor()
            //Creates a new anchorNode attaching it to anchor
            val anchorNode = AnchorNode(anchor)
            // Add anchorNode as root scene node's child
            anchorNode.setParent(arFrag.arSceneView.scene)
            // Can be selected, rotated...
            val viewNode = TransformableNode(arFrag.transformationSystem)
            viewNode.renderable = viewRenderable
            // Add viewNode as anchorNode's child
            viewNode.setParent(anchorNode)
            // Sets this as the selected node in the TransformationSystem
            viewNode.select()
        }
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        arViewModel.userBalance.observe(viewLifecycleOwner, {
            userMoneyTxt.text = it.toString()
        })
    }

    private fun getScreenCenter(): Point {
        // find the root view of the activity
        val vw = view
        // returns center of the screen as a Point object
        return Point(vw!!.width / 2, vw.height / 2)
    }

    private fun add3dObject() {
        val frame = arFrag.arSceneView.arFrame
        if (frame != null && modelRenderable != null) {
            //    view?.findViewById<Button>(R.id.showTicketBtn)?.visibility = View.GONE
            val pt = getScreenCenter()
            val hits = frame.hitTest(pt.x.toFloat(), pt.y.toFloat())
            for (hit in hits) {
                val trackable = hit.trackable
                if (trackable is Plane) {
                    val anchor = hit!!.createAnchor()
                    val anchorNode = AnchorNode(anchor)
                    anchorNode.setParent(arFrag.arSceneView.scene)
                    val mNode =
                        TransformableNode(arFrag.transformationSystem)
                    mNode.setOnTapListener { _, _ ->
                        view?.findViewById<TextView>(R.id.moneyAddedTxt)?.visibility = View.VISIBLE
                        view?.findViewById<Button>(R.id.showTicketBtn)?.visibility = View.GONE
                        view?.findViewById<Button>(R.id.goBackBtn)?.visibility = View.VISIBLE

                        userMoney += 10
                        val sharedPrefs: SharedPreferences =
                            requireContext().getSharedPreferences("user_Balance", 0)
                        val editor = sharedPrefs.edit()
                        editor.putInt("USER_MONEY", userMoney)
                        editor.apply()

                        userTicket += 1
                        val ticketSharedPrefs: SharedPreferences =
                            requireContext().getSharedPreferences("user_Ticket", 0)
                        val editorTicket = ticketSharedPrefs.edit()
                        editorTicket.putInt("USER_TICKET", userTicket)
                        editorTicket.apply()
                        runViewModel.selectedCoordinates.observe(viewLifecycleOwner, {
                            arViewModel.collectedCoordinates.postValue(it)
                            Timber.d("it: $it")
                        })
                        arViewModel.userTicket.postValue(userTicket)
                        arViewModel.userBalance.postValue(userMoney)
                        arViewModel.isCollected.postValue(true)

                        MotionToast.darkToast(
                            requireActivity(),
                            getString(R.string.Congrats),
                            getString(R.string.ticket_collected),
                            MotionToast.TOAST_SUCCESS,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.SHORT_DURATION,
                            ResourcesCompat.getFont(requireContext(), R.font.helvetica_regular)
                        )
                        mNode.setParent(null)
                        anchorNode.anchor?.detach()
                        Handler(Looper.getMainLooper()).postDelayed({
                            view?.findViewById<TextView>(R.id.moneyAddedTxt)?.visibility = View.GONE
                        }, 1200)

                    }
                    mNode.renderable = modelRenderable
                    mNode.setRenderable(this.modelRenderable).animate(true).start()
                    mNode.scaleController.minScale = 0.2f
                    mNode.scaleController.maxScale = 2.0f
                    mNode.localScale = Vector3(0.2f, 0.2f, 0.2f)
                    mNode.setParent(anchorNode)
                    mNode.select()
                    break
                }
            }
        }
    }
}