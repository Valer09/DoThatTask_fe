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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

private data class FeatureStep(
    val emoji: String,
    val title: String,
    val body: String,
    val highlight: String,
)

private val featureSteps = listOf(
    FeatureStep(
        emoji = "🧭",
        title = "Cinque sezioni, una barra in basso",
        body = "Home: la task che stai facendo ora. Manage: tutte le task. " +
            "Completed: cosa hai già chiuso. Groups: i tuoi gruppi. Inviti: chi ti chiama.",
        highlight = "Tap = navighi. Niente menu nascosti.",
    ),
    FeatureStep(
        emoji = "👥",
        title = "Crea o entra in un gruppo",
        body = "Vai su Groups e crea il primo (es: \"Casa\", \"Coppia\", \"Team\"). " +
            "Oppure accetta un invito da Inviti. Le task vivono dentro al gruppo.",
        highlight = "Senza gruppo, niente task. È il primo passo.",
    ),
    FeatureStep(
        emoji = "🗂",
        title = "Categorie su misura",
        body = "Su Manage Tasks aggiungi categorie con un colore — Casa, Lavoro, Studio, " +
            "Spesa… ognuna è un \"barattolo\" dove infili task di quel tipo.",
        highlight = "I colori ti aiutano a riconoscerle al volo.",
    ),
    FeatureStep(
        emoji = "📝",
        title = "Riempi i barattoli",
        body = "Dentro ogni categoria aggiungi tutte le task che ti vengono in mente, " +
            "anche le piccole. Più ne hai, più opzioni avrà la pesca.",
        highlight = "Scrivi tutto, decidi dopo.",
    ),
    FeatureStep(
        emoji = "🎲",
        title = "Pesca, fai, completa",
        body = "Sulla Home tocca una categoria → l'app pesca una task a caso e la rende attiva. " +
            "Tu la fai. Premi \"Completa\". Ne peschi un'altra.",
        highlight = "Nessuna decisione su cosa fare per primo.",
    ),
)

@Composable
fun FeatureTourPage(onFinish: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { featureSteps.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp, vertical = 32.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Come funziona",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
            TextButton(onClick = onFinish) {
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
            FeatureStepContent(featureSteps[page])
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            repeat(featureSteps.size) { index ->
                val isSelected = pagerState.currentPage == index
                val width by animateDpAsState(if (isSelected) 24.dp else 8.dp)
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(width = width, height = 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f),
                        ),
                )
            }
        }

        val isLast = pagerState.currentPage == featureSteps.size - 1
        Button(
            onClick = {
                if (isLast) onFinish()
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
                text = if (isLast) "Andiamo" else "Avanti",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
            )
        }
    }
}

@Composable
private fun FeatureStepContent(step: FeatureStep) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = step.emoji, fontSize = 64.sp)
        }

        Spacer(Modifier.height(32.dp))

        Text(
            text = step.title,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = 30.sp,
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = step.body,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
        )

        Spacer(Modifier.height(24.dp))

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = TaskUIHelper.appCardColors(),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        ) {
            Text(
                text = "💡 ${step.highlight}",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            )
        }
    }
}
