package com.thething.cos.morphogenesis

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.thething.cos.lifecycle.DefaultCosLifecycleEngine
import com.thething.cos.lifecycle.LifecycleAction
import com.thething.cos.lifecycle.LifecycleStageCommand
import com.thething.cos.lifecycle.morpho.SharedMorphoFormChannel
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MorphogenesisViewModelInstrumentedTest {

    @Test
    fun activateDraftEmitsEvent() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val engine = DefaultCosLifecycleEngine()
        val repository = InMemoryMorphoFormRepository()
        val dispatcher = AndroidMorphoEventDispatcher(context)
        val channel = SharedMorphoFormChannel()
        val viewModel = MorphogenesisViewModel(engine, repository, dispatcher, channel, kotlinx.coroutines.Dispatchers.IO)

        engine.apply(LifecycleAction.SetStage(LifecycleStageCommand.BUD))
        engine.apply(LifecycleAction.SetStage(LifecycleStageCommand.MATURE))
        viewModel.addCell()
        viewModel.activateDraft()
    }
}


