package com.plotts.jonathan.turbonotes

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel


data class RuneData(@DrawableRes val id: Int, val description: String)
class RavensViewModel : ViewModel() {

    var runesMasterList = mutableListOf(
        RuneData(R.drawable.algiz, "algiz"),
        RuneData(R.drawable.ansuz, "ansuz"),
        RuneData(R.drawable.berkana, "berkana"),
        RuneData(R.drawable.ehwaz, "ehwaz"),
        RuneData(R.drawable.eihwaz, "eihwaz"),
        RuneData(R.drawable.fehu, "fehu"),
        RuneData(R.drawable.gebo, "gebo"),
        RuneData(R.drawable.hagalaz, "hagalaz"),
        RuneData(R.drawable.inguz, "inguz"),
        RuneData(R.drawable.isa, "isa"),
        RuneData(R.drawable.jera, "jera"),
        RuneData(R.drawable.kauna, "kauna"),
        RuneData(R.drawable.lagus, "lagus"),
        RuneData(R.drawable.mannaz, "mannaz"),
        RuneData(R.drawable.nauthiz, "nauthiz"),
        RuneData(R.drawable.othila, "othila"),
        RuneData(R.drawable.perth, "perth"),
        RuneData(R.drawable.raido, "raido"),
        RuneData(R.drawable.sowelu, "sowelu"),
        RuneData(R.drawable.teiwaz, "teiwaz"),
        RuneData(R.drawable.thurisaz, "thurisaz"),
        RuneData(R.drawable.urus, "urus"),
        RuneData(R.drawable.wunjo, "wunjo"),
    )

    val runesContent = mutableListOf<RuneData>()
    init {
        runesMasterList.shuffle()
        //add 8 runes to the game
        runesContent.addAll(runesMasterList.subList(0,8))
        //add their match
        runesContent.addAll(runesContent)
        runesContent.shuffle()
    }

}

class MainActivity : AppCompatActivity() {

    val viewModel by viewModels<RavensViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeEverything()
        }
    }

    @Preview
    @Composable
    fun ComposeEverything() {
        ComposeRunes(runes = viewModel.runesContent)
    }

    @Composable
    fun ComposeRunes(runes: List<RuneData>) {
        LazyVerticalGrid(
            columns = GridCells.FixedSize(60.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            items(runes) { rune ->
                DrawRuneCard(runeData = rune)
            }
        }
    }


    @Composable
    fun DrawRuneCard(runeData: RuneData) {
        ElevatedCard(
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            ),
            modifier = Modifier
                .size(width = 60.dp, height = 120.dp)
        ) {
            Box {

                Image(
                    painter = painterResource(id = runeData.id),
                    contentDescription = runeData.description,
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.Center),
                )
            }
        }
    }
}