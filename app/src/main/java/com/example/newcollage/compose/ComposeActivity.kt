package com.example.newcollage.compose

import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.newcollage.R
import com.example.newcollage.compose.ui.theme.ComposeTheme

class ComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeTheme {
                Surface {
                    MessageCard(Message("Android", "Jetpack Compose"))
                }
            }
        }
    }
}

@Composable
fun MessageCard(msg: Message) {
    Row(Modifier.padding(8.dp)) {
        Image(
            painter = painterResource(R.drawable.test_2),
            contentScale = ContentScale.Crop,
            contentDescription = "Contact profile picture",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.secondary, CircleShape)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(text = msg.author)

            Spacer(modifier = Modifier.height(4.dp))

            Text(text = msg.body)
        }
    }
}



// Preview
@Composable
fun PreviewCard() {
    ComposeTheme {
        Surface {
            MessageCard(
                msg = Message("Lexi", "Hey, take a look at Jetpack Compose, it's great!")
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MessageCardPreview() {
    PreviewCard()
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun MessageCardPreviewNight() {
    PreviewCard()
}



data class Message(val author: String, val body: String)