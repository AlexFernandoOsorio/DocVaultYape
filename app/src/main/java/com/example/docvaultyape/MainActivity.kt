package com.example.docvaultyape

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.compose.rememberNavController
import com.example.docvaultyape.presentation.navigation.DocVaultNavGraph
import com.example.docvaultyape.ui.theme.DocVaultYapeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DocVaultYapeTheme {
                val navController = rememberNavController()
                DocVaultNavGraph(navController = navController)
            }
        }
    }
}
