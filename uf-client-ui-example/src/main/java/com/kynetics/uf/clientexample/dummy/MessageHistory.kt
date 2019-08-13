package com.kynetics.uf.clientexample.dummy

import com.kynetics.uf.android.api.v1.UFServiceMessageV1
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Date
import java.util.HashMap

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 *
 * TODO: Replace all uses of this class before publishing your app.
 */

fun Long.toDate(): String {
    val sdf = DateFormat.getDateTimeInstance()
    return sdf.format(Date(this))
}

fun Double.format(minFractionDigits: Int = 0): String {
    val format = NumberFormat.getNumberInstance()
    format.maximumFractionDigits = minFractionDigits
    format.minimumFractionDigits = minFractionDigits
    return format.format(this)
}

object MessageHistory {
    const val CAPACITY = 100
    /**
     * An array of sample (dummy) items.
     */
    val ITEMS: MutableList<StateEntry> = ArrayList(CAPACITY)

    /**
     * A map of sample (dummy) items, by ID.
     */
    val ITEM_MAP: MutableMap<Long, StateEntry> = HashMap(CAPACITY)

    /**
     * A dummy item representing a piece of content.
     */

    data class StateEntry(
        val id: Long = System.currentTimeMillis(),
        val state: UFServiceMessageV1.State,
        val events: MutableList<EventEntry> = ArrayList(CAPACITY),
        var unread: Int = 0
    ) {

        fun addEvent(item: UFServiceMessageV1.Event) {
            if (events.size == CAPACITY) {
                events.removeAt(0)
            }
            events.add(EventEntry(System.currentTimeMillis().toDate(), item))
            unread++
        }

        fun printDate(): String {
            return id.toDate()
        }
    }

    data class EventEntry(val date: String, val event: UFServiceMessageV1.Event) {
        override fun toString(): String {
            val baseMessage = "$date - ${event.name}"
            return when (event) {
                is UFServiceMessageV1.Event.StartDownloadFile -> print(baseMessage, event)
                is UFServiceMessageV1.Event.FileDownloaded -> print(baseMessage, event)
                is UFServiceMessageV1.Event.UpdateFinished -> print(baseMessage, event)
                is UFServiceMessageV1.Event.Error -> print(baseMessage, event)
                is UFServiceMessageV1.Event.DownloadProgress -> print(baseMessage, event)
                is UFServiceMessageV1.Event.UpdateProgress -> print(baseMessage, event)
                else -> return baseMessage
            }
        }

        private fun print(infix: String, event: UFServiceMessageV1.Event.StartDownloadFile): String =
            "$infix \nFile Name: ${event.fileName}"
        private fun print(infix: String, event: UFServiceMessageV1.Event.FileDownloaded): String =
            "$infix \nFile Name: ${event.fileDownloaded}"
        private fun print(infix: String, event: UFServiceMessageV1.Event.UpdateFinished): String = """
            "$infix \nUpdate Result: ${if (event.successApply) "applied" else "not applied" }
            ${event.details.joinToString("\n")}"
        """.trimIndent()
        private fun print(infix: String, event: UFServiceMessageV1.Event.Error): String =
            "$infix \n${event.details.joinToString("\n")}"
        private fun print(infix: String, event: UFServiceMessageV1.Event.DownloadProgress): String =
            "$infix \n${event.fileName} is downloaded at ${event.percentage.format(2)}"
        private fun print(infix: String, event: UFServiceMessageV1.Event.UpdateProgress): String =
            "$infix  \nPhase name:${event.phaseName} is at ${event.percentage.format(2)}"
    }

    fun appendEvent(event: UFServiceMessageV1.Event) {
        ITEMS.last().addEvent(event)
    }

    fun addState(item: StateEntry) {
        if (ITEMS.size == CAPACITY) {
            val itemToRemove = ITEMS.removeAt(0)
            ITEM_MAP.remove(itemToRemove.id)
        }
        ITEMS.add(item)
        ITEM_MAP[item.id] = item
    }
}
