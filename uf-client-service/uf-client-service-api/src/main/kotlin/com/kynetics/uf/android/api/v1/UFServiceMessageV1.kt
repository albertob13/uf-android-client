/*
 * Copyright © 2017-2020  Kynetics  LLC
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.kynetics.uf.android.api.v1

import com.kynetics.uf.android.api.Communication
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * This class maps all possible messages sent by UpdateFactoryService to the clients that are
 * subscribed to its notification system.
 *
 * @see Communication.V1.Out.ServiceNotification
 */
@Serializable
@Suppress("MaxLineLength", "unused")
sealed class UFServiceMessageV1 {
    /**
     * Enum of all the possible messages type
     */
    enum class MessageName {
        DOWNLOADING,
        ERROR,
        UPDATING,
        CANCELLING_UPDATE,
        WAITING_DOWNLOAD_AUTHORIZATION,
        WAITING_UPDATE_AUTHORIZATION,
        IDLE,
        START_DOWNLOAD_FILE,
        DOWNLOAD_PROGRESS,
        UPDATE_PROGRESS,
        FILE_DOWNLOADED,
        UPDATE_FINISHED,
        POLLING,
        ALL_FILES_DOWNLOADED,
        UPDATE_AVAILABLE,
        CONFIGURATION_ERROR
    }

    override fun toString(): String {
        return this.javaClass.simpleName
    }

    /**
     * Message description
     */
    abstract val description: String
    /**
     * Message type
     */
    abstract val name: MessageName

    /**
     * Class that maps all the possible actions that the service is doing
     */
    @Serializable
    sealed class State(override val name: MessageName, override val description: String) : UFServiceMessageV1() {

        /**
         *  Client is downloading artifacts from server
         *
         *  @property artifacts list of all artifacts to download
         */
        @Serializable
        data class Downloading(val artifacts: List<Artifact>) : State(MessageName.DOWNLOADING, "Client is downloading artifacts from server") {
            override fun toJson(): String {
                return json.encodeToString(serializer(), this)
            }

            /**
             * This class represent a file to download
             *
             * @property name file's name
             * @property size file's size in byte
             * @property md5 file's md5
             */
            @Serializable
            data class Artifact(val name: String, val size: Long, val md5: String)
        }

        /**
         *  Client has started the update process. Any request to cancel an update will be rejected
         *
         */
        object Updating : State(MessageName.UPDATING, "The update process is started. Any request to cancel an update will be rejected")

        /**
         *  Client is cancelling the last update request
         */
        object CancellingUpdate : State(MessageName.CANCELLING_UPDATE, "Last update request is being cancelled")

        /**
         *  Client is waiting for an authorization to start the artifacts downloading
         */
        object WaitingDownloadAuthorization : State(MessageName.WAITING_DOWNLOAD_AUTHORIZATION, "Waiting authorization to start download")

        /**
         *  Client is waiting for an authorization to start the update
         */
        object WaitingUpdateAuthorization : State(MessageName.WAITING_UPDATE_AUTHORIZATION, "Waiting authorization to start update")

        /**
         *  Client is waiting for new requests from server
         */
        object Idle : State(MessageName.IDLE, "Client is waiting for new requests from server")

        /**
         * Bad service configuration
         *
         * @property details optional additional information about the errors
         */
        @Serializable
        data class ConfigurationError(val details: List<String> = emptyList()) : State(MessageName.CONFIGURATION_ERROR, "Bad service configuration") {
            override fun toJson(): String {
                return json.encodeToString(serializer(), this)
            }
        }
    }

    /**
     * Class that maps all the events that are notified
     */
    @Serializable
    sealed class Event(override val name: MessageName, override val description: String) : UFServiceMessageV1() {
        /**
         * Client is contacting server to retrieve new action to execute
         */
        object Polling : Event(MessageName.POLLING, "Client is contacting server to retrieve new action to execute")

        /**
         * A file downloading is started
         *
         * @property fileName file's name
         */
        @Serializable
        data class StartDownloadFile(val fileName: String) : Event(MessageName.START_DOWNLOAD_FILE, "A file downloading is started") {
            override fun toJson(): String {
                return json.encodeToString(serializer(), this)
            }
        }

        /**
         * A file is downloaded
         *
         * @property fileDownloaded name of file downloaded
         */
        @Serializable
        data class FileDownloaded(val fileDownloaded: String) : Event(MessageName.FILE_DOWNLOADED, "A file is downloaded") {
            override fun toJson(): String {
                return json.encodeToString(serializer(), this)
            }
        }

        /**
         * Percent of file downloaded
         *
         * @property fileName file's name
         * @property percentage percentage of file that it is downloaded
         */
        @Serializable
        data class DownloadProgress(val fileName: String, val percentage: Double = 0.0) : Event(MessageName.DOWNLOAD_PROGRESS, "Percent of file downloaded") {
            override fun toJson(): String {
                return json.encodeToString(serializer(), this)
            }
        }

        /**
         * All file needed are downloaded
         */
        object AllFilesDownloaded : Event(MessageName.ALL_FILES_DOWNLOADED, "All file needed are downloaded")

        /**
         * Update process is finish
         *
         * @property successApply true if the system is successfully updated, false otherwise
         * @property details optional additional details
         */
        @Serializable
        data class UpdateFinished(val successApply: Boolean, val details: List<String> = emptyList()) : Event(MessageName.UPDATE_FINISHED, "The update is finished") {
            override fun toJson(): String {
                return json.encodeToString(serializer(), this)
            }
        }

        /**
         * An error is occurred
         *
         * @property details optional additional details about the error
         */
        @Serializable
        data class Error(val details: List<String> = emptyList()) : Event(MessageName.ERROR, "An error is occurred") {
            override fun toJson(): String {
                return json.encodeToString(serializer(), this)
            }
        }

        /**
         * Update phase
         *
         * @property phaseName name of the update phase
         * @property phaseDescription description of the update phase
         * @property percentage percentage of update phase
         */
        @Serializable
        data class UpdateProgress(val phaseName: String, val phaseDescription: String = "", val percentage: Double = 0.0) : Event(MessageName.UPDATE_PROGRESS, "Phase of update") {
            override fun toJson(): String {
                return json.encodeToString(serializer(), this)
            }
        }

        /**
         * An update is available on cloud
         *
         * @property id update's id
         */
        @Serializable
        data class UpdateAvailable(val id: String) : Event(MessageName.UPDATE_AVAILABLE, "An update is available on cloud") {
            override fun toJson(): String {
                return json.encodeToString(serializer(), this)
            }
        }
    }

    @Serializable
    private class SerializationObject(val name: MessageName, val description: String)

    open fun toJson(): String {
        return json.encodeToString(SerializationObject.serializer(), SerializationObject(name, description))
    }

    companion object {

        private val json = Json { encodeDefaults = true }

        /**
         * Deserialize a [jsonContent] element into a corresponding object of type [UFServiceMessageV1].
         * @throws [SerializationException] if given input can not be deserialized
         * @throws [IllegalArgumentException] if given input isn't a UFServiceMessageV1 json serialization
         */
        @Suppress("ComplexMethod")
        fun fromJson(jsonContent: String): UFServiceMessageV1 {
            val jsonElement = json.parseToJsonElement(jsonContent)
            return when (jsonElement.jsonObject["name"]?.jsonPrimitive?.content) {

                MessageName.DOWNLOADING.name -> json.decodeFromString(State.Downloading.serializer(), jsonContent)
                MessageName.UPDATING.name -> State.Updating
                MessageName.CANCELLING_UPDATE.name -> State.CancellingUpdate
                MessageName.WAITING_DOWNLOAD_AUTHORIZATION.name -> State.WaitingDownloadAuthorization
                MessageName.WAITING_UPDATE_AUTHORIZATION.name -> State.WaitingUpdateAuthorization
                MessageName.IDLE.name -> State.Idle

                MessageName.ERROR.name -> json.decodeFromString(Event.Error.serializer(), jsonContent)
                MessageName.START_DOWNLOAD_FILE.name ->json.decodeFromString(Event.StartDownloadFile.serializer(), jsonContent)
                MessageName.UPDATE_PROGRESS.name -> json.decodeFromString(Event.UpdateProgress.serializer(), jsonContent)
                MessageName.DOWNLOAD_PROGRESS.name -> json.decodeFromString(Event.DownloadProgress.serializer(), jsonContent)
                MessageName.FILE_DOWNLOADED.name -> json.decodeFromString(Event.FileDownloaded.serializer(), jsonContent)
                MessageName.UPDATE_FINISHED.name -> json.decodeFromString(Event.UpdateFinished.serializer(), jsonContent)
                MessageName.POLLING.name -> Event.Polling
                MessageName.ALL_FILES_DOWNLOADED.name -> Event.AllFilesDownloaded
                MessageName.UPDATE_AVAILABLE.name -> json.decodeFromString(Event.UpdateAvailable.serializer(), jsonContent)
                MessageName.CONFIGURATION_ERROR.name -> json.decodeFromString(State.ConfigurationError.serializer(), jsonContent)

                else -> throw IllegalArgumentException("$jsonContent is not obtained by toJson method of ${UFServiceMessageV1::class.java.simpleName}")
            }
        }
    }
}
