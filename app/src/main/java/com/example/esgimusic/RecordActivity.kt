package com.example.esgimusic

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.example.esgimusic.databinding.ActivityLibraryBinding
import com.example.esgimusic.databinding.ActivityRecordBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import java.io.File
import java.util.*
import kotlin.concurrent.thread

class RecordActivity : AppCompatActivity() {
    private var isRecord = false
    private var isPause = false
    private var mMediaRecorder: MediaRecorder? = null
    private var path: String = ""
    private lateinit var fileName: String
    val updateTimeText = 1
    val updateWaveView = 2
    val clearWaveView = 3
    private var mAudioRecord: AudioRecord? = null
    private var audioSize: Int = 0
    private var defaultFormat = "aac"
    var waveView: WaveView? = null
    private lateinit var binding: ActivityRecordBinding
    private lateinit var buttonStartRecord: ImageButton
    private lateinit var buttonDelete: ImageButton
    private lateinit var buttonPause: ImageButton
    private lateinit var buttonStop: ImageButton
    private lateinit var buttonContinue: ImageButton
    private val storage = Firebase.storage
    private val storageRef = storage.reference.child("records") // Reference to the "records" directory in Firebase Storage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        buttonStartRecord = binding.buttonStartRecord
        buttonDelete = binding.buttonDeleteRecord
        buttonPause = binding.buttonPauseRecord
        buttonStop = binding.buttonStopRecord
        buttonContinue = binding.buttonContinueRecord
        waveView = binding.voiceWaveView

        buttonStartRecord.setOnClickListener {
            startRecord()
        }

        buttonStop.setOnClickListener {
            showSaveDialog()
        }

        buttonPause.setOnClickListener {
            pauseRecord()
        }

        buttonContinue.setOnClickListener {
            continueRecord()
        }
        buttonDelete.setOnClickListener {
            showDeleteDialog()
        }

        binding.bottomNavigation.menu.findItem(R.id.navigation_record).isChecked = true
        binding.bottomNavigation.setOnNavigationItemSelectedListener {
            when(it.itemId) {
                R.id.navigation_home -> {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_search -> {
                    val intent = Intent(this, SearchActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_library -> {
                    val intent = Intent(this, LibraryActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.navigation_record -> {
                    // Handle navigation record
                    true
                }
                else -> false
            }
        }
    }


    private val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                updateTimeText -> {
                    findViewById<TextView>(R.id.record_time_text)?.text =
                        msg.obj as String
                }
                updateWaveView -> {
                    println("set waveView " + msg.obj)
                    waveView?.putValue(msg.obj as Int)
                }
                clearWaveView -> {
                    waveView?.clearValue()
                }
            }
        }
    }

    private fun startRecord() {
        isRecord = true
        buttonPause?.visibility = View.VISIBLE
        buttonStartRecord?.visibility = View.INVISIBLE
        checkPermission(this)

        path = getExternalFilesDir("").toString() + "/voice/"
        val file = File(path)
        if (!file.exists()) file.mkdirs()
        fileName = DateFormat.format("yyyyMMdd_HHmmss", Calendar.getInstance(Locale.CHINA))
            .toString() + "." + defaultFormat
        val filePath = path + fileName

        mMediaRecorder = MediaRecorder()
        mMediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mMediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mMediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mMediaRecorder?.setOutputFile(filePath)
        mMediaRecorder?.prepare()
        mMediaRecorder?.start()

        audioSize = (44100 / 40) * 2
        mAudioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            44100,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            audioSize
        )
        mAudioRecord!!.startRecording()
        countTime()
        showVoiceWave()
    }

    private fun pauseRecord() {
        isPause = true
        buttonPause?.visibility = View.INVISIBLE
        buttonContinue?.visibility = View.VISIBLE
        buttonDelete?.visibility = View.VISIBLE
        buttonStop?.visibility = View.VISIBLE
        mMediaRecorder?.pause()
    }

    private fun stopRecord() {
        isRecord = false
        isPause = false
        buttonDelete?.visibility = View.INVISIBLE
        buttonStop?.visibility = View.INVISIBLE
        buttonPause?.visibility = View.INVISIBLE
        buttonStartRecord?.visibility = View.VISIBLE
        buttonContinue?.visibility = View.INVISIBLE
        mMediaRecorder?.release()
        mMediaRecorder = null
        findViewById<TextView>(R.id.record_time_text)?.text = ""
    }

    private fun continueRecord() {
        isPause = false
        buttonPause?.visibility = View.VISIBLE
        buttonContinue?.visibility = View.INVISIBLE
        buttonDelete?.visibility = View.INVISIBLE
        buttonStop?.visibility = View.INVISIBLE
        mMediaRecorder?.resume()
    }

    private fun deleteRecord() {
        stopRecord()
        val file = File(path + fileName)
        if (file.exists())
            file.delete()
    }

    private fun checkPermission(context: Context) {
        val permissions = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        val requireList = ArrayList<String>()
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) requireList.add(permission)
        }
        if (requireList.isNotEmpty()) {
            ActivityCompat.requestPermissions(context as Activity, permissions, 1)
        }
    }

    private fun countTime() {
        thread {
            var value = 0
            while (isRecord) {

                if (!isPause) {
                    value++
                    val msg = Message()
                    val str =
                        "" + value / 100 / 60 + ":" + (if (value / 100 % 60 < 10) "0" else "") + value / 100 % 60 + ":" + (if (value % 100 < 10) "0" else "") + value % 100
                    msg.what = updateTimeText
                    msg.obj = str
                    handler.sendMessage(msg)
                    Thread.sleep(10)
                }
            }
        }
    }

    private fun showDeleteDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmation")
        builder.setMessage("Etes-vous sûr de vouloir supprimer l'enregistrement en cours ?")

        builder.setPositiveButton(
            "Supprimer"
        ) { _, _ ->
            deleteRecord()
        }
        builder.setNegativeButton("Annuler", null)
        val dialog = builder.create()
        dialog.show()
    }

    private fun showSaveDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Enregistrer le vocal")
        val input = EditText(this)
        input.setText("enregistrement_${System.currentTimeMillis()}")
        builder.setView(input)

        builder.setPositiveButton(
            "Valider"
        ) { _, _ ->
            var filenameNew = input.text.toString()
            filenameNew += ".$defaultFormat"
            stopRecord()
            if (filenameNew.isNotEmpty()) {
                val file = File(path + fileName)
                val fileNew = File(path + filenameNew)
                file.copyTo(fileNew)
                file.delete()
                val fileUri = fileNew.toUri()
                val newFileRef = storageRef.child(filenameNew)

                // Remove file extension from filenameNew for Firestore
                val rawName = filenameNew.substring(0, filenameNew.lastIndexOf('.'))

                newFileRef.putFile(fileUri)
                    .addOnSuccessListener { uploadTask ->
                        uploadTask.storage.downloadUrl.addOnSuccessListener { uri ->
                            val downloadUrl = uri.toString()
                            Log.d("UploadFile", "File uploaded successfully. Download URL: $downloadUrl")

                            // Add the song to Firestore without the file extension and with the download URL
                            val firestore = FirebaseFirestore.getInstance()
                            val enregistrementRef = firestore.collection("category").document("Enregistrement")
                            enregistrementRef.update("songs", FieldValue.arrayUnion(rawName))
                                .addOnSuccessListener {
                                    Log.d("Firestore", "Song added to Enregistrement collection")
                                }
                                .addOnFailureListener { exception ->
                                    Log.e("Firestore", "Failed to add song to Enregistrement collection: ${exception.message}", exception)
                                }

                            // Now you can also add the song to the 'songs' collection with additional data if needed
                            val songData = hashMapOf(
                                "count" to 0,
                                "coverUrl" to "https://firebasestorage.googleapis.com/v0/b/esgi-music.appspot.com/o/song_images%2Frecord.png?alt=media&token=29fe028f-fa6f-4424-bf4b-e7611e4256cb",
                                "id" to filenameNew,
                                "subtitle" to "enregistrement",
                                "title" to filenameNew,
                                "url" to downloadUrl
                            )
                            val songsRef = firestore.collection("songs")
                            songsRef.document(rawName)
                                .set(songData)
                                .addOnSuccessListener {
                                    Log.d("Firestore", "New song added to 'songs' collection")
                                }
                                .addOnFailureListener { exception ->
                                    Log.e("Firestore", "Failed to add new song to 'songs' collection: ${exception.message}", exception)
                                }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e("UploadFile", "Failed to upload file: ${exception.message}", exception)
                    }
            }
        }
        builder.setNegativeButton("Annuler", null)
        val dialog = builder.create()
        dialog.show()
    }



    private fun showVoiceWave() {
        thread {
            while (isRecord) {
                if (!isPause) {
                    val buffer = ByteArray(audioSize)
                    mAudioRecord?.read(buffer, 0, buffer.size)
                    var valueSum = 0
                    for (i in 0..audioSize - 2 step 2) {
                        if ((buffer[i + 1].toInt() and 0xff) < 0x80) {
                            val value: Int =
                                (buffer[i].toInt() and 0xff) or ((buffer[i + 1].toInt() and 0xff) shl 8)
                            valueSum += value
                        } else {
                            var value: Int =
                                (buffer[i].toInt() and 0xff) or ((buffer[i + 1].toInt() and 0xff) shl 8)
                            value = 0xffff - value + 1
                            valueSum += value
                        }
                    }
                }
            }
        }
    }
}