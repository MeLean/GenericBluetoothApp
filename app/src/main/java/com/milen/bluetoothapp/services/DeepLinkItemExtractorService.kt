package com.milen.bluetoothapp.services

import android.net.Uri
import com.milen.bluetoothapp.utils.EMPTY_STRING

class DeepLinkItemExtractorService {

    fun extractQueryParams(data: Uri?, callback: OnItemsExtracted){
        val result : Map<String, String>? = createMapFromUri(data)
        callback.onItemsExtracted(result)
    }

    private fun createMapFromUri(data: Uri?): Map<String, String>? {
        return data?.let {uri ->
            uri.queryParameterNames.map { it to (uri.getQueryParameter(it) ?: EMPTY_STRING)}.toMap()
        }
    }

    interface OnItemsExtracted{
        fun onItemsExtracted(items: Map<String, String>?)
    }
}


