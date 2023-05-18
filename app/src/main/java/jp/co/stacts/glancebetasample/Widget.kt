package jp.co.stacts.glancebetasample

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.padding
import androidx.glance.text.Text

class PrimitiveWidgetProvider : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget
        get() = PrimitiveWidget()

}

private val countKey = intPreferencesKey("count")

class PrimitiveWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) = provideContent {
        val count = currentState(countKey) ?: 0
        Box(modifier = GlanceModifier.background(Color.White)) {
            Column(modifier = GlanceModifier.padding(16.dp)) {
                Text(text = count.toString())
                Button(
                    text = "increment",
                    onClick = actionRunCallback<IncrementAction>()
                )
            }
        }
    }
}

private class IncrementAction : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        updateAppWidgetState(context, glanceId) { state ->
            state[countKey] = (state[countKey] ?: 0) + 1
        }
        PrimitiveWidget().update(context, glanceId)
    }
}