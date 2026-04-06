package com.animesh.gullak.ui.goals

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.animesh.gullak.domain.model.NoSpendChallenge
import com.animesh.gullak.domain.model.SavingsGoal
import com.animesh.gullak.ui.theme.GoalYellow
import com.animesh.gullak.ui.theme.IncomeGreen
import com.animesh.gullak.ui.theme.SavingsBlue
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(viewModel: GoalsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val form by viewModel.addGoalForm.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Goals & Challenges", fontWeight = FontWeight.SemiBold) },
                actions = {
                    IconButton(onClick = viewModel::showAddGoalDialog) {
                        Icon(Icons.Default.Add, "Add goal")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // No-spend challenge section
                item {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Active Challenge",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    if (state.activeChallenge != null) {
                        NoSpendChallengeCard(
                            challenge = state.activeChallenge!!,
                            onMarkDay = { viewModel.markChallengeDay(it) },
                            onEnd = { viewModel.endChallenge(it) }
                        )
                    } else {
                        StartChallengeCard(onClick = viewModel::showChallengeDialog)
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Savings Goals",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "${state.savingsGoals.count { it.isCompleted }}/${state.savingsGoals.size} done",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                if (state.savingsGoals.isEmpty()) {
                    item { EmptyGoalsPlaceholder(onClick = viewModel::showAddGoalDialog) }
                } else {
                    items(state.savingsGoals, key = { it.id }) { goal ->
                        SavingsGoalCard(
                            goal = goal,
                            currencySymbol = state.currencySymbol,
                            onDelete = { viewModel.deleteGoal(goal) },
                            onAddSavings = { viewModel.addSavingsToGoal(goal, it) }
                        )
                    }
                }
            }
        }
    }

    // Add Goal Dialog
    if (state.showAddGoalDialog) {
        AddGoalDialog(
            form = form,
            onTitleChange = viewModel::updateGoalTitle,
            onAmountChange = viewModel::updateGoalAmount,
            onEmojiChange = viewModel::updateGoalEmoji,
            onToggleDeadline = viewModel::toggleDeadline,
            onSave = viewModel::saveGoal,
            onDismiss = viewModel::hideAddGoalDialog
        )
    }

    // Challenge dialog
    if (state.showChallengeDialog) {
        StartChallengeDialog(
            onStart = viewModel::startChallenge,
            onDismiss = viewModel::hideChallengeDialog
        )
    }
}

@Composable
private fun NoSpendChallengeCard(
    challenge: NoSpendChallenge,
    onMarkDay: (NoSpendChallenge) -> Unit,
    onEnd: (NoSpendChallenge) -> Unit
) {
    val daysLeft = ChronoUnit.DAYS.between(
        LocalDate.now(),
        challenge.startDate.plusDays(challenge.durationDays.toLong())
    ).coerceAtLeast(0)
    val progress = challenge.currentStreak.toFloat() / challenge.durationDays.toFloat()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🔥", fontSize = 28.sp)
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        "No-Spend Challenge",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        "$daysLeft days remaining",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    "🏆 ${challenge.currentStreak}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = GoalYellow
                )
            }

            Spacer(Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = GoalYellow,
                trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
            )

            Spacer(Modifier.height(4.dp))

            Text(
                "${challenge.currentStreak} / ${challenge.durationDays} days",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { onMarkDay(challenge) },
                    modifier = Modifier.weight(1f),
                    enabled = challenge.currentStreak < challenge.durationDays
                ) {
                    Text("✅ Mark Day")
                }
                TextButton(onClick = { onEnd(challenge) }) {
                    Text("End", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun StartChallengeCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick() },
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🔥", fontSize = 32.sp)
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Start a No-Spend Challenge", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text("Track days without spending", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
        }
    }
}

@Composable
private fun SavingsGoalCard(
    goal: SavingsGoal,
    currencySymbol: String,
    onDelete: () -> Unit,
    onAddSavings: (Double) -> Unit
) {
    val progress = (goal.savedAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f)
    var showAddDialog by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(800, easing = EaseOutCubic),
        label = "goal_progress"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Circular progress
                Box(
                    modifier = Modifier.size(52.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(
                            color = Color.LightGray.copy(alpha = 0.3f),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = if (goal.isCompleted) IncomeGreen else SavingsBlue,
                            startAngle = -90f,
                            sweepAngle = 360f * animatedProgress,
                            useCenter = false,
                            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    Text(goal.emoji, fontSize = 20.sp)
                }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        goal.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "$currencySymbol ${"%.0f".format(goal.savedAmount)} / ${"%.0f".format(goal.targetAmount)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    goal.deadline?.let { dl ->
                        val daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), dl).coerceAtLeast(0)
                        Text(
                            "⏳ $daysLeft days left",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (daysLeft < 7) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }

                if (goal.isCompleted) {
                    Text("🏆", fontSize = 24.sp)
                } else {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, "Add savings", tint = SavingsBlue)
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                }
            }

            Spacer(Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (goal.isCompleted) IncomeGreen else SavingsBlue,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(Modifier.height(4.dp))

            Text(
                "${"%.0f".format(progress * 100)}% saved",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }

    if (showAddDialog) {
        AddSavingsDialog(
            goal = goal,
            currencySymbol = currencySymbol,
            onAdd = { onAddSavings(it); showAddDialog = false },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
private fun AddSavingsDialog(
    goal: SavingsGoal,
    currencySymbol: String,
    onAdd: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var amount by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to ${goal.title}") },
        text = {
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount ($currencySymbol)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = { amount.toDoubleOrNull()?.let { onAdd(it) } }) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun AddGoalDialog(
    form: AddGoalForm,
    onTitleChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onEmojiChange: (String) -> Unit,
    onToggleDeadline: (Boolean) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val emojiOptions = listOf("🎯", "🏠", "✈️", "🚗", "📱", "💍", "🎓", "🌴", "💻", "🎮")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Savings Goal") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Emoji picker
                LazyRowEmojiPicker(
                    emojis = emojiOptions,
                    selected = form.emoji,
                    onSelect = onEmojiChange
                )
                OutlinedTextField(
                    value = form.title,
                    onValueChange = onTitleChange,
                    label = { Text("Goal title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = form.targetAmount,
                    onValueChange = onAmountChange,
                    label = { Text("Target amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = form.hasDeadline, onCheckedChange = onToggleDeadline)
                    Text("Set a deadline", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Button(onClick = onSave) { Text("Create Goal") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun LazyRowEmojiPicker(
    emojis: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        emojis.forEach { emoji ->
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (emoji == selected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable { onSelect(emoji) },
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 20.sp)
            }
        }
    }
}

@Composable
private fun StartChallengeDialog(
    onStart: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedDays by remember { mutableIntStateOf(7) }
    val options = listOf(7, 14, 21, 30)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("🔥 No-Spend Challenge") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Choose your challenge duration", style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    options.forEach { days ->
                        FilterChip(
                            selected = selectedDays == days,
                            onClick = { selectedDays = days },
                            label = { Text("${days}d") }
                        )
                    }
                }
                Text(
                    "Avoid non-essential spending for $selectedDays days. Mark each successful day to keep your streak!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        },
        confirmButton = {
            Button(onClick = { onStart(selectedDays) }) { Text("Start Challenge") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun EmptyGoalsPlaceholder(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🎯", fontSize = 48.sp)
        Spacer(Modifier.height(12.dp))
        Text("No goals yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(
            "Set a savings goal to start\nworking towards your dreams",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onClick) {
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(8.dp))
            Text("Add Goal")
        }
    }
}
