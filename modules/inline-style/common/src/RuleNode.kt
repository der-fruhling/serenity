package net.derfruhling.serenity.style

class RuleNode() : StyleNode() {
    lateinit var ruleName: String
    lateinit var ruleValue: String

    constructor(from: RuleNode) : this() {
        if (from::ruleName.isInitialized) ruleName = from.ruleName
        if (from::ruleValue.isInitialized) ruleValue = from.ruleValue
    }

    override fun deepCopy() = RuleNode(this)
}
