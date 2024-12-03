package com.spoiligaming.explorer.server

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList

object LiveServerEntryList {
    lateinit var serverNameList: SnapshotStateList<String>
        private set

    lateinit var serverAddressList: SnapshotStateList<String>
        private set

    var selectedServer by mutableStateOf<Int?>(null)

    fun initialize(
        names: SnapshotStateList<String>,
        addresses: SnapshotStateList<String>,
    ) {
        serverNameList = names
        serverAddressList = addresses
    }

    fun updateServerName(
        index: Int,
        name: String,
    ) {
        require(index in 0 until serverNameList.size) { "Index $index is out of bounds." }

        serverNameList[index] = name
        ServerFileHandler.renameServer(index, name)
    }

    fun updateServerAddress(
        index: Int,
        address: String,
    ) {
        require(index in 0 until serverNameList.size) { "Index $index is out of bounds." }

        serverAddressList[index] = address
        ServerFileHandler.changeAddress(index, address)
    }

    fun updateServerIcon(
        index: Int,
        icon: String?,
    ) = icon?.let { ServerFileHandler.modifyIcon(index, it) }
        ?: run { ServerFileHandler.deleteServerIcon(index) }

    fun deleteServer(index: Int) {
        require(index in 0 until serverNameList.size) { "Index $index is out of bounds." }
        serverNameList.removeAt(index)
        serverAddressList.removeAt(index)
        selectedServer = null
        ServerFileHandler.deleteServer(index)
    }

    fun wipeServerFile() {
        serverNameList.clear()
        serverAddressList.clear()
        selectedServer = null
        ServerFileHandler.wipeServerFile()
    }

    fun createServer(
        acceptedTextures: Int?,
        icon: String?,
        address: String,
        name: String,
    ) {
        serverNameList.add(name)
        serverAddressList.add(address)
        ServerFileHandler.createServerEntry(acceptedTextures, icon, address, name)
    }
}
