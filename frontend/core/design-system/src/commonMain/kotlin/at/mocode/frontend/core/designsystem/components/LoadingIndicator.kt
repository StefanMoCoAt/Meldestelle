package at.mocode.frontend.core.designsystem.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

enum class LoadingSize {
  SMALL, MEDIUM, LARGE
}

@Composable
fun LoadingIndicator(
  modifier: Modifier = Modifier,
  size: LoadingSize = LoadingSize.MEDIUM,
  message: String? = null
) {
  val indicatorSize = when (size) {
    LoadingSize.SMALL -> 24.dp
    LoadingSize.MEDIUM -> 32.dp
    LoadingSize.LARGE -> 48.dp
  }

  val strokeWidth = when (size) {
    LoadingSize.SMALL -> 2.dp
    LoadingSize.MEDIUM -> 3.dp
    LoadingSize.LARGE -> 4.dp
  }

  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    CircularProgressIndicator(
      modifier = Modifier.size(indicatorSize),
      strokeWidth = strokeWidth
    )

    if (message != null) {
      Spacer(modifier = Modifier.height(16.dp))
      Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center
      )
    }
  }
}

@Composable
fun FullScreenLoading(
  message: String = "Loading...",
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.Center
  ) {
    LoadingIndicator(
      size = LoadingSize.LARGE,
      message = message
    )
  }
}

@Composable
fun InlineLoading(
  message: String? = null,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier.padding(16.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.Center
  ) {
    LoadingIndicator(
      size = LoadingSize.SMALL,
      message = message
    )
  }
}

@Composable
fun LinearLoadingIndicator(
  modifier: Modifier = Modifier,
  message: String? = null
) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    LinearProgressIndicator(
      modifier = Modifier.fillMaxWidth()
    )

    if (message != null) {
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = message,
        style = MaterialTheme.typography.bodySmall,
        textAlign = TextAlign.Center
      )
    }
  }
}
