package com.example.newcollage.compose

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
                    LazyColumn {
                        items(datas) {
                            MessageCard(msg = it)
                        }
                    }
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
            Text(
                text = msg.author,
                color = MaterialTheme.colorScheme.secondaryContainer,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))
            Surface(shape = MaterialTheme.shapes.medium, tonalElevation = 2.dp) {
                Text(
                    text = msg.body,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }
}


// Preview
@Composable
fun PreviewCard() {
    ComposeTheme {
        Surface {
            LazyColumn {
                items(datas) {
                    MessageCard(msg = it)
                }
            }
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Composable
fun MessageCardPreview() {
    PreviewCard()
}

@Preview(name = "Dark Mode", showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun MessageCardPreviewNight() {
    PreviewCard()
}

data class Message(val author: String, val body: String)

val datas = listOf(
    Message(
        "Colleague",
        "Test...Test...Test..."
    ),
    Message(
        "Colleague",
        "List of Android versions:\n" +
                "Android KitKat (API 19)\n" +
                "Android Lollipop (API 21)\n" +
                "Android Marshmallow (API 23)\n" +
                "Android Nougat (API 24)\n" +
                "Android Oreo (API 26)\n" +
                "Android Pie (API 28)\n" +
                "Android 10 (API 29)\n" +
                "Android 11 (API 30)\n" +
                "Android 12 (API 31)\n"
    ),
    Message(
        "Colleague",
        "I think Kotlin is my favorite programming language.\n" +
                "It's so much fun!"
    ),
    Message(
        "Colleague",
        "Searching for alternatives to XML layouts..."
    ),
    Message(
        "Colleague",
        "Hey, take a look at Jetpack Compose, it's great!\n" +
                "It's the Android's modern toolkit for building native UI." +
                "It simplifies and accelerates UI development on Android." +
                "Less code, powerful tools, and intuitive Kotlin APIs :)"
    ),
    Message(
        "Colleague",
        "It's available from API 21+ :)"
    ),
    Message(
        "Colleague",
        "Writing Kotlin for UI seems so natural, Compose where have you been all my life?"
    ),
    Message(
        "Colleague",
        "Android Studio next version's name is Arctic Fox"
    ),
    Message(
        "Colleague",
        "Android Studio Arctic Fox tooling for Compose is top notch ^_^"
    ),
    Message(
        "Colleague",
        "I didn't know you can now run the emulator directly from Android Studio"
    ),
    Message(
        "Colleague",
        "Compose Previews are great to check quickly how a composable layout looks like"
    ),
    Message(
        "Colleague",
        "Previews are also interactive after enabling the experimental setting"
    ),
    Message(
        "Colleague",
        "Have you tried writing build.gradle with KTS?"
    ),
)