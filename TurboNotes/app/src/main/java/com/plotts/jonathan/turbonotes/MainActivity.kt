package com.plotts.jonathan.turbonotes

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.plotts.jonathan.turbonotes.ui.theme.AppTheme
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.UUID


data class RuneData(
    @DrawableRes val id: Int,
    val description: String,
    var uuid: UUID = UUID.randomUUID(),
    var matched: Boolean = false,
    var flipped: Boolean = false,
) {


    fun getForegroundResource(): Int =
        when {
            matched -> R.drawable.check
            flipped -> id
            else -> R.drawable.card_background
        }

    fun immutableUpdate(
        @DrawableRes id: Int? = null,
        description: String? = null,
        uuid: UUID? = null,
        matched: Boolean? = null,
        flipped: Boolean? = null,
    ) = RuneData(
        id?:this.id,
        description?:this.description,
        uuid?:this.uuid,
        matched?:this.matched,
        flipped?:this.flipped,
    )

    companion object {

        fun duplicateWithNewUUID(runeData: RuneData) =
            RuneData(
                runeData.id,
                runeData.description,
                UUID.randomUUID()
            )
    }
}

class RavensViewModel : ViewModel() {
    fun tapOnRune(uuid: UUID) {

        val memoryState = _gameState.duplicate()
        val mutableData = memoryState.runeList.toMutableList()
        val tappedRune = mutableData.find { it.uuid == uuid } ?: return
        if(tappedRune.matched) return
        tappedRune.flipped = tappedRune.flipped.not()

        var newMatch = false
        var shouldFlipCardsBackDown = false
        val flippedCards = memoryState.runeList.filter { it.flipped }

        //flip a card up
        viewModelScope.launch {
            val newList = mutableData.map { it.immutableUpdate() }
            _gameState = MemoryGameState(newList,
                memoryState.score,memoryState.isGameOver)
            _uiState.emit(_gameState)
        }
        when {
            flippedCards.size < 2 -> {}
            flippedCards.size == 2 -> {
                if (flippedCards[0].id == flippedCards[1].id) {
                    flippedCards[0].matched = true
                    flippedCards[1].matched = true
                    flippedCards[0].flipped = false
                    flippedCards[1].flipped = false
                } else {
                    flippedCards[0].flipped = false
                    flippedCards[1].flipped = false
                }
                shouldFlipCardsBackDown = true
            }
        }

        if (shouldFlipCardsBackDown) {
            viewModelScope.launch {
                val newList = mutableData.map { it.immutableUpdate() }
                var keepPlaying = false
                newList.forEach { if(it.matched.not()){keepPlaying = true} }
                _gameState = MemoryGameState(newList, memoryState.score+1,keepPlaying.not())

                delay(1000)
                _uiState.emit(_gameState)
            }
        }
    }

    fun restartGame() {
        startGame()
    }

    private val runesMasterList = mutableListOf(
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

    //for tracking game state
    private var _gameState = MemoryGameState(emptyList(),0,false)

    //for tracking display state
    private val _uiState = MutableStateFlow(_gameState)
    // The UI collects from this StateFlow to get its state updates
    @OptIn(FlowPreview::class)
    val uiState: Flow<MemoryGameState> = _uiState

    private fun startGame(){
        val runesContent = mutableListOf<RuneData>()
        runesMasterList.shuffle()
        //add 8 runes to the game
        runesContent.addAll(runesMasterList.subList(0, 8))
        //add their match
        val tempList = mutableListOf<RuneData>()
        runesContent.forEach {
            tempList.add(RuneData.duplicateWithNewUUID(it))
        }
        runesContent.addAll(tempList)
        runesContent.shuffle()
        viewModelScope.launch {
            _gameState = MemoryGameState(runesContent,0,false)
            _uiState.emit(_gameState)
        }
    }
    init {
       startGame()
    }

}

data class MemoryGameState(
    val runeList: List<RuneData>,
    val score: Int,
    val isGameOver:Boolean,
){

    fun duplicate():MemoryGameState = MemoryGameState(runeList.map { it.immutableUpdate() },score,isGameOver)

}

class MainActivity : AppCompatActivity() {

    private val viewModel by viewModels<RavensViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {

                ComposeEverything()
            }
        }
    }
    @Composable
    fun ComposeYouWin(score: Int) {
        Dialog(
            onDismissRequest = { viewModel.restartGame() },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false)
        ) {
            ElevatedCard(
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 6.dp),
                ) {
                    Column {
                        Text(
                            text = getString(R.string.game_over_title),
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = getString(R.string.game_over_text, score),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ){
                            Button(
                                onClick = { viewModel.restartGame() },
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = getString(R.string.game_over_play_again),
                                    style = MaterialTheme.typography.bodyMedium
                                )

                            }
                        }
                    }
                }
        }

    }
    @Composable
    fun ComposeEverything(){
        val uiState: MemoryGameState by viewModel.uiState.collectAsStateWithLifecycle(
            MemoryGameState(emptyList(),0,false)
        )
        AnimatedContent(
                targetState = uiState.isGameOver,
        label = "") {gameOver ->
            when{
                gameOver -> {
                    ComposeRunes(runes = uiState.runeList,uiState.score)
                    ComposeYouWin(uiState.score)
                }
                else ->  ComposeRunes(runes = uiState.runeList,uiState.score)
            }
        }


    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ComposeRunes(runes: List<RuneData>,score:Int) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "Raven's Memory") },
                    actions = {Text(text = "Score: $score")},
                   
                )
            },
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(),
         content =   { contentPadding ->
             LazyVerticalGrid(
                 columns = GridCells.Fixed(resources.getInteger(R.integer.columns)),
                 verticalArrangement = Arrangement.Top,
                 horizontalArrangement = Arrangement.SpaceEvenly,
                 contentPadding = contentPadding,
                 modifier = Modifier.fillMaxSize()
             ) {
                 items(runes) { rune ->
                     Box(Modifier.padding(8.dp)) {
                         DrawRuneWithAnimation(runeData = rune)

                     }
                 }
             }
         }
        )
    }


    @Composable
    fun DrawRuneWithAnimation(runeData: RuneData){
        AnimatedContent(
            targetState = runeData,
            label = "") {
            DrawRuneCard(it)
        }
    }
    @Composable
    fun DrawRuneCard(runeData: RuneData) {
        ElevatedCard(
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            ),
            modifier = Modifier
                .aspectRatio(.5f, false)
        ) {
            Box(
                modifier = Modifier.clickable { viewModel.tapOnRune(runeData.uuid) }
            ) {
                Image(
                    painter = painterResource(id = runeData.getForegroundResource()),
                    contentDescription = runeData.description,
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.Center)
                        .fillMaxSize()
                )
            }
        }
    }

}