package at.mocode.clients.shared.commonui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import at.mocode.clients.shared.presentation.state.Notification
import at.mocode.clients.shared.presentation.state.NotificationType

@Composable
fun NotificationCard(
    notification: Notification,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (notification.type) {
        NotificationType.SUCCESS -> Color(0xFF4CAF50).copy(alpha = 0.1f)
        NotificationType.ERROR -> Color(0xFFF44336).copy(alpha = 0.1f)
        NotificationType.WARNING -> Color(0xFFFF9800).copy(alpha = 0.1f)
        NotificationType.INFO -> Color(0xFF2196F3).copy(alpha = 0.1f)
    }

    val borderColor = when (notification.type) {
        NotificationType.SUCCESS -> Color(0xFF4CAF50)
        NotificationType.ERROR -> Color(0xFFF44336)
        NotificationType.WARNING -> Color(0xFFFF9800)
        NotificationType.INFO -> Color(0xFF2196F3)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                if (notification.message.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = notification.message,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = notification.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Text("×", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
fun NotificationList(
    notifications: List<Notification>,
    onDismissNotification: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        notifications.forEach { notification ->
            NotificationCard(
                notification = notification,
                onDismiss = { onDismissNotification(notification.id) }
            )
        }
    }
}

@Composable
fun SnackbarNotification(
    notification: Notification,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (notification.type) {
        NotificationType.SUCCESS -> Color(0xFF4CAF50)
        NotificationType.ERROR -> Color(0xFFF44336)
        NotificationType.WARNING -> Color(0xFFFF9800)
        NotificationType.INFO -> Color(0xFF2196F3)
    }

    Snackbar(
        modifier = modifier,
        containerColor = backgroundColor,
        contentColor = Color.White,
        action = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.White
                )
            ) {
                Text("Dismiss")
            }
        }
    ) {
        Column {
            Text(
                text = notification.title,
                fontWeight = FontWeight.SemiBold
            )
            if (notification.message.isNotBlank()) {
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun ToastNotification(
    message: String,
    type: NotificationType = NotificationType.INFO,
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (visible) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(3000) // Auto dismiss after 3 seconds
            onDismiss()
        }

        val backgroundColor = when (type) {
            NotificationType.SUCCESS -> Color(0xFF4CAF50)
            NotificationType.ERROR -> Color(0xFFF44336)
            NotificationType.WARNING -> Color(0xFFFF9800)
            NotificationType.INFO -> Color(0xFF2196F3)
        }

        Card(
            modifier = modifier,
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text(
                text = message,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
