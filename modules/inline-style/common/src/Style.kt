package net.derfruhling.serenity.style

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ReusableComposition
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import net.derfruhling.serenity.HtmlComposable

object Style {
    @Composable
    @StyleComposable
    fun Rule(ruleName: String, ruleValue: String) {
        ComposeNode<RuleNode, StylistApplier<out RuleNode>>(::RuleNode, update = {
            set(ruleName) { this.ruleName = ruleName }
            set(ruleValue) { this.ruleValue = ruleValue }
        })
    }

    @Composable
    @StyleComposable
    fun <T> Rule(ruleName: String, notation: Notation<T>, ruleValueNotation: T) {
        val ruleValue = remember(ruleValueNotation) { notation.asNotationString(ruleValueNotation) }
        ComposeNode<RuleNode, StylistApplier<out RuleNode>>(::RuleNode, update = {
            set(ruleName) { this.ruleName = ruleName }
            set(ruleValue) { this.ruleValue = ruleValue }
        })
    }
}

@Composable
@HtmlComposable
fun Style(fn: @Composable @StyleComposable Style.() -> Unit): InlineStyle {
    val compositionContext = rememberCompositionContext()
    val inlineStyle = remember { InlineStyle() }
    val applier = remember { StylistApplier(inlineStyle) }
    val composition = remember(applier) { ReusableComposition(applier, compositionContext) }

    DisposableEffect(composition) {
        composition.setContentWithReuse { Style.fn() }
        onDispose { composition.dispose() }
    }

    return inlineStyle
}
