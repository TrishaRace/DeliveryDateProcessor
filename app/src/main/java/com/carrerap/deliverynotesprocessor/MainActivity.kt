package com.carrerap.deliverynotesprocessor

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import java.io.IOException

class MainActivity : AppCompatActivity() {

    var deliveryNoteList : ArrayList<String> = ArrayList()

    val MY_PERMISSION_REQUEST_CAMERA = 100
    val MY_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 200
    val MY_PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 300
    val PHOTO_REQUEST_CODE = 5243
    val REQUEST_CODE = 99

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
        b_camera.setOnClickListener {
            RepsolCamera.start(this, PHOTO_REQUEST_CODE, 1)
        }
    }
    private fun init(){
        if(deliveryNoteList.isEmpty()){
            showCustomAlert(getString(R.string.anyDeliveryNoteCapturedAlert))
        }
        FirebaseApp.initializeApp(this)
        askPhotoPermissions()
    }

    fun askPhotoPermissions() {
        requestPermission(Manifest.permission.CAMERA, MY_PERMISSION_REQUEST_CAMERA)
        requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,MY_PERMISSION_REQUEST_READ_EXTERNAL_STORAGE)
        requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE,MY_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE)

    }

    fun requestPermission(permission: String,myPermissionConstant : Int) {

        if (ContextCompat.checkSelfPermission(this,
                permission)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE),myPermissionConstant)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            PHOTO_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        val returnValue = data.getStringArrayListExtra(RepsolCamera.IMAGE_RESULTS)
                        prepareImageToRecognize(returnValue.first())
                    }
                }
            }
            REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK){
                    val uri = data?.getExtras()!!.getParcelable<Uri>(ScanConstants.SCANNED_RESULT)
                    lateinit var bitmap: Bitmap
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                        //contentResolver.delete(uri, null, null)
                        textRecognizer(bitmap)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
    fun prepareImageToRecognize(path : String) {
        //Incluir aquí todos los procesos de estabilización de imagen

        val intent = Intent(this, ScanActivity::class.java)
        intent.putExtra("imagePath",path)
        startActivityForResult(intent, REQUEST_CODE)

    }
    fun textRecognizer(bitmap: Bitmap){
        val image = FirebaseVisionImage.fromBitmap(bitmap)
        val detector = FirebaseVision.getInstance().onDeviceTextRecognizer
        detector.processImage(image)
            .addOnSuccessListener { firebaseVisionText ->
                if (!firebaseVisionText.text.isEmpty()) {
                    // orderText(firebaseVisionText)
                    showResult(firebaseVisionText.text)
                }else{
                    showCustomAlert("No se ha podido reconocer el texto, por favor vuelva a intentarlo")
                }
            }
            .addOnFailureListener {
                showCustomAlert("No se ha podido reconocer el texto, por favor vuelva a intentarlo")
            }
    }

    fun showResult(result: String){
        deliveryNoteList.add(result)
        supportFragmentManager.beginTransaction()
            .replace(R.id.lyFragmentContainer, DeliveryNoteListFragment.newInstance(deliveryNoteList))
            .commit()
    }

    fun showCustomAlert(text : String){
        supportFragmentManager.beginTransaction()
            .replace(R.id.lyFragmentContainer, CustomAlertFragment.newInstance(text))
            .commit()
    }
    fun orderText(result: FirebaseVisionText){
        result.textBlocks.forEach{block ->
            if(block.lines[0].text.equals("Descripción")){
                block.lines.forEach { line -> showResult(line.text)}
            }
        }
    }



}
