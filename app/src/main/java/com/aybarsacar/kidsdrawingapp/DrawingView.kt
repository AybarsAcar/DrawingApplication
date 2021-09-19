package com.aybarsacar.kidsdrawingapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import java.util.*
import kotlin.collections.ArrayList

/**
 * custom View class that allows the user draw on the it
 */
class DrawingView(context: Context, attributes: AttributeSet) : View(context, attributes) {

  internal inner class CustomPath(var color: Int, var brushThickness: Float) : Path() {

  }

  // stack that is used to implement an undo functionality
  private val _undoPaths = ArrayList<CustomPath>()

  private var _drawPath: CustomPath? = null
  private var _canvasBitmap: Bitmap? = null
  private var _drawPaint: Paint? = null
  private var _canvasPaint: Paint? = null
  private var _brushSize: Float = 0f
  private var _color = Color.BLACK
  private var _canvas: Canvas? = null

  // to store the lines we draw paths
  private val _paths = ArrayList<CustomPath>()

  init {
    setupDrawing()
  }

  /**
   * sets the colour of the brush
   */
  public fun setBrushColor(color: String) {
    _color = Color.parseColor(color)

    _drawPaint!!.color = _color
  }

  public fun setBrushSize(size: Float) {
    // adjusts the size according to the displayMetrics of the device
    // which is the pixel density and screen size and ratio of the device
    _brushSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size, resources.displayMetrics)

    _drawPaint!!.strokeWidth = _brushSize
  }

  /**
   * initialises and sets up the class members
   */
  private fun setupDrawing() {
    _drawPaint = Paint();
    _drawPath = CustomPath(_color, _brushSize)

    _drawPaint!!.color = _color
    _drawPaint!!.style = Paint.Style.STROKE
    _drawPaint!!.strokeJoin = Paint.Join.ROUND
    _drawPaint!!.strokeCap = Paint.Cap.ROUND

    _canvasPaint = Paint(Paint.DITHER_FLAG)
  }

  override fun onDraw(canvas: Canvas?) {
    super.onDraw(canvas)

    canvas?.drawBitmap(_canvasBitmap!!, 0f, 0f, _canvasPaint);

    // draw all the caches paths so far - paths we have drew so far
    for (path in _paths) {
      _drawPaint!!.strokeWidth = path.brushThickness
      _drawPaint!!.color = path.color
      canvas?.drawPath(path, _drawPaint!!)
    }

    if (!_drawPath!!.isEmpty) {

      _drawPaint!!.strokeWidth = _drawPath!!.brushThickness

      _drawPaint!!.color = _drawPath!!.color

      canvas?.drawPath(_drawPath!!, _drawPaint!!)
    }
  }

  /**
   *
   */
  override fun onTouchEvent(event: MotionEvent?): Boolean {

    val touchX = event?.x
    val touchY = event?.y

    when (event?.action) {
      // put the finger down on the screen
      MotionEvent.ACTION_DOWN -> {
        _drawPath!!.color = _color
        _drawPath!!.brushThickness = _brushSize

        _drawPath!!.reset()
        _drawPath!!.moveTo(touchX!!, touchY!!)
      }

      // drag the finger across the screen
      MotionEvent.ACTION_MOVE -> {
        _drawPath!!.lineTo(touchX!!, touchY!!)
      }

      // move the finger away from the screen
      MotionEvent.ACTION_UP -> {
        // persist the draw path
        _paths.add(_drawPath!!)

        _drawPath = CustomPath(_color, _brushSize)
      }

      // default case
      else -> return false
    }

    // invalidate the whole view if visible
    invalidate()

    return true
  }


  /**
   * it exists in the view class
   * called once the view is displayed / changed
   */
  override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    super.onSizeChanged(w, h, oldw, oldh)

    // set the canvas bitmap
    _canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)

    // use the bitmap as the canvas
    _canvas = Canvas(_canvasBitmap!!)
  }

  public fun handleUndo() {
    if (_paths.size > 0) {
      _undoPaths.add(_paths.removeLast())

      // redraw the path
      invalidate()
    }
  }

}