package com.iug.iug_firbasestorage

import android.app.DownloadManager
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.iug.iug_firbasestorage.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding:ActivityMainBinding
    lateinit var ref: StorageReference
    private lateinit var progressDialog: ProgressDialog
    private var pdfUri: Uri? = null
    val PICK_PDF_REQUEST = 100
    val FILE_NAME = "pdf_file"




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val storage = Firebase.storage
        ref = storage.reference
        addCallbacks()
    }


    private fun addCallbacks(){

        binding.imgChooseFile.setOnClickListener {
            selectPdfFile()
            binding.tvResult.text = "File selected"
        }

        binding.btnUpload.setOnClickListener {
            pdfUri?.let { pdfPath ->
                showProgress()
                ref.child("pdf/${FILE_NAME}.pdf")
                    .putFile(pdfPath)
                    .addOnSuccessListener {
                        showToast("Upload Success")
                        dismissProgress()
                    }
                    .addOnFailureListener {
                        showToast("Upload failed: ${it.message}")
                        dismissProgress()
                    }
            } ?: run {
                showToast("no file selected")
            }


        }

        binding.btnDownload.setOnClickListener {
            showProgress()
            ref.child("pdf/${FILE_NAME}.pdf")
                .downloadUrl
                .addOnSuccessListener {
                    downloadFile(it.toString(), "pdf file")
                    showToast("Download Success")
                    dismissProgress()
                }
                .addOnFailureListener {
                    showToast("Download failed: ${it.message}")
                    dismissProgress()
                }
        }

    }

    private fun selectPdfFile(){
        val intent = Intent()
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select PDF"), PICK_PDF_REQUEST)
    }

    private fun storePdfFile(){
        pdfUri?.let { uri ->
            val storageReference: StorageReference = FirebaseStorage.getInstance().reference
            storageReference.child("my_pdf_file.pdf").putFile(uri)
                .addOnSuccessListener {
                    showToast("PDF Uploaded Successfully")
                }
                .addOnFailureListener { exception ->
                    showToast("PDF Upload Failed: $exception")
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_PDF_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            pdfUri = data.data
            changeTextAndImageToShowTeResult()

        }
    }

    fun showProgress(){
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Loading...")
        progressDialog.setCancelable(false)
        progressDialog.show()
    }

    fun dismissProgress() = progressDialog.dismiss()

    fun showToast(msg: String){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun downloadFile(downloadUrl: String, fileName: String) {
        val request = DownloadManager.Request(Uri.parse(downloadUrl))
            .setTitle(fileName)
            .setDescription("Downloading")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
    }

    fun changeTextAndImageToShowTeResult(){
        binding.tvResult.text = "File selected"
        binding.imgChooseFile.setImageResource(R.drawable.ic_download_done)
    }

}
