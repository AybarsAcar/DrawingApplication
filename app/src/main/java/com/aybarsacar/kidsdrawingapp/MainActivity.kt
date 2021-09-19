package com.aybarsacar.kidsdrawingapp

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
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
import java.io.File
import java.io.OutputStream
import java.lang.Exception
import java.util.*


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

      if (ContextCompat.checkSelfPermission(
          this,
          Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
      ) {
        getImage.launch(MediaStore.Images.Media.CONTENT_TYPE)
      } else {
        requestPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
      }

    }


    ib_undo.setOnClickListener {
      drawing_view.handleUndo()
    }

    ib_save.setOnClickListener {
      if (ContextCompat.checkSelfPermission(
          this,
          Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
      ) {
        saveImageToGallery(getBitmapFromView(fl_drawing_view_container))
      } else {
        requestPermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
      }
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
   * saves the image to the user gallery
   */
  private fun saveImageToGallery(bitmap: Bitmap) {
    val fos: OutputStream

    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val resolver = contentResolver
        val contentValues = ContentValues()
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "Image_" + ".jpg")
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
        contentValues.put(
          MediaStore.MediaColumns.RELATIVE_PATH,
          Environment.DIRECTORY_PICTURES + File.separator + "TestFolder"
        )

        val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        fos = resolver.openOutputStream(Objects.requireNonNull(imageUri)!!)!!

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)

        Objects.requireNonNull<OutputStream>(fos)

        Toast.makeText(this, "Image Saved", Toast.LENGTH_SHORT).show()
      }
    } catch (e: Exception) {
      Toast.makeText(this, "Problam saving the image", Toast.LENGTH_SHORT).show()

    }
  }

  /**
   * converts the passed in View to a Bitmap
   * to allow the user to store the data as an image
   */
  private fun getBitmapFromView(view: View): Bitmap {
    val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)

    // bind the canvas on the view
    val canvas = Canvas(bitmap)

    // also get the background image if exists
    val bgDrawable = view.background

    if (bgDrawable !== null) {
      // draw background onto the canvas
      bgDrawable.draw(canvas)
    } else {
      // no bg image - draw white background
      canvas.drawColor(Color.WHITE)
    }

    // draw whats on the canvas onto the view (our drawn paths)
    view.draw(canvas)

    return bitmap
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