package com.chan.geotryout

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class GeoMainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setComposeView()
    }

    private fun setComposeView() {
        setContent {
            Column(modifier = Modifier.fillMaxWidth()) {
                SingleText("Geo Coordinates")
                displayGeoLocation("Latitude", "1.2222.333")
                displayGeoLocation("Langtitude")
            }
        }
    }

    @Composable
    private fun displayGeoLocation(label: String, value: String = "") {
        Row(modifier = Modifier.fillMaxWidth()) {
            SingleText(label)
            SingleText(value)
        }
    }

    @Composable
    private fun SingleText(valueText: String) {
        Text(
            text = valueText,
            style = TextStyle(fontSize = 20.sp),
            modifier = Modifier
                .testTag("singleText")
                .fillMaxWidth()
                .padding(16.dp)
        )
    }

}