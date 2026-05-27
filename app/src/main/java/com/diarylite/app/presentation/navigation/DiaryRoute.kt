package com.diarylite.app.presentation.navigation

object DiaryRoute {
    const val Home = "home"
    const val Entries = "entries"
    const val Calendar = "calendar"
    const val AddEntry = "entry/add"
    const val AddEntryWithDate = "entry/add?entryDateEpochDay={entryDateEpochDay}"
    const val EditEntry = "entry/edit/{entryId}"
    const val DetailEntry = "entry/detail/{entryId}"
    const val Search = "search"
    const val Settings = "settings"

    fun addEntry(entryDateEpochDay: Long? = null): String =
        if (entryDateEpochDay == null) AddEntry else "entry/add?entryDateEpochDay=$entryDateEpochDay"

    fun editEntry(entryId: Long): String = "entry/edit/$entryId"

    fun detailEntry(entryId: Long): String = "entry/detail/$entryId"
}
