import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import extensions.filePath
import extensions.icon
import extensions.md5
import extensions.toByteArray
import kotlinx.coroutines.*
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.revwalk.RevCommit
import org.jetbrains.skija.Image.makeFromEncoded
import theme.backgroundColorLight
import theme.primaryTextColor
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun CommitChanges(commitDiff: Pair<RevCommit, List<DiffEntry>>, onDiffSelected: (DiffEntry) -> Unit) {
    val commit = commitDiff.first
    val diff = commitDiff.second

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
    ) {
        Column(
            modifier = Modifier
                .padding(all = 8.dp)
                .fillMaxWidth(),
        ) {
            val scroll = rememberScrollState(0)
            Text(
                text = commit.fullMessage,
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .background(MaterialTheme.colors.surface)
                    .height(120.dp)
                    .verticalScroll(scroll),

                )

            Card(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .fillMaxWidth()
                    .height(72.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val url = "https://www.gravatar.com/avatar/${commit.authorIdent.emailAddress.md5}"
                    Image(
                        bitmap = rememberNetworkImage(url),
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .height(40.dp)
                            .clip(CircleShape),
                        contentDescription = null,
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(commit.authorIdent.name)
                    }
                }

            }


        }


        CommitLogChanges(diff, onDiffSelected = onDiffSelected)
    }
}

suspend fun loadImage(link: String): ByteArray = withContext(Dispatchers.IO) {
    val url = URL(link)
    val connection = url.openConnection() as HttpURLConnection
    connection.connect()

    connection.inputStream.toByteArray()
}

@Composable
fun rememberNetworkImage(url: String): ImageBitmap {
    var image by remember(url) {
        mutableStateOf<ImageBitmap>(
            useResource("image.jpg") {
                makeFromEncoded(it.toByteArray()).asImageBitmap()
            }
        )
    }

    LaunchedEffect(url) {
        loadImage(url).let {
            image = makeFromEncoded(it).asImageBitmap()
        }
    }

    return image
}

@Composable
fun CommitLogChanges(diffEntries: List<DiffEntry>, onDiffSelected: (DiffEntry) -> Unit) {
    val selectedIndex = remember(diffEntries) { mutableStateOf(-1) }

    LazyColumn(
        modifier = Modifier
            .background(backgroundColorLight)
            .fillMaxSize()
    ) {
        itemsIndexed(items = diffEntries) { index, diffEntry ->
            val textColor = if (selectedIndex.value == index) {
                MaterialTheme.colors.primary
            } else
                MaterialTheme.colors.primaryTextColor

            Column(
                modifier = Modifier
                    .height(56.dp)
                    .fillMaxWidth()
                    .clickable {
                        selectedIndex.value = index
                        onDiffSelected(diffEntry)
                    },
                verticalArrangement = Arrangement.Center,
            ) {
                Spacer(modifier = Modifier.weight(2f))


                Row {
                    Icon(
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .size(24.dp),
                        imageVector = diffEntry.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colors.primary,
                    )

                    Text(
                        text = diffEntry.filePath,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(modifier = Modifier.weight(2f))

                Divider()
            }
        }
    }
}