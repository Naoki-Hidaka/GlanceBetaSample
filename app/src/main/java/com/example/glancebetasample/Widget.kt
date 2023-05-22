package com.example.glancebetasample

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.Action
import androidx.glance.action.action
import androidx.glance.action.clickable
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch


class PrimitiveWidgetProvider : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget
        get() = PrimitiveWidget()

}

class PrimitiveWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val (data, refetch) = fetchResult()
            val coroutineScope = rememberCoroutineScope()
            GlanceTheme(colors = colorScheme()) {
                Content {
                    when (data) {
                        LoadState.Loading -> {
                            LoadingWidget()
                        }

                        is LoadState.Error -> {
                            ErrorWidget(
                                errorMessage = data.errorMessage,
                                onClick = action {
                                    coroutineScope.launch {
                                        refetch()
                                    }
                                }
                            )
                        }

                        is LoadState.Loaded -> {
                            LoadedWidget(articleResponseList = data.data)
                        }

                        else -> {
                            LoadingWidget()
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun Content(content: @Composable () -> Unit) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.background)
                .cornerRadius(8.dp)
        ) {
            content()
        }
    }

    @Composable
    private fun ErrorWidget(errorMessage: String, onClick: Action) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = errorMessage,
                style = TextStyle(
                    color = GlanceTheme.colors.error
                )
            )
            Spacer(modifier = GlanceModifier.height(8.dp))
            Button(
                text = "retry",
                onClick = onClick,
                style = TextStyle(color = GlanceTheme.colors.onError),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = GlanceTheme.colors.error,
                    contentColor = GlanceTheme.colors.onError
                )
            )
        }
    }

    @Composable
    private fun LoadingWidget() {
        Box(
            modifier = GlanceModifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = GlanceTheme.colors.primary)
        }
    }

    @Composable
    private fun LoadedWidget(articleResponseList: List<ArticleResponse>) {
        val context = LocalContext.current
        LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
            items(articleResponseList) {
                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clickable(
                            onClick = action {
                                Intent(Intent.ACTION_VIEW, Uri.parse(it.url)).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    setPackage("com.android.chrome")
                                }.run {
                                    context.startActivity(this)
                                }
                            },
                        ),
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "+${it.likesCount}",
                        style = TextStyle(color = ColorProvider(Color.Green))
                    )
                    Spacer(modifier = GlanceModifier.width(4.dp))
                    Text(
                        modifier = GlanceModifier.padding(vertical = 8.dp),
                        text = it.title,
                        style = TextStyle(
                            fontSize = 17.sp,
                            color = GlanceTheme.colors.onBackground
                        ),
                        maxLines = 1,
                    )
                }
            }
        }
    }

    @Composable
    private fun fetchResult(): ApiResult<LoadState<List<ArticleResponse>>> {
        val apiClient = QiitaApiClient.provideQiitaApiClient()
        val (result, setResult) = remember {
            mutableStateOf<LoadState<List<ArticleResponse>>>(LoadState.Loading)
        }
        val fetch = suspend {
            flow {
                runCatching {
                    apiClient.getArticles().body()!!
                }.onSuccess {
                    emit(LoadState.Loaded(it))
                }.onFailure {
                    emit(LoadState.Error(it.message.orEmpty()))
                }
            }.catch {
                emit(LoadState.Error(it.message.orEmpty()))
            }.collect {
                setResult(it)
            }
        }
        val refetch = suspend {
            flow {
                runCatching {
                    emit(LoadState.Loading)
                    apiClient.getArticles().body()!!
                }.onSuccess {
                    emit(LoadState.Loaded(it))
                }.onFailure {
                    emit(LoadState.Error(it.message.orEmpty()))
                }
            }.catch {
                emit(LoadState.Error(it.message.orEmpty()))
            }.collect {
                setResult(it)
            }
        }
        LaunchedEffect(key1 = Unit) {
            fetch()
        }
        return ApiResult(
            data = result,
            refetch = refetch
        )
    }
}

data class ApiResult<T>(
    val data: T?,
    val refetch: suspend () -> Unit
)

sealed interface LoadState<out T> {
    data class Loaded<out T>(val data: T) : LoadState<T>
    object Loading : LoadState<Nothing>
    data class Error(val errorMessage: String) : LoadState<Nothing>
}