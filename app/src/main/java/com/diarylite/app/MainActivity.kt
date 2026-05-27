package com.diarylite.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.diarylite.app.presentation.DiaryViewModel
import com.diarylite.app.presentation.DiaryViewModelFactory
import com.diarylite.app.presentation.navigation.DiaryNavGraph
import com.diarylite.app.presentation.theme.DiaryLiteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = (application as DiaryLiteApplication).container.repository
        setContent {
            DiaryLiteTheme {
                val viewModel: DiaryViewModel = viewModel(
                    factory = DiaryViewModelFactory(repository),
                )
                DiaryNavGraph(viewModel = viewModel)
            }
        }
    }
}
