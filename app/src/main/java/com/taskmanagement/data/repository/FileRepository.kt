package com.taskmanagement.data.repository

import android.net.Uri
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.taskmanagement.data.model.FileData
import kotlinx.coroutines.tasks.await

class FileRepository {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val storage: StorageReference = FirebaseStorage.getInstance().reference

    suspend fun uploadFile(userId: String, fileId: String, fileName: String, fileUri: Uri, fileData: FileData): String? {
        val storageRef = storage.child("files/$fileId/$fileName")
        return try {
            val uploadTask: UploadTask = storageRef.putFile(fileUri)
            uploadTask.await()
            val downloadUrl = storageRef.downloadUrl.await()
            val fileUrl = downloadUrl.toString()

            val userFileReference = database.child("users").child(userId).child("files")
            userFileReference.push().setValue(fileData.copy(url = fileUrl)).await()

            fileUrl
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAllFiles(userId: String): List<FileData> {
        val userFileReference = database.child("users").child(userId).child("files")
        return userFileReference.get().await().children.mapNotNull { it.getValue(FileData::class.java) }
    }
}
