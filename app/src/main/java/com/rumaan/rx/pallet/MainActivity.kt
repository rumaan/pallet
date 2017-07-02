package com.rumaan.rx.pallet

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.view.animation.FastOutSlowInInterpolator
import android.support.v7.app.AppCompatActivity
import android.support.v7.graphics.Palette
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), ImageFragment.OnImageLoadedListener {
    override fun onImageLoaded() {
        // show close button and text after image loaded
        val anim = AnimationUtils.makeInAnimation(this, false)
        anim.interpolator = FastOutSlowInInterpolator()
        anim.duration = 500
        close_btn.animation = anim
        close_btn.visibility = View.VISIBLE
    }


    val REQUEST_IMG = 4
    val REQUEST_PERMISSION = 5
    var typeface: Typeface? = null

    val TAG = "MainActivity"

    var imageFragment: Fragment? = null

    var palletAsyncListener = Palette.PaletteAsyncListener({
        // change color scheme with pallet generated
        val vibrantSwatch = it.vibrantSwatch
        if (vibrantSwatch != null) {
            root_view.setBackgroundColor(vibrantSwatch.rgb)
            window.statusBarColor = vibrantSwatch.rgb
            hexcode_text.setTextColor(vibrantSwatch.bodyTextColor)
            hexcode_text.text = "$vibrantSwatch"

            animateText()
            return@PaletteAsyncListener
        }

        val darkSwatch = it.darkVibrantSwatch
        if (darkSwatch != null) {
            root_view.setBackgroundColor(darkSwatch.rgb)
            window.statusBarColor = darkSwatch.rgb
            hexcode_text.setTextColor(darkSwatch.bodyTextColor)
            hexcode_text.text = "$darkSwatch"
            animateText()
            return@PaletteAsyncListener
        }

        hexcode_text.text = "Could'nt get the hexcode please try again!"
        animateText()

    })

    private fun animateText() {
        val anim = AnimationUtils.makeInChildBottomAnimation(this)
        anim.interpolator = FastOutSlowInInterpolator()
        anim.duration = 500
        hexcode_text.animation = anim
        hexcode_text.visibility = View.VISIBLE
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        typeface = Typeface.createFromAsset(assets, "fonts/oswald_medium.ttf")

        choose_image.setOnClickListener {
            // If fragment is already loaded show this feedback
            if (imageFragment != null) {
                Snackbar
                        .make(root_view,
                                "Please close the current image to choose a new one.",
                                Snackbar.LENGTH_SHORT)
                        .show()

            } else {
                requestImage()
            }
        }

        choose_image_text.typeface = typeface
        hexcode_text.typeface = typeface

        close_btn.setOnClickListener {
            // remove the fragment
            if (imageFragment != null) {
                supportFragmentManager.beginTransaction()
                        .remove(imageFragment)
                        .commit()
                imageFragment = null

                val anim = AnimationUtils.makeInAnimation(this, false)
                anim.interpolator = FastOutSlowInInterpolator()
                anim.duration = 500
                close_btn.visibility = View.GONE

                root_view.setBackgroundColor(ContextCompat.getColor(this, android.R.color.background_light))
                window.statusBarColor = ContextCompat.getColor(this, R.color.colorAccent)

                hexcode_text.visibility = View.GONE
            }

        }
    }

    private fun requestImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA),
                        REQUEST_PERMISSION)
            } else {
                getImage()
            }
        } else {
            getImage()
        }
    }

    private fun getImage() {
        /*val takePicIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePicIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePicIntent, REQUEST_IMG)
        }*/
        val pickImageIntent = Intent()
        pickImageIntent.type = "image/*"
        pickImageIntent.action = Intent.ACTION_GET_CONTENT

        startActivityForResult(Intent.createChooser(pickImageIntent, "Select Picture"), REQUEST_IMG)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMG) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    val uri: Uri = data.data
                    changeFragment(uri)
                }
            } else {
                Toast.makeText(this, "Action Canceled by user.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun changeFragment(uri: Uri) {

        // send the image for generate palette async
        createNewPalette(uri)

        imageFragment = ImageFragment.newInstance(uri)
        supportFragmentManager
                .beginTransaction()
                .add(R.id.choose_image_container, imageFragment)
                .setCustomAnimations(R.anim.abc_fade_in, R.anim.abc_fade_out, R.anim.abc_popup_enter, R.anim.abc_popup_exit)
                .addToBackStack(null)
                .commit()
    }

    private fun createNewPalette(uri: Uri) {
        // create a new pallet with the image
        val inputStream = contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)

        // create palette asynchronously
        Palette.from(bitmap)
                .generate(palletAsyncListener)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getImage()
            } else {
                Toast.makeText(this, "Permissions Not Granted", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
