package net.derfruhling.serenity.test

import androidx.compose.runtime.Composable
import androidx.compose.runtime.withRunningRecomposer
import kotlinx.coroutines.test.runTest
import net.derfruhling.serenity.tree.HtmlCompositionContext
import net.derfruhling.serenity.tree.composeHtmlOnce
import net.derfruhling.serenity.tree.platform.DocumentFragment
