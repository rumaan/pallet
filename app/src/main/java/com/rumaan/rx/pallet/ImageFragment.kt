package com.rumaan.rx.pallet

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import kotlinx.android.synthetic.main.fragment_image.*


class ImageFragment : Fragment() {

    private var mListener: OnImageLoadedListener? = null

    interface OnImageLoadedListener {
        fun onImageLoaded()
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnImageLoadedListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    var filePathUri: String? = null

    val TAG = "ImageFragment"

    companion object {
        val KEY_BUNDLE_URI = "uri"

        fun newInstance(uri: Uri): Fragment {
            val args = Bundle()
            val fragment = ImageFragment()
            args.putString(KEY_BUNDLE_URI, uri.toString())
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            filePathUri = arguments.getString(KEY_BUNDLE_URI)
            Log.d(TAG, filePathUri.toString())
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.fragment_image, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (filePathUri != null) {
            // FIXME: OOM Exception on Large Image Sizes
            // image.setImageURI(Uri.parse(filePathUri))
            Glide.with(this)
                    .load(filePathUri)
                    .into(image)
            mListener?.onImageLoaded()
        }
    }
}
