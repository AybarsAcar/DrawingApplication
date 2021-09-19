package com.aybarsacar.kidsdrawingapp

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import com.aybarsacar.kidsdrawingapp.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_brush_size.*


class MainActivity : AppCompatActivity() {

  // request permission to Reading from storage
  private val requestPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->

    if (granted) {
      Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()

    } else {
      Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
    }
  }


  private var _imageButtonCurrentPaint: ImageButton? = null


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.activity_main)


    drawing_view.setBrushSize(10f)

    // set the initial colour
    _imageButtonCurrentPaint = color_selector[1] as ImageButton
    _imageButtonCurrentPaint!!.setImageDrawable(
      ContextCompat.getDrawable(this, R.drawable.palette_selected)
    )

    ib_brush.setOnClickListener {
      showBrushSizeSelectorDialog()
    }


    val getImage = registerForActivityResult(ActivityResultContracts.GetContent()) { data ->
      image_background.visibility = View.VISIBLE
      image_background.setImageURI(data)
    }


    ib_gallery.setOnClickListener {
      requestPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    ib_gallery.setOnClickListener {

      getImage.launch(MediaStore.Images.Media.CONTENT_TYPE)
    }

  }


  /**
   * shows the brush size selector dialogue
   * when clicked on the brush image button
   */
  private fun showBrushSizeSelectorDialog() {

    // import the dialogue class with this context
    val brushDialog = Dialog(this)

    // set the content of the dialogue with the layout we have created
    brushDialog.setContentView(R.layout.dialog_brush_size)

    brushDialog.setTitle("Brush size")

    // cache in the brush size buttons from our view
    var smallButton = brushDialog.ib_small_brush
    var mediumButton = brushDialog.ib_medium_brush
    var largeButton = brushDialog.ib_large_brush

    // set their onclicks
    smallButton.setOnClickListener {
      drawing_view.setBrushSize(5f)

      // close the dialog
      brushDialog.dismiss()
    }

    mediumButton.setOnClickListener {
      drawing_view.setBrushSize(10f)

      // close the dialog
      brushDialog.dismiss()
    }

    largeButton.setOnClickListener {
      drawing_view.setBrushSize(20f)

      // close the dialog
      brushDialog.dismiss()
    }


    brushDialog.show()

  }


  /**
   * we pass in the view clicked to get teh information out of it
   */
  fun handlePaintSelection(view: View) {

    if (view !== _imageButtonCurrentPaint) {

      val imageButton = view as ImageButton

      val colorTag = imageButton.tag.toString() // this is our colour in hexadecimal

      drawing_view.setBrushColor(colorTag)


      // handle the selection
      imageButton.setImageDrawable(
        ContextCompat.getDrawable(this, R.drawable.palette_selected)
      )


      // unselect the previously selected paint button
      _imageButtonCurrentPaint!!.setImageDrawable(
        ContextCompat.getDrawable(this, R.drawable.palette_normal)
      )

      _imageButtonCurrentPaint = view
    }
  }
}