/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.aperture.utils

import android.content.Context
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import org.lineageos.aperture.models.Rotation
import org.lineageos.aperture.models.Transform
import java.io.InputStream

class ExifUtils {
    companion object {
        private val orientationMap = mapOf(
            ExifInterface.ORIENTATION_UNDEFINED to Transform.DEFAULT,
            ExifInterface.ORIENTATION_NORMAL to Transform.DEFAULT,
            ExifInterface.ORIENTATION_ROTATE_90 to Transform(Rotation.ROTATION_90, false),
            ExifInterface.ORIENTATION_ROTATE_180 to Transform(Rotation.ROTATION_180, false),
            ExifInterface.ORIENTATION_ROTATE_270 to Transform(Rotation.ROTATION_270, false),
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL to Transform(Rotation.ROTATION_0, true),
            ExifInterface.ORIENTATION_FLIP_VERTICAL to Transform(Rotation.ROTATION_180, true),
            ExifInterface.ORIENTATION_TRANSPOSE to Transform(Rotation.ROTATION_270, true),
            ExifInterface.ORIENTATION_TRANSVERSE to Transform(Rotation.ROTATION_90, true),
        )

        private fun getOrientation(inputStream: InputStream): Int {
            inputStream.mark(Int.MAX_VALUE)
            val orientation =
                ExifInterface(inputStream).getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)
            inputStream.reset()
            return orientation
        }

        private fun orientationToTransform(exifOrientation: Int): Transform {
            return orientationMap.getOrDefault(exifOrientation, Transform.DEFAULT)
        }

        fun getTransform(inputStream: InputStream): Transform {
            return orientationToTransform(getOrientation(inputStream))
        }

        fun stripMetadata(context: Context, uri: Uri) {
            try {
                context.contentResolver.openFileDescriptor(uri, "rw")?.use { pfd ->
                    val exif = ExifInterface(pfd.fileDescriptor)
                    val tagsToClear = listOf(
                        ExifInterface.TAG_MAKE,
                        ExifInterface.TAG_MODEL,
                        ExifInterface.TAG_SOFTWARE,
                        ExifInterface.TAG_ARTIST,
                        ExifInterface.TAG_COPYRIGHT,
                        ExifInterface.TAG_IMAGE_DESCRIPTION,
                        ExifInterface.TAG_USER_COMMENT,
                        ExifInterface.TAG_IMAGE_UNIQUE_ID,
                        ExifInterface.TAG_CAMERA_OWNER_NAME,
                        ExifInterface.TAG_BODY_SERIAL_NUMBER,
                        ExifInterface.TAG_LENS_SERIAL_NUMBER,
                        ExifInterface.TAG_LENS_MAKE,
                        ExifInterface.TAG_LENS_MODEL,
                        ExifInterface.TAG_LENS_SPECIFICATION,
                        // Camera specs & settings
                        ExifInterface.TAG_EXPOSURE_TIME,
                        ExifInterface.TAG_F_NUMBER,
                        ExifInterface.TAG_ISO_SPEED_RATINGS,
                        ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY,
                        ExifInterface.TAG_FOCAL_LENGTH,
                        ExifInterface.TAG_APERTURE_VALUE,
                        ExifInterface.TAG_SHUTTER_SPEED_VALUE,
                        ExifInterface.TAG_FLASH,
                        ExifInterface.TAG_BRIGHTNESS_VALUE,
                        ExifInterface.TAG_EXPOSURE_BIAS_VALUE,
                        ExifInterface.TAG_MAX_APERTURE_VALUE,
                        ExifInterface.TAG_SUBJECT_DISTANCE,
                        ExifInterface.TAG_METERING_MODE,
                        ExifInterface.TAG_LIGHT_SOURCE,
                        ExifInterface.TAG_SENSING_METHOD,
                        ExifInterface.TAG_FILE_SOURCE,
                        ExifInterface.TAG_SCENE_TYPE,
                        ExifInterface.TAG_CFA_PATTERN,
                        ExifInterface.TAG_CUSTOM_RENDERED,
                        ExifInterface.TAG_EXPOSURE_MODE,
                        ExifInterface.TAG_WHITE_BALANCE,
                        ExifInterface.TAG_DIGITAL_ZOOM_RATIO,
                        ExifInterface.TAG_FOCAL_LENGTH_IN_35MM_FILM,
                        ExifInterface.TAG_SCENE_CAPTURE_TYPE,
                        ExifInterface.TAG_GAIN_CONTROL,
                        ExifInterface.TAG_CONTRAST,
                        ExifInterface.TAG_SATURATION,
                        ExifInterface.TAG_SHARPNESS,
                        ExifInterface.TAG_DEVICE_SETTING_DESCRIPTION,
                        ExifInterface.TAG_SUBJECT_DISTANCE_RANGE,
                        // Date/Time
                        ExifInterface.TAG_DATETIME,
                        ExifInterface.TAG_DATETIME_ORIGINAL,
                        ExifInterface.TAG_DATETIME_DIGITIZED,
                        ExifInterface.TAG_SUBSEC_TIME,
                        ExifInterface.TAG_SUBSEC_TIME_ORIGINAL,
                        ExifInterface.TAG_SUBSEC_TIME_DIGITIZED,
                        // GPS Latitude/Longitude/Altitude etc.
                        ExifInterface.TAG_GPS_LATITUDE,
                        ExifInterface.TAG_GPS_LONGITUDE,
                        ExifInterface.TAG_GPS_ALTITUDE,
                        ExifInterface.TAG_GPS_TIMESTAMP,
                        ExifInterface.TAG_GPS_DATESTAMP,
                        ExifInterface.TAG_GPS_PROCESSING_METHOD,
                        ExifInterface.TAG_GPS_LATITUDE_REF,
                        ExifInterface.TAG_GPS_LONGITUDE_REF,
                        ExifInterface.TAG_GPS_ALTITUDE_REF,
                        ExifInterface.TAG_GPS_SPEED,
                        ExifInterface.TAG_GPS_SPEED_REF,
                        ExifInterface.TAG_GPS_TRACK,
                        ExifInterface.TAG_GPS_TRACK_REF,
                        ExifInterface.TAG_GPS_IMG_DIRECTION,
                        ExifInterface.TAG_GPS_IMG_DIRECTION_REF,
                        ExifInterface.TAG_GPS_MAP_DATUM,
                        ExifInterface.TAG_GPS_DEST_LATITUDE,
                        ExifInterface.TAG_GPS_DEST_LATITUDE_REF,
                        ExifInterface.TAG_GPS_DEST_LONGITUDE,
                        ExifInterface.TAG_GPS_DEST_LONGITUDE_REF,
                        ExifInterface.TAG_GPS_DEST_BEARING,
                        ExifInterface.TAG_GPS_DEST_BEARING_REF,
                        ExifInterface.TAG_GPS_DEST_DISTANCE,
                        ExifInterface.TAG_GPS_DEST_DISTANCE_REF,
                        ExifInterface.TAG_GPS_VERSION_ID,
                    )
                    for (tag in tagsToClear) {
                        exif.setAttribute(tag, null)
                    }
                    exif.saveAttributes()
                }
            } catch (e: Exception) {
                android.util.Log.e("ExifUtils", "Failed to strip EXIF metadata from $uri", e)
            }
        }
    }
}
