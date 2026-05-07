package homeaq.dothattask.dothattask_fe.dothattask_fe.View

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.AppState
import homeaq.dothattask.dothattask_fe.dothattask_fe.Model.Screen
import homeaq.dothattask.dothattask_fe.dothattask_fe.Network.OnboardingPreferences
import kotlinx.coroutines.launch

private data class OnboardingStep(
    val emoji: String,
    val title: String,
    val body: String,
)

private val steps = listOf(
    OnboardingStep(
        emoji = "✨",
        title = "Stop choosing.\nStart doing.",
        body = "Do That Task removes the decision of what to do next. " +
                "You set up your tasks; Do That Task tells you which one comes next.",
    ),
    OnboardingStep(
        emoji = "👥",
        title = "Create your group.",
        body = "Roommates, family, friends, team. " +
                "Have fun together with challenges you assign each other! "
    ),
    OnboardingStep(
        emoji = "🎲",
        title = "Pick a task.",
        body = "Choose a category: Home, Work, Study… " +
                "and tap \"Pick\". Do That Task chooses one for you at random. " +
                "No more procrastinating over what to do first.",
    ),
    OnboardingStep(
        emoji = "🎯",
        title = "One at a time.",
        body = "You only have one active task a week. "+
                "You get it, you complete it, then you pick another one. " +
                "The simplest way to actually get things done!",
    ),
)

@Composable
fun OnboardingPage(onFinish: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { steps.size })
    val scope = rememberCoroutineScope()

    val finish: () -> Unit = {
        OnboardingPreferences.markOnboardingSeen()
        onFinish()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp, vertical = 32.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = finish) {
                Text(
                    text = "Salta",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 0.dp),
        ) { page ->
            OnboardingStepContent(steps[page])
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            repeat(steps.size) { index ->
                val isSelected = pagerState.currentPage == index
                val width by animateDpAsState(if (isSelected) 24.dp else 8.dp)
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .height(8.dp)
                        .widthIn(min = width, max = width)
                        .size(width = width, height = 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f),
                        ),
                )
            }
        }

        val isLast = pagerState.currentPage == steps.size - 1
        Button(
            onClick = {
                if (isLast) finish()
                else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Text(
                text = if (isLast) "Let's start!" else "Next",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
            )
        }

        Spacer(Modifier.height(8.dp))

        TextButton(
            onClick = {
                OnboardingPreferences.markOnboardingSeen()
                AppState.changePage(Screen.Login)
                onFinish()
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "I already have an account",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun OnboardingStepContent(step: OnboardingStep) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = step.emoji, fontSize = 76.sp)
        }

        Spacer(Modifier.height(40.dp))

        Text(
            text = step.title,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = 32.sp,
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = step.body,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
        )
    }
}
