package com.example.drawingapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private var drawingView: DrawingView?=null
    private var mImageButtonCurrentPaint: ImageButton?= null

    var customProgressDialog : Dialog? =null

    val openGalleryLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
            if(result.resultCode == RESULT_OK && result.data!=null){
                val imageBackGround: ImageView =findViewById(R.id.iv_background)

                imageBackGround.setImageURI(result.data?.data)
            }
        }




    val requestPermission: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
            permissions ->
            permissions.entries.forEach{
                val permissionName =it.key
                val isGranted =it.value

                if(isGranted){
                    Toast.makeText(
                        this@MainActivity,
                        "Permission granted",
                        Toast.LENGTH_LONG
                    ).show()

                    val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    openGalleryLauncher.launch(pickIntent)


                }else{
                    if(permissionName== Manifest.permission.READ_EXTERNAL_STORAGE){
                        Toast.makeText(
                            this@MainActivity,
                        "Permission Denied ",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        drawingView=findViewById(R.id.drawing_view)
        drawingView?.setSizeForBrush(10.toFloat())



        mImageButtonCurrentPaint = findViewById(R.id.ib_color)
        mImageButtonCurrentPaint!!.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.black)
        )


        val ib_brush : ImageButton = findViewById(R.id.ib_brush)
        ib_brush.setOnClickListener{
            showBrushSuzeChooserDialog()
        }
        val ibUndo: ImageButton = findViewById(R.id.ib_undo)
        ibUndo.setOnClickListener{
            drawingView?.onCLickUndo()

        }
        val ibRedo: ImageButton = findViewById(R.id.ib_redo)
        ibRedo.setOnClickListener{
            drawingView?.onCLickRedo()

        }
        val ibGallery : ImageButton = findViewById(R.id.ib_gallery)
        ibGallery.setOnClickListener {
            requestStoragePermission()
        }

        val ibSave: ImageButton = findViewById(R.id.ib_save)
        ibSave.setOnClickListener{
            if(isReadStorageAllowed()){
                showProgressDialog()
                lifecycleScope.launch{
                    val flDrawingView: FrameLayout = findViewById(R.id.fl_drawing_view_container)
                    saveBitmapFile(getBitmapFromView((flDrawingView)))
                }

            }


        }


    }
    private fun showBrushSuzeChooserDialog(){
        val brushDialog= Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)
        brushDialog.setTitle("Brush size: ")
        val smallBtn = brushDialog.findViewById<ImageButton>(R.id.ib_small_brush)
        smallBtn.setOnClickListener{
            drawingView?.setSizeForBrush(5.toFloat())
            brushDialog.dismiss()
        }
        val mediumBtn = brushDialog.findViewById<ImageButton>(R.id.ib_medium_brush)
        mediumBtn.setOnClickListener{
            drawingView?.setSizeForBrush(15.toFloat())
            brushDialog.dismiss()
        }
        val largeBtn = brushDialog.findViewById<ImageButton>(R.id.ib_large_brush)
        largeBtn.setOnClickListener{
            drawingView?.setSizeForBrush(25.toFloat())
            brushDialog.dismiss()
        }
        brushDialog.show()
    }









    fun paintClicker(view:View){
        val colorDialog= Dialog(this)
        colorDialog.setContentView(R.layout.color_change)
        colorDialog.setTitle("Pick Color: ")
        val yellowClr = colorDialog.findViewById<ImageButton>(R.id.ib_yellow_brush)
        yellowClr.setOnClickListener{
            drawingView?.setColor("#FFFF00")
            mImageButtonCurrentPaint!!.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.yellow)
            )
            colorDialog.dismiss()
        }
        val fuchsiaClr = colorDialog.findViewById<ImageButton>(R.id.ib_fuchsia_brush)
        fuchsiaClr.setOnClickListener{
            drawingView?.setColor("#FF00FF")
            mImageButtonCurrentPaint!!.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.fuchsia)
            )
            colorDialog.dismiss()

        }
        val redClr = colorDialog.findViewById<ImageButton>(R.id.ib_red_brush)
        redClr.setOnClickListener{
            drawingView?.setColor("#FF0000")
            mImageButtonCurrentPaint!!.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.red)
            )
            colorDialog.dismiss()
        }
        val whiteClr = colorDialog.findViewById<ImageButton>(R.id.ib_white_brush)
        whiteClr.setOnClickListener{
            drawingView?.setColor("#FFFFFF")
            mImageButtonCurrentPaint!!.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.white)
            )
            colorDialog.dismiss()
        }
        val blueClr = colorDialog.findViewById<ImageButton>(R.id.ib_blue_brush)
        blueClr.setOnClickListener{
            mImageButtonCurrentPaint!!.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.blue)
            )
            drawingView?.setColor("#0000FF")
            colorDialog.dismiss()
        }
        val greenClr = colorDialog.findViewById<ImageButton>(R.id.ib_green_brush)
        greenClr.setOnClickListener{
            drawingView?.setColor("#00FF00")
            mImageButtonCurrentPaint!!.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.green)
            )
            colorDialog.dismiss()
        }
        val brownClr = colorDialog.findViewById<ImageButton>(R.id.ib_brown_brush)
        brownClr.setOnClickListener{
            drawingView?.setColor("#3d251e")
            mImageButtonCurrentPaint!!.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.brown)
            )
            colorDialog.dismiss()
        }
        val blackClr = colorDialog.findViewById<ImageButton>(R.id.ib_black_brush)
        blackClr.setOnClickListener{
            drawingView?.setColor("#000000")
            mImageButtonCurrentPaint!!.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.black)
            )
            colorDialog.dismiss()
        }
            colorDialog.show()
        }


    private fun isReadStorageAllowed(): Boolean{
        val result=ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        return  result == PackageManager.PERMISSION_GRANTED
    }
    private fun requestStoragePermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
        )){
            showRationaleDialog("Drawing App", "Drawing app requires access to your external Storage")
        }else{
            requestPermission.launch((arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )))
        }
    }

    private fun getBitmapFromView(view:View): Bitmap{
        val returnedBitmap = Bitmap.createBitmap(view.width,
        view.height,Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        val bgDrawable= view.background
        if(bgDrawable!=null){
            bgDrawable.draw(canvas)
        }else{
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)

        return returnedBitmap
    }

    private fun showProgressDialog(){
        customProgressDialog = Dialog(this@MainActivity)
        customProgressDialog?.setContentView(R.layout.dialog_custom_progress)
        customProgressDialog?.show()
    }

    private fun cancelProgressDialog(){
        if(customProgressDialog!=null){
            customProgressDialog?.dismiss()
            customProgressDialog=null
        }
    }


    private  suspend fun saveBitmapFile(mBitmap: Bitmap?):String{
        var result = ""
        withContext(Dispatchers.IO){
            if(mBitmap!=null){
                try{
                    val bytes =ByteArrayOutputStream()
                    mBitmap.compress(Bitmap.CompressFormat.PNG, 90,bytes)
                    val f=File(externalCacheDir?.absoluteFile.toString()+
                    File.separator+"DrawingApp" + System.currentTimeMillis())

                    val fo = FileOutputStream(f)
                    fo.write(bytes.toByteArray())
                    fo.close()
                    result = f.absolutePath
                    runOnUiThread{
                        cancelProgressDialog()
                        if(result.isNotEmpty()){
                            shareImage(result)
                        }
                    }
                }
                catch (e:Exception){
                    result=""
                    e.printStackTrace()

                }
            }
        }
        return result
    }

    private fun shareImage(result: String){
        MediaScannerConnection.scanFile(this, arrayOf(result), null){
            path, uri ->
            val shareIntent = Intent()
            shareIntent.action= Intent.ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
            shareIntent.type="image/png"
            startActivity(Intent.createChooser(shareIntent,"Share"))
        }
    }


    private fun showRationaleDialog(
        title: String,
        message: String
    ){

        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("Cancel"){ dialog, _ ->
                dialog.dismiss()
            }
        builder.create().show()
    }
}