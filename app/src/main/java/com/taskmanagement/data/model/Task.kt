package com.taskmanagement.data.model

import android.os.Parcel
import android.os.Parcelable

data class Task(
    var id: String = "",
    val title: String = "",
    val description: String = "",
    val dateString: String = "",
    val date: Long = 0L,
    val time: Long = 0L,
    val startTime: Long = 0L,
    val photoFileName: String = "",
    val status: String = "Pending",
    val userId: String = ""
)