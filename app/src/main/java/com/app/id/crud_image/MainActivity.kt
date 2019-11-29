package com.app.id.crud_image

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.app.Dialog
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.util.Calendar
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private val btCamera: Button? = null
    private val ivCamera: ImageView? = null
    private val TAG = MainActivity::class.java!!.getSimpleName()
    private val FINAL_CHOOSE_PHOTO = 2
    private val CAMERA_REQUEST_CODE = 123
    val path: String = ""


    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnUbah.setOnClickListener {
            showPictureDialog()
        }

    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            CAMERA_REQUEST_CODE ->
                if (resultCode == Activity.RESULT_OK) {
                    var bmp = data?.extras?.get("data") as? Bitmap
                    imgContoh.setImageBitmap(bmp)
                }
            FINAL_CHOOSE_PHOTO ->
                if (resultCode == Activity.RESULT_OK) {
                    if (Build.VERSION.SDK_INT >= 19) {
                        handleImageOnKitkat(data)
                    }
                }
        }
    }

    private fun camera(){
        var intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    private fun galery(){
        val checkSelfPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (checkSelfPermission != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }
        else{
            openAlbum()
        }
    }

    private fun showPictureDialog() {
        val pictureDialog = AlertDialog.Builder(this)
        val inflater = this@MainActivity.layoutInflater
        val view = inflater.inflate(R.layout.icon, null)
        pictureDialog.setTitle("Select Action")
//        val pictureDialogItems = arrayOf("Select image from gallery", "Capture photo from camera")
//        pictureDialog.setItems(pictureDialogItems
//        ) { dialog, which ->
//            when (which) {
//                0 -> galery()
//                1 -> camera()
//            }
//        }
//        pictureDialog.show()


        view.findViewById<ImageView>(R.id.imgCamera).setOnClickListener {
            camera()
        }

        view.findViewById<ImageView>(R.id.imgGalery).setOnClickListener {
            galery()
        }

        pictureDialog.setView(view)
        val al = pictureDialog.create()
        al.show()

    }

    private fun openAlbum(){
        val intent = Intent("android.intent.action.GET_CONTENT")
        intent.type = "image/*"
        startActivityForResult(intent, FINAL_CHOOSE_PHOTO)
    }

    @TargetApi(19)
    private fun handleImageOnKitkat(data: Intent?) {
        var imagePath: String? = null
        val uri = data!!.data
        if (DocumentsContract.isDocumentUri(this, uri)){
            val docId = DocumentsContract.getDocumentId(uri)
            if ("com.android.providers.media.documents" == uri.authority){
                val id = docId.split(":")[1]
                val selsetion = MediaStore.Images.Media._ID + "=" + id
                imagePath = imagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selsetion)
            }
            else if ("com.android.providers.downloads.documents" == uri.authority){
                val contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(docId))
                imagePath = imagePath(contentUri, null)
            }
        }
        else if ("content".equals(uri.scheme, ignoreCase = true)){
            imagePath = imagePath(uri, null)
        }
        else if ("file".equals(uri.scheme, ignoreCase = true)){
            imagePath = uri.path
        }
        displayImage(imagePath)
    }

    private fun displayImage(imagePath: String?){
        if (imagePath != null) {
            val bitmap = BitmapFactory.decodeFile(imagePath)
            imgContoh?.setImageBitmap(bitmap)
        }
        else {
            Toast.makeText(this, "Failed to get image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun imagePath(uri: Uri?, selection: String?): String {
        var path: String? = null
        val cursor = contentResolver.query(uri, null, selection, null, null )
        if (cursor != null){
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
            }
            cursor.close()
        }
        return path!!
    }
}
