package org.telegram.version

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class VersionData (
    var android: VersionInfoData,
    var status: Int = 0
)
@Parcelize
data class VersionInfoData(
    var force: String,
    var date: String,
    var version: String,
    var downloadUrl: String
) : Parcelable